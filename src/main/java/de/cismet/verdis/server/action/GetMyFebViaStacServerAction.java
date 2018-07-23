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

import java.util.Properties;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;

import de.cismet.verdis.server.utils.VerdisServerResources;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class GetMyFebViaStacServerAction implements MetaServiceStore,
    UserAwareServerAction,
    ServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            GetMyFebViaStacServerAction.class);

    public static final String TASKNAME = "getMyFEB";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        BODY, RETURN, STAC
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Body {

        //~ Enum constants -----------------------------------------------------

        STRING, BYTE_ARRAY
    }
    
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

    @Override
    public Object execute(final Object object, final ServerActionParameter... params) {
        Body bodyType = Body.BYTE_ARRAY;
        String stac = "";
        try {
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    if (sap.getKey().equals(Parameter.BODY.toString())) {
                        bodyType = Body.valueOf((String)sap.getValue());
                    } else if (sap.getKey().equals(Parameter.STAC.toString())) {
                        stac = (String)sap.getValue();
                    }
                }
            }

            if (stac == null && object == null) {
                throw new Exception("no stac given");
            } else if (bodyType == null) {
                throw new Exception("body-type parameter is null");
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("object=" + object);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("body=" + bodyType);
                }
                switch (bodyType) {
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

            if (LOG.isDebugEnabled()) {
                LOG.debug("STAC=" + stac);
            }
            if (stac == null) {
                throw new Exception("STAC is null");
            } else {
                return createReport(stac);
            }
        } catch (final Exception ex) {
            LOG.error("Error during GetMyFebViaStacServerAction.execute()", ex);
        }
        return "{\"nothing\":\"at all\"}";
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stac  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private byte[] createReport(final String stac) throws Exception {
        final CidsBean kassenzeichenBean = StacUtils.getKassenzeichenBean(
                stac,
                getMetaService(),
                getConnectionContext());

        final Properties properties = ServerResourcesLoader.getInstance()
                    .loadProperties(VerdisServerResources.GET_MY_FEB_VIA_STAC_ACTION_PROPERTIES.getValue());

        final Integer kassenzeichenNummer = (Integer)kassenzeichenBean.getProperty(
                KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER);
        final EBReportServerAction.Type type = (properties.getProperty("type") != null)
            ? EBReportServerAction.Type.valueOf(properties.getProperty("type")) : null;
        final EBReportServerAction.MapFormat mapFormat = (properties.getProperty("mapFormat") != null)
            ? EBReportServerAction.MapFormat.valueOf(properties.getProperty("mapFormat")) : null;
        final String hints = properties.getProperty("hints");
        final Double mapScale = (properties.getProperty("mapScale") != null)
            ? Double.valueOf(properties.getProperty("mapScale")) : null;
        final Boolean abflusswirksamkeit = (properties.getProperty("abflusswirksamkeit") != null)
            ? Boolean.valueOf(properties.getProperty("abflusswirksamkeit")) : null;

        return EBReportServerAction.createReport(
                kassenzeichenNummer,
                type,
                mapFormat,
                hints,
                mapScale,
                abflusswirksamkeit);
    }

    @Override
    public String getTaskName() {
        return TASKNAME;
    }
}
