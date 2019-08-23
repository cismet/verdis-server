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

import de.cismet.verdis.commons.constants.VerdisConstants;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */

public class FrontenCrossReferencesServerSearch extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(FrontenCrossReferencesServerSearch.class);

    //~ Instance fields --------------------------------------------------------

    String searchQuery = "";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaechenCrossReferencesServerSearch object.
     *
     * @param  kzNummer  DOCUMENT ME!
     */
    public FrontenCrossReferencesServerSearch(final int kzNummer) {
        searchQuery = "SELECT "
                    + "    kassenzeichen1." + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " AS kz1, "
                    + "    front1." + VerdisConstants.PROP.FRONT.ID + " AS fid, "
                    + "    front1." + VerdisConstants.PROP.FRONT.NUMMER + " AS f1, "
                    + "    kassenzeichen2." + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " AS kz2, "
                    + "    front2." + VerdisConstants.PROP.FRONT.NUMMER + " AS f2 "
                    + "FROM "
                    + "    " + VerdisConstants.MC.KASSENZEICHEN + " AS kassenzeichen1, "
                    + "    " + VerdisConstants.MC.KASSENZEICHEN + " AS kassenzeichen2, "
                    + "    " + VerdisConstants.MC.FRONTEN + " AS fronten1, "
                    + "    " + VerdisConstants.MC.FRONTEN + " AS fronten2, "
                    + "    " + VerdisConstants.MC.FRONT + " AS front1, "
                    + "    " + VerdisConstants.MC.FRONT + " AS front2, "
                    + "    " + VerdisConstants.MC.FRONTINFO + " AS frontinfo1, "
                    + "    " + VerdisConstants.MC.FRONTINFO + " AS frontinfo2 "
                    + "WHERE "
                    + "    kassenzeichen1." + VerdisConstants.PROP.KASSENZEICHEN.ID + " = fronten1."
                    + VerdisConstants.PROP.FRONTEN.KASSENZEICHEN_REFERENCE + " AND "
                    + "    kassenzeichen2." + VerdisConstants.PROP.KASSENZEICHEN.ID + " = fronten2."
                    + VerdisConstants.PROP.FRONTEN.KASSENZEICHEN_REFERENCE + " AND "
                    + "    fronten1." + VerdisConstants.PROP.FRONTEN.FRONT + " = front1."
                    + VerdisConstants.PROP.FRONT.ID + " AND "
                    + "    fronten2." + VerdisConstants.PROP.FRONTEN.FRONT + " = front2."
                    + VerdisConstants.PROP.FRONT.ID + " AND "
                    + "    front1." + VerdisConstants.PROP.FRONT.FRONTINFO + " = frontinfo1."
                    + VerdisConstants.PROP.FRONTINFO.ID + " AND "
                    + "    front2." + VerdisConstants.PROP.FRONT.FRONTINFO + " = frontinfo2."
                    + VerdisConstants.PROP.FRONTINFO.ID + " AND "
                    + "    frontinfo2." + VerdisConstants.PROP.FRONTINFO.ID + " = frontinfo1."
                    + VerdisConstants.PROP.FRONTINFO.ID + " AND "
                    + "    NOT fronten2." + VerdisConstants.PROP.FRONTEN.KASSENZEICHEN_REFERENCE + " = fronten1."
                    + VerdisConstants.PROP.FRONTEN.KASSENZEICHEN_REFERENCE + " AND "
                    + "    kassenzeichen1." + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " = "
                    + kzNummer;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);
        if (ms != null) {
            try {
                final ArrayList<ArrayList> lists = ms.performCustomSearch(searchQuery);
                return lists;
            } catch (RemoteException ex) {
                LOG.fatal("error while performing custom server search", ex);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return searchQuery;
    }
}
