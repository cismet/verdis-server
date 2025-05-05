/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.search.AbstractCidsServerSearch;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */

public class BuchungsblattLanparcelsGeomServerSearch extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(BuchungsblattLanparcelsGeomServerSearch.class);
    private static final float FLURSTUECKBUFFER_FOR_KASSENZEICHEN_GEOMSEARCH = -0.1f;

    //~ Instance fields --------------------------------------------------------

    String searchQuery = "";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RegenFlaechenSummenServerSearch object.
     *
     * @param  bbcode  kz DOCUMENT ME!
     */
    public BuchungsblattLanparcelsGeomServerSearch(final String bbcode) {
        searchQuery = ""
                    + "SELECT st_astext(ST_SimplifyPreserveTopology(st_union(st_buffer(alkis_buchungsblatt_landparcel.geometrie, "
                    + FLURSTUECKBUFFER_FOR_KASSENZEICHEN_GEOMSEARCH
                    + ")), 0.1)), st_srid(max(alkis_buchungsblatt_landparcel.geometrie)) "
                    + "FROM alkis_buchungsblatt, alkis_buchungsblatt_to_buchungsblattlandparcels, alkis_buchungsblatt_landparcel "
                    + "WHERE "
                    + "alkis_buchungsblatt.landparcels = alkis_buchungsblatt_to_buchungsblattlandparcels.buchungsblatt_reference "
                    + "AND alkis_buchungsblatt_to_buchungsblattlandparcels.buchungsblatt_landparcel = alkis_buchungsblatt_landparcel.id "
                    + "AND alkis_buchungsblatt.buchungsblattcode ILIKE '" + bbcode + "' "
                    + "GROUP BY alkis_buchungsblatt.id";
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        final MetaService ms = (MetaService)getActiveLocalServers().get("WUNDA_BLAU");
        if (ms != null) {
            try {
                final ArrayList<ArrayList> lists = ms.performCustomSearch(searchQuery);
                if (lists.isEmpty()) {
                    return lists;
                } else {
                    return lists.get(0);
                }
            } catch (RemoteException ex) {
                LOG.error("error while performing custom server search", ex);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return searchQuery;
    }
}
