/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import de.cismet.cids.server.search.AbstractCidsServerSearch;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.verdis.commons.constants.VerdisConstants;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class StacInfoSearchStatement extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(StacInfoSearchStatement.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String FIELD_KASSENZEICHENID = "kassenzeichenid";

    //~ Instance fields --------------------------------------------------------

    private final Integer kassenzeichenId;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenSearchStatement object.
     *
     * @param  kassenzeichenId  DOCUMENT ME!
     */
    public StacInfoSearchStatement(final Integer kassenzeichenId) {
        this.kassenzeichenId = kassenzeichenId; // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<Object[]> performServerSearch() {
        try {
            final String sql =
                "SELECT base_login_name, expiration, stac_options FROM cs_stac WHERE stac_options ilike '%\""
                        + FIELD_KASSENZEICHENID
                        + "\":"
                        + kassenzeichenId
                        + "%'";
            final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);
            final ArrayList<ArrayList> result = ms.performCustomSearch(sql, ConnectionContext.createDummy());

            final ArrayList<Object[]> aln = new ArrayList<>();
            for (final ArrayList al : result) {
                final String baseLoginName = (String)al.get(0);
                final Timestamp timestamp = (Timestamp)al.get(1);
                final String stacOptions = (String)al.get(2);

                final Map<String, Object> stacOptionsMap = OBJECT_MAPPER.readValue(
                        stacOptions,
                        new TypeReference<Map<String, Object>>() {
                        });
                if (stacOptionsMap.containsKey(FIELD_KASSENZEICHENID)
                            && kassenzeichenId.equals(stacOptionsMap.get(FIELD_KASSENZEICHENID))) {
                    final Object[] objectArray = new Object[] {
                            baseLoginName,
                            timestamp,
                            stacOptionsMap
                        };
                    aln.add(objectArray);
                }
            }

            return aln;
        } catch (final Exception ex) {
            LOG.error("problem during search", ex); // NOI18N

            return null;
        }
    }
}
