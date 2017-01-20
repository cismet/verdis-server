/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.cismet.verdis.commons.constants.FlaecheninfoPropertyConstants;
import de.cismet.verdis.commons.constants.FrontinfoPropertyConstants;
import de.cismet.verdis.commons.constants.GeomPropertyConstants;
import de.cismet.verdis.commons.constants.KassenzeichenGeometriePropertyConstants;
import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;
import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class KassenzeichenGeomSearch extends GeomServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(KassenzeichenGeomSearch.class);

    //~ Instance fields --------------------------------------------------------

    private boolean flaecheFilter = false;
    private boolean frontFilter = false;
    private boolean allgemeinFilter = false;
    private double scaleDenominator = 0.0d;

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        Geometry searchGeometry = getGeometry();

        if (searchGeometry != null) {
            final ArrayList<String> joinFilter = new ArrayList<String>();
            final ArrayList<String> whereFilter = new ArrayList<String>();

            if (flaecheFilter) {
                joinFilter.add(VerdisMetaClassConstants.MC_FLAECHENINFO + " AS flaecheninfo");
                whereFilter.add("flaecheninfo." + FlaecheninfoPropertyConstants.PROP__GEOMETRIE + " = geom."
                            + GeomPropertyConstants.PROP__ID);
            }
            if (frontFilter) {
                searchGeometry = searchGeometry.buffer(0.0010 * scaleDenominator);

                joinFilter.add(VerdisMetaClassConstants.MC_FRONTINFO + " AS frontinfo");
                whereFilter.add("frontinfo." + FrontinfoPropertyConstants.PROP__GEOMETRIE + " = geom."
                            + GeomPropertyConstants.PROP__ID);
            }
            if (allgemeinFilter) {
                joinFilter.add(VerdisMetaClassConstants.MC_KASSENZEICHEN_GEOMETRIE + " AS kassenzeichen_geometrie");
                whereFilter.add("kassenzeichen_geometrie." + KassenzeichenGeometriePropertyConstants.PROP__GEOMETRIE
                            + " = geom." + GeomPropertyConstants.PROP__ID);
            }

            final String geomFromText = "GeomFromText('" + searchGeometry.toText() + "', " + searchGeometry.getSRID()
                        + ")";
            final String sqlDerived = "SELECT "
                        + "    DISTINCT " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "."
                        + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kassenzeichennumer "
                        + "FROM "
                        + "    cs_attr_object_derived, "
                        + "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen, "
                        + ((joinFilter.isEmpty()) ? "" : (implodeArray(joinFilter.toArray(new String[0]), ", ") + ", "))
                        + "    " + VerdisMetaClassConstants.MC_GEOM + " AS geom "
                        + "WHERE "
                        + ((whereFilter.isEmpty())
                            ? " TRUE " : ("(" + implodeArray(whereFilter.toArray(new String[0]), " OR ") + ")"))
                        + "    AND cs_attr_object_derived.class_id = 11 "
                        + "    AND cs_attr_object_derived.attr_class_id = 0 "
                        + "    AND kassenzeichen." + KassenzeichenPropertyConstants.PROP__ID
                        + " = cs_attr_object_derived.object_id "
                        + "    AND kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER
                        + " IS NOT NULL "
                        + "    AND geom." + GeomPropertyConstants.PROP__ID + " = cs_attr_object_derived.attr_object_id "
                        + "    AND ST_Intersects("
                        + "       (SELECT geom." + GeomPropertyConstants.PROP__GEO_FIELD + "), "
                        + "       " + geomFromText
                        + "    ) ORDER BY kassenzeichennumer ASC;";

            final MetaService metaService = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);

            // ids der kassenzeichen sammeln
            final Set<Integer> idSet = new HashSet<Integer>();
            if (LOG.isDebugEnabled()) {
                LOG.debug(sqlDerived);
            }
            try {
                for (final ArrayList fields : metaService.performCustomSearch(sqlDerived)) {
                    idSet.add((Integer)fields.get(0));
                }
            } catch (Exception ex) {
                LOG.error("problem during kassenzeichen geom search", ex);
            }

            // ids der Kassenzeichen sortieren
            final List<Integer> sortedIdList = Arrays.asList(idSet.toArray(new Integer[0]));
            Collections.sort(sortedIdList);

            //
            return sortedIdList;
        } else {
            LOG.info("searchGeometry is null, geom search is not possible");
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flaecheFilter  DOCUMENT ME!
     */
    public void setFlaecheFilter(final boolean flaecheFilter) {
        this.flaecheFilter = flaecheFilter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  frontFilter  DOCUMENT ME!
     */
    public void setFrontFilter(final boolean frontFilter) {
        this.frontFilter = frontFilter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  allgemeinFilter  DOCUMENT ME!
     */
    public void setAllgemeinFilter(final boolean allgemeinFilter) {
        this.allgemeinFilter = allgemeinFilter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   inputArray  DOCUMENT ME!
     * @param   glueString  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String implodeArray(final String[] inputArray, final String glueString) {
        String output = "";
        if (inputArray.length > 0) {
            final StringBuilder sb = new StringBuilder();
            sb.append(inputArray[0]);
            for (int i = 1; i < inputArray.length; i++) {
                sb.append(glueString);
                sb.append(inputArray[i]);
            }
            output = sb.toString();
        }
        return output;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  scaleDenominator  DOCUMENT ME!
     */
    public void setScaleDenominator(final double scaleDenominator) {
        this.scaleDenominator = scaleDenominator;
    }
}
