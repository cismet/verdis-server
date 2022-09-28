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

    final int kassenzeichen;
    final boolean aktiv;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RegenFlaechenSummenServerSearch object.
     *
     * @param  kassenzeichen  DOCUMENT ME!
     */
    public FlaechenSummenServerSearch(final int kassenzeichen) {
        this(kassenzeichen, true);
    }

    /**
     * Creates a new FlaechenSummenServerSearch object.
     *
     * @param  kassenzeichen  DOCUMENT ME!
     * @param  aktiv          DOCUMENT ME!
     */
    public FlaechenSummenServerSearch(final int kassenzeichen, final boolean aktiv) {
        this.kassenzeichen = kassenzeichen;
        this.aktiv = aktiv;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);
        if (ms != null) {
            try {
                final String flaeche__id = VerdisConstants.MC.FLAECHE + "." + VerdisConstants.PROP.FLAECHE.ID;
                final String flaeche__anteil = VerdisConstants.MC.FLAECHE + "." + VerdisConstants.PROP.FLAECHE.ANTEIL;
                final String flaeche__flaecheninfo = VerdisConstants.MC.FLAECHE + "."
                            + VerdisConstants.PROP.FLAECHE.FLAECHENINFO;
                final String flaechen__flaeche = VerdisConstants.MC.FLAECHEN + "."
                            + VerdisConstants.PROP.FLAECHEN.FLAECHE;
                final String flaechen__kassenzeichen_reference = VerdisConstants.MC.FLAECHEN + "."
                            + VerdisConstants.PROP.FLAECHEN.KASSENZEICHEN_REFERENCE;
                final String flaecheninfo__id = VerdisConstants.MC.FLAECHENINFO + "."
                            + VerdisConstants.PROP.FLAECHENINFO.ID;
                final String flaecheninfo__flaechenart = VerdisConstants.MC.FLAECHENINFO + "."
                            + VerdisConstants.PROP.FLAECHENINFO.FLAECHENART;
                final String flaecheninfo__anschlussgrad = VerdisConstants.MC.FLAECHENINFO + "."
                            + VerdisConstants.PROP.FLAECHENINFO.ANSCHLUSSGRAD;
                final String flaecheninfo__groesse_korrektur = VerdisConstants.MC.FLAECHENINFO + "."
                            + VerdisConstants.PROP.FLAECHENINFO.GROESSE_KORREKTUR;
                final String kassenzeichen__id = VerdisConstants.MC.KASSENZEICHEN + "."
                            + VerdisConstants.PROP.KASSENZEICHEN.ID;
                final String kassenzeichen__kassenzeichennummer = VerdisConstants.MC.KASSENZEICHEN + "."
                            + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER;
                final String veranlagungsnummer__id = VerdisConstants.MC.VERANLAGUNGSNUMMER + "."
                            + VerdisConstants.PROP.VERANLAGUNGSNUMMER.ID;
                final String veranlagungsnummer__bezeichner = VerdisConstants.MC.VERANLAGUNGSNUMMER + "."
                            + VerdisConstants.PROP.VERANLAGUNGSNUMMER.BEZEICHNER;
                final String veranlagungsgrundlage__flaechenart = VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                            + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.FLAECHENART;
                final String veranlagungsgrundlage__anschlussgrad = VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                            + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.ANSCHLUSSGRAD;
                final String veranlagungsgrundlage__veranlagungsnummer = VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                            + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.VERANLAGUNGSNUMMER;
                final String veranlagungsgrundlage__veranlagungsschluessel = VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE
                            + "."
                            + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.VERANLAGUNGSSCHLUESSEL;
                final String veranlagungsgrundlage__aktiv = VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE + "."
                            + VerdisConstants.PROP.VERANLAGUNGSGRUNDLAGE.AKTIV;

                final String froms = String.join(
                        ",\n  ",
                        new String[] {
                            VerdisConstants.MC.FLAECHEN,
                            VerdisConstants.MC.FLAECHE,
                            VerdisConstants.MC.FLAECHENINFO,
                            VerdisConstants.MC.VERANLAGUNGSGRUNDLAGE,
                            VerdisConstants.MC.VERANLAGUNGSNUMMER,
                            VerdisConstants.MC.KASSENZEICHEN,
                        });
                final String wheres = String.join(
                        " AND\n  ",
                        new String[] {
                            String.format("%s = %s", flaechen__kassenzeichen_reference, kassenzeichen__id),
                            String.format(
                                "%s = %s",
                                kassenzeichen__kassenzeichennummer,
                                Integer.toString(kassenzeichen)),
                            String.format("%s = %s", flaechen__flaeche, flaeche__id),
                            String.format("%s = %s", flaeche__flaecheninfo, flaecheninfo__id),
                            String.format("%s = %s", flaecheninfo__flaechenart, veranlagungsgrundlage__flaechenart),
                            String.format("%s = %s", flaecheninfo__anschlussgrad, veranlagungsgrundlage__anschlussgrad),
                            String.format("%s = %s", veranlagungsgrundlage__veranlagungsnummer, veranlagungsnummer__id),
                            String.format("%s IS %s", veranlagungsgrundlage__aktiv, aktiv ? "TRUE" : "FALSE"),
                        });
                final String sub = ""
                            + "SELECT "
                            + String.join(
                                ",\n  ",
                                new String[] {
                                    flaeche__id,
                                    String.format("%s AS bezeichner", veranlagungsnummer__bezeichner),
                                    String.format("%s AS Groesse", flaecheninfo__groesse_korrektur),
                                    String.format(
                                        "(%s * %s) AS GroesseGewichtet",
                                        flaecheninfo__groesse_korrektur,
                                        veranlagungsgrundlage__veranlagungsschluessel),
                                }) + "\n"
                            + "FROM " + froms + "\n"
                            + "WHERE " + String.format("%s IS null", flaeche__anteil) + " AND " + wheres + "\n"
                            + "UNION SELECT "
                            + String.join(
                                ",\n  ",
                                new String[] {
                                    flaeche__id,
                                    String.format("%s AS bezeichner", veranlagungsnummer__bezeichner),
                                    String.format("%s AS Groesse", flaeche__anteil),
                                    String.format(
                                        "(%s * %s) AS GroesseGewichtet",
                                        flaeche__anteil,
                                        veranlagungsgrundlage__veranlagungsschluessel),
                                }) + "\n"
                            + "FROM " + froms + "\n"
                            + "WHERE " + String.format("%s IS NOT null", flaeche__anteil) + " AND " + wheres;

                final String query = String.format(""
                                + "SELECT sub.bezeichner, sum(groesse) AS Groesse, round(sum(GroesseGewichtet) * 10000) / 10000 as GroesseGewichtet "
                                + "FROM (%s) AS sub "
                                + "GROUP BY bezeichner "
                                + "HAVING bezeichner IS NOT null "
                                + "ORDER BY 1",
                        sub);
                final ArrayList<ArrayList> lists = ms.performCustomSearch(query);
                return lists;
            } catch (RemoteException ex) {
                LOG.error("error while performing custom server search", ex);
            }
        }
        return null;
    }
}
