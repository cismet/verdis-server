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

public class FlaechenCrossReferencesServerSearch extends AbstractCidsServerSearch {

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
                    + "    kassenzeichen1." + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " AS kz1, "
                    + "    flaeche1." + VerdisConstants.PROP.FLAECHE.ID + " AS fid, "
                    + "    flaeche1." + VerdisConstants.PROP.FLAECHE.FLAECHENBEZEICHNUNG + " AS f1, "
                    + "    kassenzeichen2." + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " AS kz2, "
                    + "    flaeche2." + VerdisConstants.PROP.FLAECHE.FLAECHENBEZEICHNUNG + " AS f2 "
                    + "FROM "
                    + "    " + VerdisConstants.MC.KASSENZEICHEN + " AS kassenzeichen1, "
                    + "    " + VerdisConstants.MC.KASSENZEICHEN + " AS kassenzeichen2, "
                    + "    " + VerdisConstants.MC.FLAECHEN + " AS flaechen1, "
                    + "    " + VerdisConstants.MC.FLAECHEN + " AS flaechen2, "
                    + "    " + VerdisConstants.MC.FLAECHE + " AS flaeche1, "
                    + "    " + VerdisConstants.MC.FLAECHE + " AS flaeche2, "
                    + "    " + VerdisConstants.MC.FLAECHENINFO + " AS flaecheninfo1, "
                    + "    " + VerdisConstants.MC.FLAECHENINFO + " AS flaecheninfo2 "
                    + "WHERE "
                    + "    kassenzeichen1." + VerdisConstants.PROP.KASSENZEICHEN.ID + " = flaechen1."
                    + VerdisConstants.PROP.FLAECHEN.KASSENZEICHEN_REFERENCE + " AND "
                    + "    kassenzeichen2." + VerdisConstants.PROP.KASSENZEICHEN.ID + " = flaechen2."
                    + VerdisConstants.PROP.FLAECHEN.KASSENZEICHEN_REFERENCE + " AND "
                    + "    flaechen1." + VerdisConstants.PROP.FLAECHEN.FLAECHE + " = flaeche1."
                    + VerdisConstants.PROP.FLAECHE.ID + " AND "
                    + "    flaechen2." + VerdisConstants.PROP.FLAECHEN.FLAECHE + " = flaeche2."
                    + VerdisConstants.PROP.FLAECHE.ID + " AND "
                    + "    flaeche1." + VerdisConstants.PROP.FLAECHE.FLAECHENINFO + " = flaecheninfo1."
                    + VerdisConstants.PROP.FLAECHENINFO.ID + " AND "
                    + "    flaeche2." + VerdisConstants.PROP.FLAECHE.FLAECHENINFO + " = flaecheninfo2."
                    + VerdisConstants.PROP.FLAECHENINFO.ID + " AND "
                    + "    flaecheninfo2." + VerdisConstants.PROP.FLAECHENINFO.ID + " = flaecheninfo1."
                    + VerdisConstants.PROP.FLAECHENINFO.ID + " AND "
                    + "    NOT flaechen2." + VerdisConstants.PROP.FLAECHEN.KASSENZEICHEN_REFERENCE + " = flaechen1."
                    + VerdisConstants.PROP.FLAECHEN.KASSENZEICHEN_REFERENCE + " AND "
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
