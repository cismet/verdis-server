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
public class NextKassenzeichenWithoutKassenzeichenGeometrieSearchStatement extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(
            NextKassenzeichenWithoutKassenzeichenGeometrieSearchStatement.class);

    //~ Instance fields --------------------------------------------------------

    private final Integer kassenzeichennummer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenSearchStatement object.
     *
     * @param  kassenzeichennummer  DOCUMENT ME!
     */
    public NextKassenzeichenWithoutKassenzeichenGeometrieSearchStatement(final Integer kassenzeichennummer) {
        this.kassenzeichennummer = kassenzeichennummer; // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<Integer> performServerSearch() {
        try {
            final String whereKassenzeichennummer = (kassenzeichennummer == null)
                ? ""
                : ("WHERE " + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " "
                            + "> '" + kassenzeichennummer + "' ");

            final String sql = "SELECT " + VerdisConstants.MC.KASSENZEICHEN + "."
                        + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER
                        + " FROM " + VerdisConstants.MC.KASSENZEICHEN
                        + " LEFT JOIN " + VerdisConstants.MC.KASSENZEICHEN_GEOMETRIEN + " ON "
                        + VerdisConstants.MC.KASSENZEICHEN_GEOMETRIEN + "."
                        + VerdisConstants.PROP.KASSENZEICHEN_GEOMETRIEN.KASSENZEICHEN_REFERENCE + " = "
                        + VerdisConstants.MC.KASSENZEICHEN + "." + VerdisConstants.PROP.KASSENZEICHEN.ID
                        + " LEFT JOIN " + VerdisConstants.MC.KASSENZEICHEN_GEOMETRIE + " ON "
                        + VerdisConstants.MC.KASSENZEICHEN_GEOMETRIEN + "."
                        + VerdisConstants.PROP.KASSENZEICHEN_GEOMETRIEN.KASSENZEICHEN_GEOMETRIE + " = "
                        + VerdisConstants.MC.KASSENZEICHEN_GEOMETRIE + "."
                        + VerdisConstants.PROP.KASSENZEICHEN.ID + " " + whereKassenzeichennummer
                        + "GROUP BY " + VerdisConstants.MC.KASSENZEICHEN + "."
                        + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + ", "
                        + VerdisConstants.MC.KASSENZEICHEN_GEOMETRIEN + "."
                        + VerdisConstants.PROP.KASSENZEICHEN_GEOMETRIEN.KASSENZEICHEN_REFERENCE
                        + " HAVING count(" + VerdisConstants.MC.KASSENZEICHEN_GEOMETRIEN + "."
                        + VerdisConstants.PROP.KASSENZEICHEN_GEOMETRIEN.KASSENZEICHEN_REFERENCE + ") = 0 ORDER BY "
                        + VerdisConstants.MC.KASSENZEICHEN + "."
                        + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " LIMIT 1";

            final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);

            final ArrayList<ArrayList> result = ms.performCustomSearch(sql);

            final ArrayList<Integer> aln = new ArrayList<Integer>();
            for (final ArrayList al : result) {
                final Integer nextKassenzeichennummer = (Integer)al.get(0);
                aln.add(nextKassenzeichennummer);
            }

            return aln;
        } catch (final Exception e) {
            LOG.error("problem during kassenzeichen search", e); // NOI18N

            return null;
        }
    }
}
