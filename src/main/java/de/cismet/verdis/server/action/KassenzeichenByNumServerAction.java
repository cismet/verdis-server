/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.action;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.search.KassenzeichenSearchStatement;

/**
 * Checks whether the kassenzeichen with the given 8 digit number (parameter NUM) exists. Returns {@link Boolean#TRUE}
 * if it exists, otherwise {@link Boolean#FALSE}.
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class KassenzeichenByNumServerAction implements ServerAction,
    MetaServiceStore,
    UserAwareServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(KassenzeichenByNumServerAction.class);
    public static final String TASK_NAME = "KassenzeichenByNum";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        NUM
    }

    //~ Instance fields --------------------------------------------------------

    private User user;
    private MetaService metaService;

    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        String num = null;

        try {
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    if (sap.getKey().equals(Parameter.NUM.toString())) {
                        num = (sap.getValue() != null) ? sap.getValue().toString().trim() : null;
                    }
                }
            }

            if (num == null) {
                return new Exception("num can't be null");
            }
            if (!num.matches("\\d{8}")) {
                return new Exception("num must be an 8 digit number: " + num);
            }

            final KassenzeichenSearchStatement search = new KassenzeichenSearchStatement(num);
            final Map localServers = new HashMap<>();
            localServers.put(VerdisConstants.DOMAIN, getMetaService());
            search.setActiveLocalServers(localServers);
            search.setUser(getUser());

            final Collection<MetaObjectNode> mons = search.performServerSearch();

            if (mons == null) {
                return new Exception("kassenzeichen search failed for " + num);
            }

            return !mons.isEmpty();
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            return ex;
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    public MetaService getMetaService() {
        return metaService;
    }

    @Override
    public void setMetaService(final MetaService metaService) {
        this.metaService = metaService;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
