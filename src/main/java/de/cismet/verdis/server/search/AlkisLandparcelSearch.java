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
            final Geometry geometry = (Geometry)getGeometry();

            final String sql = "SELECT DISTINCT alkis_landparcel.id "
                        + "FROM "
                        + "   alkis_landparcel "
                        + "WHERE "
                        + "   st_GeomFromText('" + geometry.toText() + "', " + geometry.getSRID()
                        + ") && alkis_landparcel.geometrie AND"
                        + "   ST_intersects(ST_GeomFromText('" + geometry.toText() + "', " + geometry.getSRID()
                        + "), alkis_landparcel.geometrie)";
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
