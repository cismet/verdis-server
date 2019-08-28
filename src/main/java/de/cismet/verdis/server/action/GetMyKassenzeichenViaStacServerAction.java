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

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.utils.StacUtils;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class GetMyKassenzeichenViaStacServerAction implements MetaServiceStore, ServerAction, ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            GetMyKassenzeichenViaStacServerAction.class);

    public static final String TASKNAME = "getMyKassenzeichen";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------

        STAC // Short Time Authorization Code
    }

    //~ Instance fields --------------------------------------------------------

    private MetaService metaService;
    private ConnectionContext connectionContext;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
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
    public Object execute(final Object o, final ServerActionParameter... saps) {
        String stac = null;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("GetMyKassenzeichenViaStacServerAction");
            }
            if (saps != null) {
                for (final ServerActionParameter sap : saps) {
                    if (sap.getKey().equals(PARAMETER_TYPE.STAC.toString())) {
                        stac = (String)sap.getValue();
                        break;
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("STAC=" + stac);
                }

                final StacUtils.StacEntry stacEntry = StacUtils.getStacEntry(
                        stac,
                        getMetaService(),
                        getConnectionContext());
                final CidsBean kassenzeichenBean = StacUtils.getKassenzeichenBean(
                        stacEntry,
                        getMetaService(),
                        getConnectionContext());
                final CidsBean aenderungsanfrageBean = StacUtils.getAenderungsanfrageBean(
                        stacEntry,
                        getMetaService(),
                        getConnectionContext());

                kassenzeichenBean.setProperty(VerdisConstants.PROP.KASSENZEICHEN.STAC_OPTION, stacEntry.getOptions());
                kassenzeichenBean.setProperty(
                    VerdisConstants.PROP.KASSENZEICHEN.STAC_EXPIRATION,
                    stacEntry.getExpiration());
                kassenzeichenBean.setProperty(
                    VerdisConstants.PROP.KASSENZEICHEN.AENDERUNGSANFRAGE,
                    (aenderungsanfrageBean != null)
                        ? StacUtils.asMap(
                            (String)aenderungsanfrageBean.getProperty(
                                VerdisConstants.PROP.AENDERUNGSANFRAGE.CHANGES_JSON)) : null);
                return kassenzeichenBean.toJSONString(false);
            }
        } catch (final Exception ex) {
            LOG.error("Error during GetMyKassenzeichenViaStacServerAction.execute()", ex);
        }
        return "{\"nothing\":\"at all\"}";
    }

    @Override
    public String getTaskName() {
        return TASKNAME;
    }
}
