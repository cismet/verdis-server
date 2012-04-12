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

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;
/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */

public class RegenFlaechenSummenServerSearch extends CidsServerSearch {

    //~ Instance fields --------------------------------------------------------

    String searchQuery = "";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RegenFlaechenSummenServerSearch object.
     *
     * @param  kz  DOCUMENT ME!
     */
    public RegenFlaechenSummenServerSearch(final int kz) {
        searchQuery = ""
                    + "SELECT "
                    + "    sub.bezeichner, "
                    + "    sum(groesse) AS Groesse, "
                    + "    round(sum(GroesseGewichtet) * 10000) / 10000 as GroesseGewichtet "
                    + "FROM ( "
                    + "    SELECT "
                    + "        flaeche.id, "
                    + "        bezeichner, "
                    + "        flaecheninfo.groesse_korrektur AS Groesse, "
                    + "        (flaecheninfo.groesse_korrektur * veranlagungsgrundlage.veranlagungsschluessel) AS GroesseGewichtet "
                    + "    FROM "
                    + "        flaechen, "
                    + "        flaeche, "
                    + "        flaecheninfo, "
                    + "        veranlagungsgrundlage, "
                    + "        kassenzeichen "
                    + "    WHERE "
                    + "        anteil IS null AND "
                    + "        flaechen.kassenzeichen_reference = kassenzeichen.id AND "
                    + "        kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " = "
                    + Integer.toString(kz) + " AND "
                    + "        flaechen.flaeche = flaeche.id AND "
                    + "        flaeche.flaecheninfo = flaecheninfo.id AND "
                    + "        flaecheninfo.flaechenart = veranlagungsgrundlage.flaechenart AND "
                    + "        flaecheninfo.anschlussgrad = veranlagungsgrundlage.anschlussgrad "
                    + "    UNION "
                    + "    SELECT "
                    + "        flaeche.id, "
                    + "        bezeichner, "
                    + "        flaeche.anteil AS Groesse, "
                    + "        (flaeche.anteil * veranlagungsgrundlage.veranlagungsschluessel) AS GroesseGewichtet "
                    + "    FROM "
                    + "        flaechen, "
                    + "        flaeche, "
                    + "        flaecheninfo, "
                    + "        veranlagungsgrundlage, "
                    + "        kassenzeichen "
                    + "    WHERE "
                    + "        anteil IS NOT null AND "
                    + "        flaechen.kassenzeichen_reference = kassenzeichen.id AND "
                    + "        kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " = "
                    + Integer.toString(kz) + " AND "
                    + "        flaechen.flaeche = flaeche.id AND "
                    + "        flaeche.flaecheninfo = flaecheninfo.id AND "
                    + "        flaecheninfo.flaechenart = veranlagungsgrundlage.flaechenart AND "
                    + "        flaecheninfo.anschlussgrad = veranlagungsgrundlage.anschlussgrad "
                    + ") AS sub "
                    + "GROUP BY bezeichner "
                    + "HAVING bezeichner IS NOT null "
                    + "ORDER BY 1";
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
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return searchQuery;
    }
}
