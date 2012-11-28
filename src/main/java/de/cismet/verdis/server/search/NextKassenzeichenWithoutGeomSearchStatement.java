/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObjectNode;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.MetaObjectNodeServerSearch;

import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class NextKassenzeichenWithoutGeomSearchStatement extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(NextKassenzeichenWithoutGeomSearchStatement.class);

    //~ Instance fields --------------------------------------------------------

    private final Integer kassenzeichennummer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenSearchStatement object.
     *
     * @param  kassenzeichennummer  DOCUMENT ME!
     */
    public NextKassenzeichenWithoutGeomSearchStatement(final Integer kassenzeichennummer) {
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

            final String sql = "SELECT kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " "
                        + "FROM kassenzeichen LEFT JOIN flurstuecke ON "
                        + "     kassenzeichen." + KassenzeichenPropertyConstants.PROP__ID
                        + " = flurstuecke.kassenzeichen "
                        + whereKassenzeichennummer
                        + "GROUP BY kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER
                        + ", flurstuecke.kassenzeichen "
                        + "HAVING count(flurstuecke.kassenzeichen) = 0 "
                        + "ORDER BY " + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " "
                        + "LIMIT 1";

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
