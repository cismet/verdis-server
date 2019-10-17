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
import java.util.HashMap;
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

    private static final transient Logger LOG = Logger.getLogger(StacInfoSearchStatement.class);
    private static final String FIELD_KASSENZEICHENID = "kassenzeichenid";
    private static final String QUERY_TEMPLATE_KASSENZEICHENID =
        "SELECT base_login_name, expiration, stac_options FROM cs_stac WHERE stac_options ilike '%%\""
                + FIELD_KASSENZEICHENID
                + "\":%d%%'";
    private static final String QUERY_TEMPLATE_STACID =
        "SELECT base_login_name, expiration, stac_options FROM cs_stac WHERE id = %d";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum SearchBy {

        //~ Enum constants -----------------------------------------------------

        STAC_ID, KASSENZEICHEN_ID
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Fields {

        //~ Enum constants -----------------------------------------------------

        BASE_LOGIN_NAME, TIMESTAMP, STAC_OPTIONS_JSON
    }

    //~ Instance fields --------------------------------------------------------

    private final SearchBy searchBy;
    private Integer kassenzeichenId = null;
    private Integer stacId = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StacInfoSearchStatement object.
     *
     * @param  searchBy  DOCUMENT ME!
     */
    public StacInfoSearchStatement(final SearchBy searchBy) {
        this.searchBy = searchBy;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  kassenzeichenId  DOCUMENT ME!
     */
    public void setKassenzeichenId(final Integer kassenzeichenId) {
        this.kassenzeichenId = kassenzeichenId;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  stacId  DOCUMENT ME!
     */
    public void setStacId(final Integer stacId) {
        this.stacId = stacId;
    }

    @Override
    public Collection<Map> performServerSearch() {
        try {
            final String sql = SearchBy.KASSENZEICHEN_ID.equals(searchBy)
                ? String.format(QUERY_TEMPLATE_KASSENZEICHENID, kassenzeichenId)
                : String.format(QUERY_TEMPLATE_STACID, stacId);
            final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);
            final ArrayList<ArrayList> result = ms.performCustomSearch(sql, ConnectionContext.createDummy());

            final ArrayList<Map> aln = new ArrayList<>();
            for (final ArrayList al : result) {
                final String baseLoginName = (String)al.get(0);
                final Timestamp timestamp = (Timestamp)al.get(1);
                final String stacOptions = (String)al.get(2);

                final Map<String, Object> stacOptionsMap = OBJECT_MAPPER.readValue(
                        stacOptions,
                        new TypeReference<Map<String, Object>>() {
                        });
                if (!SearchBy.KASSENZEICHEN_ID.equals(searchBy)
                            || (stacOptionsMap.containsKey(FIELD_KASSENZEICHENID)
                                && kassenzeichenId.equals(stacOptionsMap.get(FIELD_KASSENZEICHENID)))) {
                    final Map objectArray = new HashMap<>();
                    objectArray.put(Fields.BASE_LOGIN_NAME, baseLoginName);
                    objectArray.put(Fields.TIMESTAMP, timestamp);
                    objectArray.put(Fields.STAC_OPTIONS_JSON, stacOptionsMap);
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
