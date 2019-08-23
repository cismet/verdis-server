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
                    + "        " + VerdisConstants.MC.FLAECHE + "." + VerdisConstants.PROP.FLAECHE.ID
                    + ", "
                    + "        " + VerdisConstants.MC.VERANLAGUNGSNUMMER + "."
                    + VerdisConstants.PROP.VERANLAGUNGSNUMMER.BEZEICHNER + " AS bezeichner, "
                    + "        (" + VerdisConstants.MC.FLAECHENINFO + "."
                    + VerdisConstants.PROP.FLAECHENINFO.GROESSE_KORREKTUR + ") AS Groesse, "
                    + "        (" + VerdisConstants.MC.FLAECHENINFO + "."
                    + VerdisConstants.PROP.FLAECHENINFO.GROESSE_KORREKTUR + " * "
                    + VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                    + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.VERANLAGUNGSSCHLUESSEL + ") AS GroesseGewichtet "
                    + "    FROM "
                    + "        " + VerdisConstants.MC.FLAECHEN + ", "
                    + "        " + VerdisConstants.MC.FLAECHE + ", "
                    + "        " + VerdisConstants.MC.FLAECHENINFO + ", "
                    + "        " + VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + ", "
                    + "        " + VerdisConstants.MC.VERANLAGUNGSNUMMER + ", "
                    + "        " + VerdisConstants.MC.KASSENZEICHEN + " "
                    + "    WHERE "
                    + "        " + VerdisConstants.MC.FLAECHE + "."
                    + VerdisConstants.PROP.FLAECHE.ANTEIL + " IS null AND "
                    + "        " + VerdisConstants.MC.FLAECHEN + "."
                    + VerdisConstants.PROP.FLAECHEN.KASSENZEICHEN_REFERENCE + " = "
                    + VerdisConstants.MC.KASSENZEICHEN + "." + VerdisConstants.PROP.KASSENZEICHEN.ID
                    + " AND "
                    + "        " + VerdisConstants.MC.KASSENZEICHEN + "."
                    + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " = " + Integer.toString(kz) + " AND "
                    + "        " + VerdisConstants.MC.FLAECHEN + "." + VerdisConstants.PROP.FLAECHEN.FLAECHE
                    + " = " + VerdisConstants.MC.FLAECHE + "." + VerdisConstants.PROP.FLAECHE.ID
                    + " AND "
                    + "        " + VerdisConstants.MC.FLAECHE + "."
                    + VerdisConstants.PROP.FLAECHE.FLAECHENINFO + " = "
                    + VerdisConstants.MC.FLAECHENINFO + "." + VerdisConstants.PROP.FLAECHENINFO.ID + " AND "
                    + "        " + VerdisConstants.MC.FLAECHENINFO + "."
                    + VerdisConstants.PROP.FLAECHENINFO.FLAECHENART + " = "
                    + VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                    + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.FLAECHENART + " AND "
                    + "        " + VerdisConstants.MC.FLAECHENINFO + "."
                    + VerdisConstants.PROP.FLAECHENINFO.ANSCHLUSSGRAD + " = "
                    + VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                    + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.ANSCHLUSSGRAD + " AND "
                    + "        " + VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                    + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.VERANLAGUNGSNUMMER + " = "
                    + VerdisConstants.MC.VERANLAGUNGSNUMMER + "."
                    + VerdisConstants.PROP.VERANLAGUNGSNUMMER.ID + " "
                    + "    UNION "
                    + "    SELECT "
                    + "        " + VerdisConstants.MC.FLAECHE + "." + VerdisConstants.PROP.FLAECHE.ID
                    + ", "
                    + "        " + VerdisConstants.MC.VERANLAGUNGSNUMMER + "."
                    + VerdisConstants.PROP.VERANLAGUNGSNUMMER.BEZEICHNER + " AS bezeichner, "
                    + "        " + VerdisConstants.MC.FLAECHE + "."
                    + VerdisConstants.PROP.FLAECHE.ANTEIL + " AS Groesse, "
                    + "        (" + VerdisConstants.MC.FLAECHE + "."
                    + VerdisConstants.PROP.FLAECHE.ANTEIL + " * "
                    + VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                    + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.VERANLAGUNGSSCHLUESSEL + ") AS GroesseGewichtet "
                    + "    FROM "
                    + "        " + VerdisConstants.MC.FLAECHEN + ", "
                    + "        " + VerdisConstants.MC.FLAECHE + ", "
                    + "        " + VerdisConstants.MC.FLAECHENINFO + ", "
                    + "        " + VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + ", "
                    + "        " + VerdisConstants.MC.VERANLAGUNGSNUMMER + ", "
                    + "        " + VerdisConstants.MC.KASSENZEICHEN + " "
                    + "    WHERE "
                    + "        " + VerdisConstants.MC.FLAECHE + "."
                    + VerdisConstants.PROP.FLAECHE.ANTEIL + " IS NOT null AND "
                    + "        " + VerdisConstants.MC.FLAECHEN + ".kassenzeichen_reference = "
                    + VerdisConstants.MC.KASSENZEICHEN + "." + VerdisConstants.PROP.KASSENZEICHEN.ID
                    + " AND "
                    + "        " + VerdisConstants.MC.KASSENZEICHEN + "."
                    + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " = " + Integer.toString(kz) + " AND "
                    + "        " + VerdisConstants.MC.FLAECHEN + "." + VerdisConstants.PROP.FLAECHEN.FLAECHE
                    + " = " + VerdisConstants.MC.FLAECHE + "." + VerdisConstants.PROP.FLAECHE.ID
                    + " AND "
                    + "        " + VerdisConstants.MC.FLAECHE + "."
                    + VerdisConstants.PROP.FLAECHE.FLAECHENINFO + " = " + VerdisConstants.MC.FLAECHENINFO
                    + "." + VerdisConstants.PROP.FLAECHENINFO.ID + " AND "
                    + "        " + VerdisConstants.MC.FLAECHENINFO + "."
                    + VerdisConstants.PROP.FLAECHENINFO.FLAECHENART + " = "
                    + VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                    + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.FLAECHENART + " AND "
                    + "        " + VerdisConstants.MC.FLAECHENINFO + "."
                    + VerdisConstants.PROP.FLAECHENINFO.ANSCHLUSSGRAD + " = "
                    + VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                    + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.ANSCHLUSSGRAD + " AND "
                    + "        " + VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                    + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.VERANLAGUNGSNUMMER + " = "
                    + VerdisConstants.MC.VERANLAGUNGSNUMMER + "."
                    + VerdisConstants.PROP.VERANLAGUNGSNUMMER.ID + " "
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
