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
            final Point pointGeometry = (Point)getGeometry();

            LOG.fatal("Pointgeom:" + pointGeometry);
            final String sql = "SELECT ASTEXT(geom.geo_field) "
                        + "FROM "
                        + "   alkis_landparcel, "
                        + "   geom "
                        + "WHERE "
                        + "   geom.id = alkis_landparcel.geometrie AND "
                        + "   ST_Within(GeomFromText('" + pointGeometry.toText() + "', " + pointGeometry.getSRID()
                        + "), geom.geo_field)";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            final MetaService metaService = (MetaService)getActiveLocalServers().get("WUNDA_BLAU");
            final ArrayList<ArrayList> result = metaService.performCustomSearch(sql);

            final ArrayList<Geometry> geoms = new ArrayList<Geometry>();
            for (final ArrayList fields : result) {
                final String geomString = (String)fields.get(0);
                final PGgeometry pgGeometry = new PGgeometry(geomString);
                final Geometry geom = PostGisGeometryFactory.createJtsGeometry(pgGeometry.getGeometry());
                LOG.fatal("FSgeom:" + geom);
                geoms.add(geom);
            }

            return geoms;
        } catch (final Exception e) {
            LOG.fatal("problem during landparcel search", e);
            return null;
        }
    }
}
