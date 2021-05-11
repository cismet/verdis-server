/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.verdis.server.action;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.sql.Timestamp;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.json.StacOptionsDurationJson;
import de.cismet.verdis.server.search.KassenzeichenSearchStatement;
import de.cismet.verdis.server.search.StacInfoSearchStatement;
import de.cismet.verdis.server.utils.StacUtils;
import de.cismet.verdis.server.utils.VerdisServerResources;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class CreateAStacForKassenzeichenServerAction implements MetaServiceStore,
    UserAwareServerAction,
    ServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateAStacForKassenzeichenServerAction.class);
    public static final String TASKNAME = "createAStacForKassenzeichen";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        USER, EXPIRATION, DURATION_VALUE, DURATION_UNIT, KASSENZEICHEN, KASSENZEICHEN_ID, EXPIRE_PRE_EXISTING
    }

    //~ Instance fields --------------------------------------------------------

    private User user;
    private MetaService metaService;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object object, final ServerActionParameter... params) {
        String kassenzeichen = null;
        Integer kassenzeichenId = null;
        Integer durationValue = null;
        StacOptionsDurationJson.Unit durationUnit = null;
        String userName = null;
        Timestamp expiration = null;
        Boolean expirePreExisting = null;

        try {
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    final Object value = sap.getValue();
                    if (sap.getKey().equals(Parameter.USER.toString())) {
                        userName = (String)value;
                    } else if (sap.getKey().equals(Parameter.KASSENZEICHEN.toString())) {
                        kassenzeichen = (String)value;
                    } else if (sap.getKey().equals(Parameter.KASSENZEICHEN_ID.toString())) {
                        kassenzeichenId = (value instanceof Integer) ? (Integer)value : Integer.parseInt((String)value);
                    } else if (sap.getKey().equals(Parameter.DURATION_VALUE.toString())) {
                        durationValue = (value instanceof Integer) ? (Integer)value : Integer.parseInt((String)value);
                    } else if (sap.getKey().equals(Parameter.DURATION_UNIT.toString())) {
                        durationUnit = (value instanceof StacOptionsDurationJson.Unit)
                            ? (StacOptionsDurationJson.Unit)value : StacOptionsDurationJson.Unit.valueOf((String)value);
                    } else if (sap.getKey().equals(Parameter.EXPIRATION.toString())) {
                        expiration = (value instanceof Timestamp) ? (Timestamp)value
                                                                  : new Timestamp(Long.parseLong((String)value));
                    } else if (sap.getKey().equals(Parameter.EXPIRE_PRE_EXISTING.toString())) {
                        expirePreExisting = (value instanceof Boolean) ? (Boolean)value
                                                                       : Boolean.parseBoolean((String)value);
                    }
                }
            }

            final StacOptionsDurationJson duration = ((durationUnit != null) && (durationValue != null))
                ? new StacOptionsDurationJson(durationUnit, durationValue) : null;
            if (duration != null) {
                expiration = StacUtils.createTimestampFrom(duration);
            }

            if ((kassenzeichenId == null) && (kassenzeichen != null)) {
                final KassenzeichenSearchStatement search = new KassenzeichenSearchStatement(kassenzeichen);
                final Map localServers = new HashMap<>();
                localServers.put(VerdisConstants.DOMAIN, getMetaService());
                search.setActiveLocalServers(localServers);
                search.setUser(getUser());
                final Collection<MetaObjectNode> mons = search.performServerSearch();
                if ((mons != null) && (mons.size() == 1)) {
                    final MetaObjectNode mon = mons.iterator().next();
                    final MetaObject mo = getMetaService().getMetaObject(
                            getUser(),
                            mon.getObjectId(),
                            mon.getClassId(),
                            getConnectionContext());
                    kassenzeichenId = mo.getId();
                }
            }
            if (userName == null) {
                userName = getUser().getName();
            }

            final Properties properties = ServerResourcesLoader.getInstance()
                        .loadProperties(VerdisServerResources.CREATE_STAC_FOR_A_KASSENZEICHEN_ACTION_PROPERTIES
                            .getValue());

            final MetaClass mcKassenzeichen = getMetaService().getClassByTableName(
                    getUser(),
                    VerdisConstants.MC.KASSENZEICHEN,
                    getConnectionContext());

            final StacInfoSearchStatement search = new StacInfoSearchStatement(
                    StacInfoSearchStatement.SearchBy.KASSENZEICHEN_ID);
            search.setKassenzeichenId(kassenzeichenId);

            final Map activeLocalServers = new HashMap<>();
            activeLocalServers.put(VerdisConstants.DOMAIN, getMetaService());
            search.setActiveLocalServers(activeLocalServers);
            search.setUser(getUser());
            ((ConnectionContextStore)search).initWithConnectionContext(getConnectionContext());
            search.setActiveLocalServers(activeLocalServers);
            final Collection<Map> col = search.performServerSearch();
            if (col != null) {
                final Date now = new Date();
                for (final Map row : col) {
                    if (row != null) {
                        final Integer stacId = (Integer)row.get(StacInfoSearchStatement.Fields.ID);
                        final Timestamp expirationPreExisting = (Timestamp)row.get(
                                StacInfoSearchStatement.Fields.EXPIRATION);
                        if (now.before(expirationPreExisting)) {
                            if (Boolean.TRUE.equals(expirePreExisting)) {
                                StacUtils.updateStacExpiration(
                                    stacId,
                                    new Timestamp(now.getTime()),
                                    getMetaService(),
                                    getConnectionContext());
                            } else {
                                throw new PreExistingStacException(expirationPreExisting);
                            }
                        }
                    }
                }
            }

            return StacUtils.createStac(mcKassenzeichen.getId(),
                    kassenzeichenId,
                    properties.getProperty("baseLoginName", userName),
                    userName,
                    expiration,
                    duration);
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
    public void setMetaService(final MetaService metaService) {
        this.metaService = metaService;
    }

    @Override
    public MetaService getMetaService() {
        return metaService;
    }

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    @Override
    public String getTaskName() {
        return TASKNAME;
    }
}
