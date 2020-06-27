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

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.sql.Timestamp;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.json.AenderungsanfrageJson;
import de.cismet.verdis.server.json.AenderungsanfrageResultJson;
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
public class KassenzeichenChangeRequestServerAction implements MetaServiceStore,
    UserAwareServerAction,
    ServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(KassenzeichenChangeRequestServerAction.class);
    public static final String TASKNAME = "kassenzeichenChangeRequest";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        STAC {

            @Override
            public String toString() {
                return "stac";
            }
        },
        CHANGEREQUEST_JSON {

            @Override
            public String toString() {
                return "changerequestJson";
            }
        },
        EMAIL {

            @Override
            public String toString() {
                return "email";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    private User user;
    private MetaService metaService;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object boxy, final ServerActionParameter... params) {
        String stac = null;
        String email = null;
        Map<String, Object> aenderungsanfrageMap = null;

        try {
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    final String key = sap.getKey();
                    final Object value = sap.getValue();
                    if (Parameter.STAC.toString().equals(key)) {
                        stac = (String)value;
                    } else if (Parameter.CHANGEREQUEST_JSON.toString().equals(key)) {
                        aenderungsanfrageMap = (Map<String, Object>)value;
                    } else if (Parameter.EMAIL.toString().equals(key)) {
                        email = (String)value;
                    }
                }
            }

            if (stac == null) {
                LOG.info("stac is null, returning false");
                return new AenderungsanfrageResultJson(
                        AenderungsanfrageResultJson.ResultStatus.ERROR,
                        null,
                        "request is null").toJson();
            }
            if (aenderungsanfrageMap == null) {
                LOG.info("aenderungsanfrageMap is null, returning false");
                return new AenderungsanfrageResultJson(
                        AenderungsanfrageResultJson.ResultStatus.ERROR,
                        null,
                        "request is empty").toJson();
            }

            final StacEntry stacEntry = StacUtils.getStacEntry(
                    stac,
                    getMetaService(),
                    getConnectionContext());
            if (stacEntry == null) {
                LOG.info("stacEntry not found, returning false");
                return new AenderungsanfrageResultJson(
                        AenderungsanfrageResultJson.ResultStatus.ERROR,
                        null,
                        "stacEntry not found").toJson();
            }

            final CidsBean kassenzeichenBean = StacUtils.getKassenzeichenBean(
                    stacEntry,
                    getMetaService(),
                    getConnectionContext());
            if (kassenzeichenBean == null) {
                LOG.info("kassenzeichen for stacEntry not found, returning false");
                return new AenderungsanfrageResultJson(
                        AenderungsanfrageResultJson.ResultStatus.ERROR,
                        null,
                        "kassenzeichen not found").toJson();
            }

            final AenderungsanfrageJson aenderungsanfrageRequest = AenderungsanfrageUtils.getInstance()
                        .createAenderungsanfrageJson(
                            aenderungsanfrageMap);

            final Integer kassenzeichenNummerFromBean = (Integer)kassenzeichenBean.getProperty(
                    VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER);
            final Integer kassenzeichenNummerFromObject = aenderungsanfrageRequest.getKassenzeichen();
            if (!Objects.equals(kassenzeichenNummerFromBean, kassenzeichenNummerFromObject)) {
                LOG.info(String.format(
                        "kassenzeichennummer from json (%d) not equals kassenzeichennummer from bean (%d), returning false",
                        kassenzeichenNummerFromBean,
                        kassenzeichenNummerFromObject));
                return new AenderungsanfrageResultJson(
                        AenderungsanfrageResultJson.ResultStatus.ERROR,
                        null,
                        "kassenzeichen of stac is not corresponding to the requested change").toJson();
            }

            synchronized (this) {
                final CidsBean existingAenderungsanfrageBean = AenderungsanfrageUtils.getInstance()
                            .getAenderungsanfrageBean(
                                stacEntry,
                                getMetaService(),
                                getConnectionContext());
                final boolean aenderungsanfrageAlreadyExists = existingAenderungsanfrageBean != null;
                final CidsBean aenderungsanfrageBean = aenderungsanfrageAlreadyExists
                    ? existingAenderungsanfrageBean
                    : CidsBean.createNewCidsBeanFromTableName(
                        VerdisConstants.DOMAIN,
                        VerdisConstants.MC.AENDERUNGSANFRAGE,
                        getConnectionContext());

                final String statusSchluessel = (String)aenderungsanfrageBean.getProperty(
                        VerdisConstants.PROP.AENDERUNGSANFRAGE.STATUS
                                + ".schluessel");
                if (AenderungsanfrageUtils.Status.CLOSED.toString().equals(statusSchluessel)) {
                    // todo ein resusal json-object zur unterscheidung der ablehnung ?
                    return new AenderungsanfrageResultJson(
                            AenderungsanfrageResultJson.ResultStatus.ERROR,
                            null,
                            "CLOSED").toJson();
                }

                final String aenderungsanfrageOrigJson = (String)kassenzeichenBean.getProperty(
                        KassenzeichenPropertyConstants.AENDERUNGSANFRAGE);
                final AenderungsanfrageJson aenderungsanfrageOrig = (aenderungsanfrageOrigJson != null)
                    ? AenderungsanfrageUtils.getInstance().createAenderungsanfrageJson(aenderungsanfrageOrigJson)
                    : new AenderungsanfrageJson(kassenzeichenNummerFromBean);
                final AenderungsanfrageJson aenderungsanfrageProcessed;
                if ("stac".equals(user.getName())) {
                    aenderungsanfrageProcessed = AenderungsanfrageUtils.getInstance()
                                .processAnfrageCitizen(
                                        kassenzeichenNummerFromBean,
                                        aenderungsanfrageOrig,
                                        aenderungsanfrageRequest);
                } else {
                    aenderungsanfrageProcessed = AenderungsanfrageUtils.getInstance()
                                .processAnfrageClerk(
                                        kassenzeichenNummerFromBean,
                                        aenderungsanfrageOrig,
                                        aenderungsanfrageRequest);
                }

                aenderungsanfrageBean.setProperty(
                    VerdisConstants.PROP.AENDERUNGSANFRAGE.CHANGES_JSON,
                    aenderungsanfrageProcessed.toJson());
                aenderungsanfrageBean.setProperty(VerdisConstants.PROP.AENDERUNGSANFRAGE.STAC_ID, stacEntry.getId());
                aenderungsanfrageBean.setProperty(
                    VerdisConstants.PROP.AENDERUNGSANFRAGE.KASSENZEICHEN_NUMMER,
                    (Integer)kassenzeichenBean.getProperty(VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER));
                aenderungsanfrageBean.setProperty(VerdisConstants.PROP.AENDERUNGSANFRAGE.EMAIL, email);
                aenderungsanfrageBean.setProperty(
                    VerdisConstants.PROP.AENDERUNGSANFRAGE.TIMESTAMP,
                    new Timestamp(new Date().getTime()));
                aenderungsanfrageBean.setProperty(
                    VerdisConstants.PROP.AENDERUNGSANFRAGE.STATUS,
                    AenderungsanfrageUtils.getInstance().getStatusBean(
                        AenderungsanfrageUtils.Status.PENDING,
                        stacEntry,
                        getMetaService(),
                        getConnectionContext()));

                if (aenderungsanfrageAlreadyExists) {
                    DomainServerImpl.getServerInstance()
                            .updateMetaObject(getUser(), aenderungsanfrageBean.getMetaObject(), getConnectionContext());
                } else {
                    DomainServerImpl.getServerInstance()
                            .insertMetaObject(getUser(), aenderungsanfrageBean.getMetaObject(), getConnectionContext());
                }

                if ((stacEntry.getStacOptions() != null) && (stacEntry.getStacOptions().getDuration() != null)) {
                    StacUtils.updateStacExpiration(
                        stacEntry.getId(),
                        StacUtils.createTimestampFrom(stacEntry.getStacOptions().getDuration()),
                        getMetaService(),
                        getConnectionContext());
                }
            }
            return new AenderungsanfrageResultJson(
                    AenderungsanfrageResultJson.ResultStatus.SUCCESS,
                    aenderungsanfrageRequest,
                    null).toJson();
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
