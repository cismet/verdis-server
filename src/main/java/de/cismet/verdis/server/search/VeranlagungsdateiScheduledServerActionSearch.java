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

import de.cismet.cids.server.actions.ScheduledServerActionManager;
import de.cismet.cids.server.search.AbstractCidsServerSearch;

import de.cismet.verdis.server.action.VeranlagungsdateiScheduledServerAction;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class VeranlagungsdateiScheduledServerActionSearch extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(VeranlagungsdateiScheduledServerActionSearch.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        try {
            final String key = VeranlagungsdateiScheduledServerAction.TASKNAME;
            final boolean runningOnly = true; //

            final String userString = ScheduledServerActionManager.COLUMN_USERNAME + " = '" + getUser()
                        .getName() + "'";
            final String clauseKey = (key != null) ? (ScheduledServerActionManager.COLUMN_KEY + " = '" + key + "'")
                                                   : "TRUE";
            final String clauseRunning = (runningOnly) ? (ScheduledServerActionManager.COLUMN_EXECUTION + " IS NULL")
                                                       : "TRUE";

            final String query = "SELECT "
                        + ScheduledServerActionManager.COLUMN_ID + ", "
                        + ScheduledServerActionManager.COLUMN_KEY + ", "
                        + ScheduledServerActionManager.COLUMN_TASKNAME + ", "
                        + ScheduledServerActionManager.COLUMN_USERNAME + ", "
                        + ScheduledServerActionManager.COLUMN_GROUPNAME + ", "
                        + ScheduledServerActionManager.COLUMN_BODY + ", "
                        + ScheduledServerActionManager.COLUMN_PARAMS + ", "
                        + ScheduledServerActionManager.COLUMN_START + ", "
                        + ScheduledServerActionManager.COLUMN_RULE + ", "
                        + ScheduledServerActionManager.COLUMN_EXECUTION + ", "
                        + ScheduledServerActionManager.COLUMN_ABORTED + ", "
                        + ScheduledServerActionManager.COLUMN_RESULT + " "
                        + "FROM " + ScheduledServerActionManager.SSA_TABLE + " "
                        + "WHERE "
                        + userString + " AND " + clauseKey + " AND " + clauseRunning;
            if (LOG.isDebugEnabled()) {
                LOG.debug(query);
            }
            final MetaService metaService = (MetaService)getActiveLocalServers().get("VERDIS_GRUNDIS");
            final ArrayList<ArrayList> searchResults = metaService.performCustomSearch(query);

            return searchResults;
        } catch (final Exception e) {
            LOG.fatal("problem during search", e);
            return null;
        }
    }
}
