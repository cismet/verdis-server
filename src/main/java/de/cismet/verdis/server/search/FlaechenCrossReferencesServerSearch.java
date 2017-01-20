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

import de.cismet.verdis.commons.constants.FlaechePropertyConstants;
import de.cismet.verdis.commons.constants.FlaechenPropertyConstants;
import de.cismet.verdis.commons.constants.FlaecheninfoPropertyConstants;
import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;
import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;

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
                    + "    kassenzeichen1." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kz1, "
                    + "    flaeche1." + FlaechePropertyConstants.PROP__ID + " AS fid, "
                    + "    flaeche1." + FlaechePropertyConstants.PROP__FLAECHENBEZEICHNUNG + " AS f1, "
                    + "    kassenzeichen2." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kz2, "
                    + "    flaeche2." + FlaechePropertyConstants.PROP__FLAECHENBEZEICHNUNG + " AS f2 "
                    + "FROM "
                    + "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen1, "
                    + "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen2, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHEN + " AS flaechen1, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHEN + " AS flaechen2, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHE + " AS flaeche1, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHE + " AS flaeche2, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHENINFO + " AS flaecheninfo1, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHENINFO + " AS flaecheninfo2 "
                    + "WHERE "
                    + "    kassenzeichen1." + KassenzeichenPropertyConstants.PROP__ID + " = flaechen1."
                    + FlaechenPropertyConstants.PROP__KASSENZEICHEN_REFERENCE + " AND "
                    + "    kassenzeichen2." + KassenzeichenPropertyConstants.PROP__ID + " = flaechen2."
                    + FlaechenPropertyConstants.PROP__KASSENZEICHEN_REFERENCE + " AND "
                    + "    flaechen1." + FlaechenPropertyConstants.PROP__FLAECHE + " = flaeche1."
                    + FlaechePropertyConstants.PROP__ID + " AND "
                    + "    flaechen2." + FlaechenPropertyConstants.PROP__FLAECHE + " = flaeche2."
                    + FlaechePropertyConstants.PROP__ID + " AND "
                    + "    flaeche1." + FlaechePropertyConstants.PROP__FLAECHENINFO + " = flaecheninfo1."
                    + FlaecheninfoPropertyConstants.PROP__ID + " AND "
                    + "    flaeche2." + FlaechePropertyConstants.PROP__FLAECHENINFO + " = flaecheninfo2."
                    + FlaecheninfoPropertyConstants.PROP__ID + " AND "
                    + "    flaecheninfo2." + FlaecheninfoPropertyConstants.PROP__ID + " = flaecheninfo1."
                    + FlaecheninfoPropertyConstants.PROP__ID + " AND "
                    + "    NOT flaechen2." + FlaechenPropertyConstants.PROP__KASSENZEICHEN_REFERENCE + " = flaechen1."
                    + FlaechenPropertyConstants.PROP__KASSENZEICHEN_REFERENCE + " AND "
                    + "    kassenzeichen1." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " = "
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
