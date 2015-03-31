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
import de.cismet.verdis.commons.constants.VeranlagungsgrundlagePropertyConstants;
import de.cismet.verdis.commons.constants.VeranlagungsnummerPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;
import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;
/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */

public class FlaechenSummenServerSearch extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(FlaechenSummenServerSearch.class);

    //~ Instance fields --------------------------------------------------------

    String searchQuery = "";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RegenFlaechenSummenServerSearch object.
     *
     * @param  kz  DOCUMENT ME!
     */
    public FlaechenSummenServerSearch(final int kz) {
        searchQuery = ""
                    + "SELECT "
                    + "    sub.bezeichner, "
                    + "    sum(groesse) AS Groesse, "
                    + "    round(sum(GroesseGewichtet) * 10000) / 10000 as GroesseGewichtet "
                    + "FROM ( "
                    + "    SELECT "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHE + "." + FlaechePropertyConstants.PROP__ID
                    + ", "
                    + "        " + VerdisMetaClassConstants.MC_VERANLAGUNGSNUMMER + "."
                    + VeranlagungsnummerPropertyConstants.PROP__BEZEICHNER + " AS bezeichner, "
                    + "        (" + VerdisMetaClassConstants.MC_FLAECHENINFO + "."
                    + FlaecheninfoPropertyConstants.PROP__GROESSE_KORREKTUR + ") AS Groesse, "
                    + "        (" + VerdisMetaClassConstants.MC_FLAECHENINFO + "."
                    + FlaecheninfoPropertyConstants.PROP__GROESSE_KORREKTUR + " * "
                    + VerdisMetaClassConstants.MC_VERANLAGUNGSGRUNDLAGE + "."
                    + VeranlagungsgrundlagePropertyConstants.PROP__VERANLAGUNGSSCHLUESSEL + ") AS GroesseGewichtet "
                    + "    FROM "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHEN + ", "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHE + ", "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHENINFO + ", "
                    + "        " + VerdisMetaClassConstants.MC_VERANLAGUNGSGRUNDLAGE + ", "
                    + "        " + VerdisMetaClassConstants.MC_VERANLAGUNGSNUMMER + ", "
                    + "        " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " "
                    + "    WHERE "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHE + "."
                    + FlaechePropertyConstants.PROP__ANTEIL + " IS null AND "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHEN + "."
                    + FlaechenPropertyConstants.PROP__KASSENZEICHEN_REFERENCE + " = "
                    + VerdisMetaClassConstants.MC_KASSENZEICHEN + "." + KassenzeichenPropertyConstants.PROP__ID
                    + " AND "
                    + "        " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "."
                    + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " = " + Integer.toString(kz) + " AND "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHEN + "." + FlaechenPropertyConstants.PROP__FLAECHE
                    + " = " + VerdisMetaClassConstants.MC_FLAECHE + "." + FlaechePropertyConstants.PROP__ID
                    + " AND "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHE + "."
                    + FlaechePropertyConstants.PROP__FLAECHENINFO + " = "
                    + VerdisMetaClassConstants.MC_FLAECHENINFO + "." + FlaecheninfoPropertyConstants.PROP__ID + " AND "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHENINFO + "."
                    + FlaecheninfoPropertyConstants.PROP__FLAECHENART + " = "
                    + VerdisMetaClassConstants.MC_VERANLAGUNGSGRUNDLAGE + "."
                    + VeranlagungsgrundlagePropertyConstants.PROP__FLAECHENART + " AND "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHENINFO + "."
                    + FlaecheninfoPropertyConstants.PROP__ANSCHLUSSGRAD + " = "
                    + VerdisMetaClassConstants.MC_VERANLAGUNGSGRUNDLAGE + "."
                    + VeranlagungsgrundlagePropertyConstants.PROP__ANSCHLUSSGRAD + " AND "
                    + "        " + VerdisMetaClassConstants.MC_VERANLAGUNGSGRUNDLAGE + "."
                    + VeranlagungsgrundlagePropertyConstants.PROP__VERANLAGUNGSNUMMER + " = "
                    + VerdisMetaClassConstants.MC_VERANLAGUNGSNUMMER + "."
                    + VeranlagungsnummerPropertyConstants.PROP__ID + " "
                    + "    UNION "
                    + "    SELECT "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHE + "." + FlaechePropertyConstants.PROP__ID
                    + ", "
                    + "        " + VerdisMetaClassConstants.MC_VERANLAGUNGSNUMMER + "."
                    + VeranlagungsnummerPropertyConstants.PROP__BEZEICHNER + " AS bezeichner, "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHE + "."
                    + FlaechePropertyConstants.PROP__ANTEIL + " AS Groesse, "
                    + "        (" + VerdisMetaClassConstants.MC_FLAECHE + "."
                    + FlaechePropertyConstants.PROP__ANTEIL + " * "
                    + VerdisMetaClassConstants.MC_VERANLAGUNGSGRUNDLAGE + "."
                    + VeranlagungsgrundlagePropertyConstants.PROP__VERANLAGUNGSSCHLUESSEL + ") AS GroesseGewichtet "
                    + "    FROM "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHEN + ", "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHE + ", "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHENINFO + ", "
                    + "        " + VerdisMetaClassConstants.MC_VERANLAGUNGSGRUNDLAGE + ", "
                    + "        " + VerdisMetaClassConstants.MC_VERANLAGUNGSNUMMER + ", "
                    + "        " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " "
                    + "    WHERE "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHE + "."
                    + FlaechePropertyConstants.PROP__ANTEIL + " IS NOT null AND "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHEN + ".kassenzeichen_reference = "
                    + VerdisMetaClassConstants.MC_KASSENZEICHEN + "." + KassenzeichenPropertyConstants.PROP__ID
                    + " AND "
                    + "        " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "."
                    + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " = " + Integer.toString(kz) + " AND "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHEN + "." + FlaechenPropertyConstants.PROP__FLAECHE
                    + " = " + VerdisMetaClassConstants.MC_FLAECHE + "." + FlaechePropertyConstants.PROP__ID
                    + " AND "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHE + "."
                    + FlaechePropertyConstants.PROP__FLAECHENINFO + " = " + VerdisMetaClassConstants.MC_FLAECHENINFO
                    + "." + FlaecheninfoPropertyConstants.PROP__ID + " AND "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHENINFO + "."
                    + FlaecheninfoPropertyConstants.PROP__FLAECHENART + " = "
                    + VerdisMetaClassConstants.MC_VERANLAGUNGSGRUNDLAGE + "."
                    + VeranlagungsgrundlagePropertyConstants.PROP__FLAECHENART + " AND "
                    + "        " + VerdisMetaClassConstants.MC_FLAECHENINFO + "."
                    + FlaecheninfoPropertyConstants.PROP__ANSCHLUSSGRAD + " = "
                    + VerdisMetaClassConstants.MC_VERANLAGUNGSGRUNDLAGE + "."
                    + VeranlagungsgrundlagePropertyConstants.PROP__ANSCHLUSSGRAD + " AND "
                    + "        " + VerdisMetaClassConstants.MC_VERANLAGUNGSGRUNDLAGE + "."
                    + VeranlagungsgrundlagePropertyConstants.PROP__VERANLAGUNGSNUMMER + " = "
                    + VerdisMetaClassConstants.MC_VERANLAGUNGSNUMMER + "."
                    + VeranlagungsnummerPropertyConstants.PROP__ID + " "
                    + ") AS sub "
                    + "GROUP BY bezeichner "
                    + "HAVING bezeichner IS NOT null "
                    + "ORDER BY 1";
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
