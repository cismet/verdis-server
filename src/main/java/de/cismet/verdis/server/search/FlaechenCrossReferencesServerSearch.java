/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.search.CidsServerSearch;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
import de.cismet.verdis.commons.constants.RegenFlaechenPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */

public class FlaechenCrossReferencesServerSearch extends CidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(FlaechenCrossReferencesServerSearch.class);

    //~ Instance fields --------------------------------------------------------

    String searchQuery = "";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaechenCrossReferencesServerSearch object.
     *
     * @param  kzNummer  DOCUMENT ME!
     */
    public FlaechenCrossReferencesServerSearch(final int kzNummer) {
        searchQuery = "SELECT "
                    + "    kassenzeichen1." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kz1, "
                    + "    flaeche1." + RegenFlaechenPropertyConstants.PROP__ID + " AS fid, "
                    + "    flaeche1." + RegenFlaechenPropertyConstants.PROP__FLAECHENBEZEICHNUNG + " AS f1, "
                    + "    kassenzeichen2." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kz2, "
                    + "    flaeche2." + RegenFlaechenPropertyConstants.PROP__FLAECHENBEZEICHNUNG + " AS f2 "
                    + "FROM "
                    + "    kassenzeichen AS kassenzeichen1, "
                    + "    kassenzeichen AS kassenzeichen2, "
                    + "    flaechen AS flaechen1, "
                    + "    flaechen AS flaechen2, "
                    + "    flaeche AS flaeche1, "
                    + "    flaeche AS flaeche2, "
                    + "    flaecheninfo AS flaecheninfo1, "
                    + "    flaecheninfo AS flaecheninfo2 "
                    + "WHERE "
                    + "    kassenzeichen1.id = flaechen1.kassenzeichen_reference AND "
                    + "    kassenzeichen2.id = flaechen2.kassenzeichen_reference AND "
                    + "    flaechen1.flaeche = flaeche1.id AND "
                    + "    flaechen2.flaeche = flaeche2.id AND "
                    + "    flaeche1.flaecheninfo = flaecheninfo1.id AND "
                    + "    flaeche2.flaecheninfo = flaecheninfo2.id AND "
                    + "    flaecheninfo2.id = flaecheninfo1.id AND "
                    + "    NOT flaechen2.kassenzeichen_reference = flaechen1.kassenzeichen_reference AND "
                    + "    kassenzeichen1." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " = "
                    + kzNummer;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        final MetaService ms = (MetaService)getActiveLoaclServers().get(VerdisConstants.DOMAIN);
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
