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
import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;

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
public static void main(String[] args) {
        System.out.println(
                String.format(""
                + "SELECT cs_history.object_id FROM cs_history WHERE class_key = '%1$s' AND object_id IN ("
                + "  SELECT object_id "
                + "  FROM cs_history "
                + "  WHERE class_key = '%1$s' AND json_data ilike '{ DELETED }') AND ("
                + "    cs_history.json_data LIKE '%% \"%2$s\" : %3$d,%%' OR "
                + "    cs_history.json_data LIKE '%% \"%2$s\" : %3$d %%' OR "
                + "    cs_history.json_data LIKE '%% \"%2$s\" : \"%3$d%% "
                + ") ORDER BY cs_history.valid_from DESC;", 
                VerdisMetaClassConstants.KASSENZEICHEN, 
                VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER, 
                60004629)
        );
    }
    @Override
    public Collection<Integer> performServerSearch() {
        try {
            final String sql = String.format(""
                + "SELECT cs_history.object_id FROM cs_history WHERE class_key = '%1$s' AND object_id IN ("
                + "  SELECT object_id "
                + "  FROM cs_history "
                + "  WHERE class_key = '%1$s' AND json_data ilike '{ DELETED }') AND ("
                + "    cs_history.json_data LIKE '%% \"%2$s\" : %3$d,%%' OR "
                + "    cs_history.json_data LIKE '%% \"%2$s\" : %3$d %%' OR "
                + "    cs_history.json_data LIKE '%% \"%2$s\" : \"%3$d%% "
                + ") ORDER BY cs_history.valid_from DESC;", VerdisMetaClassConstants.KASSENZEICHEN, VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER, kassenzeichennummer);

            final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);

            final ArrayList<ArrayList> result = ms.performCustomSearch(sql);

            final ArrayList<Integer> aln = new ArrayList<>();
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
