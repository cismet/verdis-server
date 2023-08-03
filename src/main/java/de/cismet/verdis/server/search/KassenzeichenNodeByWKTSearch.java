/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.verdis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObjectNode;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.cismet.cidsx.base.types.Type;

import de.cismet.cidsx.server.api.types.SearchInfo;
import de.cismet.cidsx.server.api.types.SearchParameterInfo;
import de.cismet.cidsx.server.search.RestApiCidsServerSearch;

import de.cismet.verdis.commons.constants.VerdisConstants;

import static de.cismet.verdis.server.search.KassenzeichenGeomSearch.implodeArray;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = RestApiCidsServerSearch.class)
public class KassenzeichenNodeByWKTSearch extends KassenzeichenGeomSearch implements RestApiCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(KassenzeichenNodeByWKTSearch.class);

    private static int SRID = 25832;

    //~ Instance fields --------------------------------------------------------

    private SearchInfo searchInfo;
    @Getter private String wktString = "";

    @Getter @Setter private boolean flaecheFilter = false;
    @Getter @Setter private boolean frontFilter = false;
    @Getter @Setter private boolean allgemeinFilter = false;
    @Getter @Setter private double scaleDenominator = 0.0d;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenNodeByWKTSearch object.
     */
    public KassenzeichenNodeByWKTSearch() {
        searchInfo = new SearchInfo();
        searchInfo.setKey(this.getClass().getName());
        searchInfo.setName(this.getClass().getSimpleName());
        searchInfo.setDescription("Search for Kassenzeichen-Objects by WKT Geometry");

        final List<SearchParameterInfo> parameterDescription = new LinkedList<SearchParameterInfo>();
        SearchParameterInfo searchParameterInfo;

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("wktString");
        searchParameterInfo.setType(Type.STRING);
        parameterDescription.add(searchParameterInfo);

        searchInfo.setParameterDescription(parameterDescription);

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("flaecheFilter");
        searchParameterInfo.setType(Type.BOOLEAN);
        parameterDescription.add(searchParameterInfo);

        searchInfo.setParameterDescription(parameterDescription);

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("frontFilter");
        searchParameterInfo.setType(Type.BOOLEAN);
        parameterDescription.add(searchParameterInfo);

        searchInfo.setParameterDescription(parameterDescription);

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("allgemeinFilter");
        searchParameterInfo.setType(Type.BOOLEAN);
        parameterDescription.add(searchParameterInfo);

        searchInfo.setParameterDescription(parameterDescription);

        final SearchParameterInfo resultParameterInfo = new SearchParameterInfo();
        resultParameterInfo.setKey("return");
        resultParameterInfo.setArray(true);
        resultParameterInfo.setType(Type.NODE);
        searchInfo.setResultDescription(resultParameterInfo);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   wktStringIn  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public void setWktString(final String wktStringIn) {
        this.wktString = wktStringIn;
        Geometry geometryToUse = null;
        if (wktString != null) {
            final int skIndex = wktString.indexOf(';');
            final String wkt;
            final int wktSrid;
            if (skIndex > 0) {
                final String sridKV = wktString.substring(0, skIndex);
                final int eqIndex = sridKV.indexOf('=');

                if (eqIndex > 0) {
                    wktSrid = Integer.parseInt(sridKV.substring(eqIndex + 1));
                    wkt = wktString.substring(skIndex + 1);
                } else {
                    wkt = wktString;
                    wktSrid = SRID;
                }
            } else {
                wkt = wktString;
                wktSrid = SRID;
            }

            try {
                if (wktSrid < 0) {
                    geometryToUse = new WKTReader().read(wkt);
                    geometryToUse.setSRID(wktSrid);
                } else {
                    final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(
                                PrecisionModel.FLOATING),
                            wktSrid);
                    geometryToUse = new WKTReader(geomFactory).read(wkt);
                    geometryToUse.setSRID(wktSrid);
                }
            } catch (final Exception ex) {
                LOG.error("could not parse or transform WKT String", ex);
                throw new IllegalArgumentException(ex);
            }
        }

        super.setGeometry(geometryToUse);
    }

    @Override
    public Collection performServerSearch() {
        try {
            Geometry searchGeometry = getGeometry();

            if (searchGeometry != null) {
                final ArrayList<String> joinFilter = new ArrayList<String>();
                final ArrayList<String> whereFilter = new ArrayList<String>();

                if (flaecheFilter) {
                    joinFilter.add(VerdisConstants.MC.FLAECHENINFO + " AS flaecheninfo");
                    whereFilter.add("flaecheninfo." + VerdisConstants.PROP.FLAECHENINFO.GEOMETRIE + " = geom."
                                + VerdisConstants.PROP.GEOM.ID);
                }
                if (frontFilter) {
                    searchGeometry = searchGeometry.buffer(0.0010 * scaleDenominator);

                    joinFilter.add(VerdisConstants.MC.FRONTINFO + " AS frontinfo");
                    whereFilter.add("frontinfo." + VerdisConstants.PROP.FRONTINFO.GEOMETRIE + " = geom."
                                + VerdisConstants.PROP.GEOM.ID);
                }
                if (allgemeinFilter) {
                    joinFilter.add(VerdisConstants.MC.KASSENZEICHEN_GEOMETRIE + " AS kassenzeichen_geometrie");
                    whereFilter.add("kassenzeichen_geometrie." + VerdisConstants.PROP.KASSENZEICHEN_GEOMETRIE.GEOMETRIE
                                + " = geom." + VerdisConstants.PROP.GEOM.ID);
                }

                final String geomFromText = "st_GeomFromText('" + searchGeometry.toText() + "', "
                            + searchGeometry.getSRID()
                            + ")";
                final String sqlDerived = "SELECT DISTINCT (SELECT id FROM cs_class WHERE table_name ILIKE '"
                            + VerdisConstants.MC.KASSENZEICHEN + "' LIMIT 1) as cid, "
                            + VerdisConstants.MC.KASSENZEICHEN + ".id"
                            + " AS oid "
                            + "FROM "
                            + "    cs_attr_object_derived, "
                            + "    cs_class, "
                            + "    " + VerdisConstants.MC.KASSENZEICHEN + " AS kassenzeichen, "
                            + ((joinFilter.isEmpty()) ? ""
                                                      : (implodeArray(joinFilter.toArray(new String[0]), ", ") + ", "))
                            + "    " + VerdisConstants.MC.GEOM + " AS geom "
                            + "WHERE "
                            + ((whereFilter.isEmpty())
                                ? " TRUE " : ("(" + implodeArray(whereFilter.toArray(new String[0]), " OR ") + ")"))
                            + "    AND cs_class.table_name ilike '" + VerdisConstants.MC.KASSENZEICHEN + "' "
                            + "    AND cs_attr_object_derived.class_id = cs_class.id "
                            + "    AND cs_attr_object_derived.attr_class_id = (SELECT id FROM cs_class WHERE table_name ILIKE '"
                            + VerdisConstants.MC.GEOM + "') "
                            + "    AND kassenzeichen." + VerdisConstants.PROP.KASSENZEICHEN.ID
                            + " = cs_attr_object_derived.object_id "
                            + "    AND kassenzeichen." + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER
                            + " IS NOT NULL "
                            + "    AND geom." + VerdisConstants.PROP.GEOM.ID
                            + " = cs_attr_object_derived.attr_object_id "
                            + "    AND ST_Intersects(geom." + VerdisConstants.PROP.GEOM.GEO_FIELD + ", "
                            + "       " + geomFromText
                            + "    ) ORDER BY 2 ASC;";

                final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);

                final List<MetaObjectNode> result = new ArrayList<MetaObjectNode>();
                final ArrayList<ArrayList> searchResult = ms.performCustomSearch(sqlDerived);
                LOG.info(sqlDerived);
                for (final ArrayList al : searchResult) {
                    final int cid = (Integer)al.get(0);
                    final int oid = (Integer)al.get(1);
                    final MetaObjectNode mon = new MetaObjectNode(VerdisConstants.DOMAIN, oid, cid, "", null, null); // TODO: Check4CashedGeomAndLightweightJson
                    result.add(mon);
                }

                return result;
            } else {
                LOG.info("searchGeometry is null, geom search is not possible");
            }
        } catch (final Exception e) {
            LOG.error("problem during kassenzeichen by wkt search", e); // NOI18N
        }
        return null;
    }

    @Override
    public SearchInfo getSearchInfo() {
        return searchInfo;
    }
}
