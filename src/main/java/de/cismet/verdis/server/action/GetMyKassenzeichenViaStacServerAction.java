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

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.json.AenderungsanfrageJson;
import de.cismet.verdis.server.json.ContactInfoJson;
import de.cismet.verdis.server.utils.AenderungsanfrageUtils;
import de.cismet.verdis.server.utils.StacEntry;
import de.cismet.verdis.server.utils.StacUtils;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class GetMyKassenzeichenViaStacServerAction implements MetaServiceStore,
    UserAwareServerAction,
    ServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            GetMyKassenzeichenViaStacServerAction.class);

    public static final String TASKNAME = "getMyKassenzeichen";
    private static final int MAX_CONCURRENT_RUNS = 4;
    private static volatile int currentRuns = 0;

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
    private User user;

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
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    public Object execute(final Object o, final ServerActionParameter... saps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMyKassenzeichen increaseCurrentRuns: existing runs: " + currentRuns);
        }

        try {
            increaseCurrentRuns();

            while (!canRunStart()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // nothing to do
                }
            }

            final long startTime = System.currentTimeMillis();
            String stac = null;

            if (LOG.isDebugEnabled()) {
                LOG.debug("GetMyKassenzeichenViaStacServerAction Run: " + currentRuns);
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

                final StacEntry stacEntry = StacUtils.getStacEntry(
                        stac,
                        getMetaService(),
                        getConnectionContext());
                final CidsBean kassenzeichenBean = StacUtils.getKassenzeichenBean(
                        stacEntry,
                        getMetaService(),
                        getConnectionContext());
                final CidsBean aenderungsanfrageBean = AenderungsanfrageUtils.getInstance()
                            .getAenderungsanfrageBean(stacEntry, getMetaService(), getConnectionContext());

                final String aenderungsanfrageJson = (aenderungsanfrageBean != null)
                    ? (String)aenderungsanfrageBean.getProperty(VerdisConstants.PROP.AENDERUNGSANFRAGE.CHANGES_JSON)
                    : null;
                final AenderungsanfrageJson aenderungsanfrage = (aenderungsanfrageJson != null)
                    ? AenderungsanfrageUtils.getInstance().createAenderungsanfrageJson(aenderungsanfrageJson) : null;

                final AenderungsanfrageJson anderungsanfrageFiltered = AenderungsanfrageUtils.getInstance()
                            .doFilteringOutWhatIShouldntSee(aenderungsanfrage, "stac".equals(getUser().getName()));

                final String clerkUsername = (aenderungsanfrageBean != null)
                    ? (String)aenderungsanfrageBean.getProperty(VerdisConstants.PROP.AENDERUNGSANFRAGE.CLERK_USERNAME)
                    : (((stacEntry != null) && (stacEntry.getStacOptions() != null))
                        ? stacEntry.getStacOptions().getCreatorUserName() : null);
                final ContactInfoJson contactInfo = (clerkUsername != null)
                    ? AenderungsanfrageUtils.getInstance()
                            .getContactInfo(
                                    clerkUsername,
                                    AenderungsanfrageUtils.getConfFromServerResource().getSachbearbeiterDefaultname())
                    : null;

                kassenzeichenBean.setProperty(
                    VerdisConstants.PROP.KASSENZEICHEN.STAC_OPTIONS,
                    stacEntry.getStacOptions());
                kassenzeichenBean.setProperty(
                    VerdisConstants.PROP.KASSENZEICHEN.STAC_EXPIRATION,
                    stacEntry.getExpiration());
                kassenzeichenBean.setProperty(
                    VerdisConstants.PROP.KASSENZEICHEN.AENDERUNGSANFRAGE,
                    (anderungsanfrageFiltered != null) ? StacUtils.asMap(anderungsanfrageFiltered.toJson()) : null);
                kassenzeichenBean.setProperty(
                    VerdisConstants.PROP.KASSENZEICHEN.CONTACTINFO,
                    (contactInfo != null) ? contactInfo : null);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getMyKassenzeichen runtime: " + (System.currentTimeMillis() - startTime));
                }
                return kassenzeichenBean.toJSONString(false);
            }
        } catch (final Exception ex) {
            LOG.error("Error during GetMyKassenzeichenViaStacServerAction.execute()", ex);
        } finally {
            decreaseCurrentRuns();
        }

        return "{\"nothing\":\"at all\"}";
    }

    /**
     * DOCUMENT ME!
     */
    private synchronized void increaseCurrentRuns() {
        ++currentRuns;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private synchronized boolean canRunStart() {
        return currentRuns <= MAX_CONCURRENT_RUNS;
    }

    /**
     * DOCUMENT ME!
     */
    private synchronized void decreaseCurrentRuns() {
        --currentRuns;

        if (currentRuns < 0) {
            currentRuns = 0;
        }
    }

    @Override
    public String getTaskName() {
        return TASKNAME;
    }
}
