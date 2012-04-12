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

import java.util.ArrayList;
import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class AlkisLandparcelSearch extends GeomServerSearch {

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
            if (getLog().isDebugEnabled()) {
                getLog().debug(sql);
            }
            final MetaService metaService = (MetaService)getActiveLoaclServers().get("WUNDA_BLAU");
            final ArrayList<ArrayList> result = metaService.performCustomSearch(sql);

            final ArrayList<Integer> ids = new ArrayList<Integer>();
            for (final ArrayList fields : result) {
                ids.add((Integer)fields.get(0));
            }

            return ids;
        } catch (final Exception e) {
            getLog().fatal("problem during landparcel search", e);
            return null;
        }
    }
}
