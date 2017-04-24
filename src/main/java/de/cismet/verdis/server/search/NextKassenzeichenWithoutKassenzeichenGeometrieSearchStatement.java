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

import de.cismet.verdis.commons.constants.KassenzeichenGeometriePropertyConstants;
import de.cismet.verdis.commons.constants.KassenzeichenGeometrienPropertyConstants;
import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;
import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;

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
                : ("WHERE " + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " "
                            + "> '" + kassenzeichennummer + "' ");

            final String sql = "SELECT " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "."
                        + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER
                        + " FROM " + VerdisMetaClassConstants.MC_KASSENZEICHEN
                        + " LEFT JOIN " + VerdisMetaClassConstants.MC_KASSENZEICHEN_GEOMETRIEN + " ON "
                        + VerdisMetaClassConstants.MC_KASSENZEICHEN_GEOMETRIEN + "."
                        + KassenzeichenGeometrienPropertyConstants.PROP__KASSENZEICHEN_REFERENCE + " = "
                        + VerdisMetaClassConstants.MC_KASSENZEICHEN + "." + KassenzeichenPropertyConstants.PROP__ID
                        + " LEFT JOIN " + VerdisMetaClassConstants.MC_KASSENZEICHEN_GEOMETRIE + " ON "
                        + VerdisMetaClassConstants.MC_KASSENZEICHEN_GEOMETRIEN + "."
                        + KassenzeichenGeometrienPropertyConstants.PROP__KASSENZEICHEN_GEOMETRIE + " = "
                        + VerdisMetaClassConstants.MC_KASSENZEICHEN_GEOMETRIE + "."
                        + KassenzeichenPropertyConstants.PROP__ID + " " + whereKassenzeichennummer
                        + "GROUP BY " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "."
                        + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + ", "
                        + VerdisMetaClassConstants.MC_KASSENZEICHEN_GEOMETRIEN + "."
                        + KassenzeichenGeometrienPropertyConstants.PROP__KASSENZEICHEN_REFERENCE
                        + " HAVING count(" + VerdisMetaClassConstants.MC_KASSENZEICHEN_GEOMETRIEN + "."
                        + KassenzeichenGeometrienPropertyConstants.PROP__KASSENZEICHEN_REFERENCE + ") = 0 ORDER BY "
                        + VerdisMetaClassConstants.MC_KASSENZEICHEN + "."
                        + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " LIMIT 1";

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
