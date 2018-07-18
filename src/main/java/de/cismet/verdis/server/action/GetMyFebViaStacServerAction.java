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

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;
import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class GetMyFebViaStacServerAction implements MetaServiceStore, UserAwareServerAction, ServerAction, ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            GetMyFebViaStacServerAction.class);

    //~ Instance fields --------------------------------------------------------


    private User user;

    private MetaService metaService;

    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

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

    /**
     * DOCUMENT ME!
     *
     * @version $Revision$, $Date$
     */
    public enum Parameter {

        BODY
    }
    
    public enum Body {

        STRING, BYTE_ARRAY
    }
    
    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object object, final ServerActionParameter... params) {
        Body body = Body.BYTE_ARRAY;
        try {
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    if (sap.getKey().equals(Parameter.BODY.toString())) {
                        body = Body.valueOf((String)sap.getValue());
                    }
                }
            }

            final String stac;
            if (object == null) {
                throw new Exception("body is null");
            } else if (body == null) {
                throw new Exception("body-type parameter is null");
            } else {
                switch (body) {
                    case BYTE_ARRAY: {
                        stac = new String((byte[])object);
                    }
                    break;
                    case STRING: {
                        stac = (String)object;
                    }
                    break;
                    default: {
                        stac = null;
                    }
                }
            }
            
            if (stac == null) {
                throw new Exception("STAC is null");
            } else {
                return createReport(stac);
            }
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            return ex;
        }   
    }
    
    private byte[] createReport(final String stac) throws Exception {
        return null;
    }

    @Override
    public String getTaskName() {
        return "getMyFEB";
    }
}
