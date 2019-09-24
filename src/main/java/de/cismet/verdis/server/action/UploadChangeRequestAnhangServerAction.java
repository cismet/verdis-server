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
import Sirius.server.newuser.User;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

import java.util.UUID;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.verdis.server.utils.aenderungsanfrage.NachrichtAnhangJson;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class UploadChangeRequestAnhangServerAction implements MetaServiceStore,
    UserAwareServerAction,
    ServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(UploadChangeRequestAnhangServerAction.class);
    public static final String TASKNAME = "uploadChangeRequestAnhang";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        FILENAME {

            @Override
            public String toString() {
                return "fileName";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    private User user;
    private MetaService metaService;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenChangeRequestServerAction object.
     */
    public UploadChangeRequestAnhangServerAction() {
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        } catch (final Throwable t) {
            LOG.fatal("this should never happen", t);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        final Byte[] bytes;
        String fileName = null;

        try {
//            if (body == null) {
//                throw new Exception("body can't be null");
//            } else {
//                bytes = (Byte[])body;
//            }

            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    final String key = sap.getKey();
                    final Object value = sap.getValue();
                    if (Parameter.FILENAME.toString().equals(key)) {
                        fileName = (String)value;
                    }
                }
            }
            if (fileName == null) {
                throw new Exception(Parameter.FILENAME.toString() + " parameter is missing");
            }

            final String uuid = UUID.randomUUID().toString();
            return objectMapper.writeValueAsString(new NachrichtAnhangJson(fileName, uuid));
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
