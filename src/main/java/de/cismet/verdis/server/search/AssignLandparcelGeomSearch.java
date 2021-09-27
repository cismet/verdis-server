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
import com.vividsolutions.jts.geom.Point;

import org.apache.log4j.Logger;

import org.postgis.PGgeometry;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class AssignLandparcelGeomSearch extends GeomServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(AssignLandparcelGeomSearch.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getCrs() {
        return "EPSG:25832";
    }

    @Override
    public Collection performServerSearch() {
        try {
            final Geometry geometry = (Geometry)getGeometry();

            final String sql = "SELECT "
                        + "   st_ASTEXT(geom.geo_field), "
                        + "   alkis_landparcel.alkis_id "
                        + "FROM "
                        + "   alkis_landparcel, "
                        + "   geom "
                        + "WHERE "
                        + "   geom.id = alkis_landparcel.geometrie AND "
                        + "   ST_Within(GeomFromText('" + geometry.toText() + "', " + geometry.getSRID()
                        + "), geom.geo_field)";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            final MetaService metaService = (MetaService)getActiveLocalServers().get("WUNDA_BLAU");
            final ArrayList<ArrayList> results = metaService.performCustomSearch(sql);

            if (!results.isEmpty()) {
                final ArrayList data = new ArrayList();

                for (final ArrayList result : results) {
                    final String geomString = (String)result.get(0);
                    final String bezeichnung = (String)result.get(1);

                    final PGgeometry pgGeometry = new PGgeometry(geomString);
                    final Geometry geom = PostGisGeometryFactory.createJtsGeometry(pgGeometry.getGeometry());

                    data.add(geom);
                    data.add(bezeichnung);
                }

                return data;
            } else {
                return null;
            }
        } catch (final Exception e) {
            LOG.fatal("problem during landparcel search", e);
            return null;
        }
    }
}
