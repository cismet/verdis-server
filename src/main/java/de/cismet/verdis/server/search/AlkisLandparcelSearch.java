/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;

import com.vividsolutions.jts.geom.Point;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class AlkisLandparcelSearch extends GeomServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(AlkisLandparcelSearch.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getCrs() {
        return "EPSG:25832";
    }

    @Override
    public Collection performServerSearch() {
        try {
            final Point pointGeometry = (Point)getGeometry();

            final String sql = "SELECT alkis_landparcel.id "
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

            final ArrayList<Integer> ids = new ArrayList<Integer>();
            for (final ArrayList fields : result) {
                ids.add((Integer)fields.get(0));
            }

            return ids;
        } catch (final Exception e) {
            LOG.fatal("problem during landparcel search", e);
            return null;
        }
    }
}
