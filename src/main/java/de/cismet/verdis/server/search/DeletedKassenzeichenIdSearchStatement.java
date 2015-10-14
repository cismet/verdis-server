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
public class DeletedKassenzeichenIdSearchStatement extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(DeletedKassenzeichenIdSearchStatement.class);

    //~ Instance fields --------------------------------------------------------

    private final Integer kassenzeichennummer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenSearchStatement object.
     *
     * @param  kassenzeichennummer  DOCUMENT ME!
     */
    public DeletedKassenzeichenIdSearchStatement(final Integer kassenzeichennummer) {
        this.kassenzeichennummer = kassenzeichennummer; // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<Integer> performServerSearch() {
        try {
            final String sql = "SELECT cs_history.object_id FROM cs_history\n"
                        + "WHERE class_id = 11 AND object_id IN (SELECT object_id FROM cs_history WHERE class_id = 11 AND json_data ilike '{ DELETED }') AND (\n"
                        + "      cs_history.json_data LIKE '% \"kassenzeichennummer8\" : " + kassenzeichennummer
                        + ",%' OR \n"
                        + "      cs_history.json_data LIKE '% \"kassenzeichennummer8\" : " + kassenzeichennummer
                        + " %' OR \n"
                        + "      cs_history.json_data LIKE '% \"kassenzeichennummer8\" : \"" + kassenzeichennummer
                        + "\"%' \n"
                        + ") ORDER BY cs_history.valid_from DESC";

            final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);

            final ArrayList<ArrayList> result = ms.performCustomSearch(sql);

            final ArrayList<Integer> aln = new ArrayList<Integer>();
            for (final ArrayList al : result) {
                final Integer object_id = (Integer)al.get(0);
                aln.add(object_id);
            }

            return aln;
        } catch (final Exception ex) {
            LOG.error("problem during search", ex); // NOI18N

            return null;
        }
    }
}
