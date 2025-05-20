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

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apache.log4j.Logger;

import java.io.Serializable;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;
import de.cismet.cids.server.messages.CidsServerMessageManagerImpl;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.json.AenderungsanfrageJson;
import de.cismet.verdis.server.json.AenderungsanfrageResultJson;
import de.cismet.verdis.server.json.NachrichtJson;
import de.cismet.verdis.server.json.NachrichtParameterJson;
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
    public static final String CSM_NEWREQUEST = "newChangerequest";

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
        STAC_ID {

            @Override
            public String toString() {
                return "stacId";
            }
        },
        CHANGEREQUEST_JSON {

            @Override
            public String toString() {
                return "changerequestJson";
            }
        },
        CLERK_IS_SAVING {

            @Override
            public String toString() {
                return "Sachbearbeiter speichert Kassenzeichen";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    private User user;
    private MetaService metaService;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Map<Parameter, Object> extractParams(final ServerActionParameter[] params) throws Exception {
        final Map<Parameter, Object> extractedParams = new HashMap<>();
        if (params != null) {
            for (final ServerActionParameter sap : params) {
                final String key = sap.getKey();
                final Object value = sap.getValue();
                if (Parameter.STAC.toString().equals(key)) {
                    extractedParams.put(Parameter.STAC, (String)value);
                } else if (Parameter.STAC_ID.toString().equals(key)) {
                    extractedParams.put(Parameter.STAC_ID, (Integer)value);
                } else if (Parameter.CHANGEREQUEST_JSON.toString().equals(key)) {
                    extractedParams.put(Parameter.CHANGEREQUEST_JSON, value);
                } else if (Parameter.CLERK_IS_SAVING.toString().equals(key)) {
                    extractedParams.put(Parameter.CLERK_IS_SAVING, value);
                }
            }
        }
        return extractedParams;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   extractedParams  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static void preValidateInput(final Map<Parameter, Object> extractedParams) throws Exception {
        final String stac = (String)extractedParams.get(Parameter.STAC);
        final Integer stacId = (Integer)extractedParams.get(Parameter.STAC_ID);
        final Object aenderungsanfrage = extractedParams.get(Parameter.CHANGEREQUEST_JSON);

        if ((stac == null) && (stacId == null)) {
            LOG.info("stac is null");
            throw new Exception("stac and stacId is null");
        }

        if (aenderungsanfrage == null) {
            LOG.info("aenderungsanfrage is null, returning false");
            throw new Exception("aenderungsanfrage is null");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stac    DOCUMENT ME!
     * @param   stacId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private StacEntry createStacEntry(final String stac, final Integer stacId) throws Exception {
        final StacEntry stacEntry = (stac != null)
            ? StacUtils.getStacEntry(
                stac,
                getMetaService(),
                getConnectionContext()) : StacUtils.getStacEntry(
                stacId,
                getMetaService(),
                getConnectionContext());
        if (stacEntry == null) {
            LOG.info("stacEntry not found, returning false");
            throw new Exception("stacEntry not found");
        }
        return stacEntry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stacEntry  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private CidsBean createKassenzeichenBean(final StacEntry stacEntry) throws Exception {
        final CidsBean kassenzeichenBean = StacUtils.getKassenzeichenBean(
                stacEntry,
                getMetaService(),
                getConnectionContext());
        if (kassenzeichenBean == null) {
            LOG.info("kassenzeichen for stacEntry not found, returning false");
            throw new Exception("kassenzeichen not found");
        }
        return kassenzeichenBean;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aenderungsanfrage  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private AenderungsanfrageJson createAenderungsanfrageNew(final Object aenderungsanfrage) throws Exception {
        final AenderungsanfrageJson aenderungsanfrageJson;
        if (aenderungsanfrage instanceof AenderungsanfrageJson) {
            aenderungsanfrageJson = (AenderungsanfrageJson)aenderungsanfrage;
        } else if (aenderungsanfrage instanceof String) {
            aenderungsanfrageJson = AenderungsanfrageUtils.getInstance()
                        .createAenderungsanfrageJson((String)aenderungsanfrage);
        } else if (aenderungsanfrage instanceof Map) {
            aenderungsanfrageJson = AenderungsanfrageUtils.getInstance()
                        .createAenderungsanfrageJson((Map<String, Object>)aenderungsanfrage);
        } else {
            throw new Exception("aenderungsanfrage in wrong format");
        }
        return aenderungsanfrageJson;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichenBean      DOCUMENT ME!
     * @param   aenderungsanfrageJson  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Integer createKassenzeichennummer(final CidsBean kassenzeichenBean,
            final AenderungsanfrageJson aenderungsanfrageJson) throws Exception {
        final Integer kassenzeichenNummerFromBean = (Integer)kassenzeichenBean.getProperty(
                VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER);
        final Integer kassenzeichenNummerFromObject = aenderungsanfrageJson.getKassenzeichen();
        if (!Objects.equals(kassenzeichenNummerFromBean, kassenzeichenNummerFromObject)) {
            LOG.info(String.format(
                    "kassenzeichennummer from json (%d) not equals kassenzeichennummer from bean (%d), returning false",
                    kassenzeichenNummerFromBean,
                    kassenzeichenNummerFromObject));
            throw new Exception("kassenzeichen of stac is not corresponding to the requested change");
        }
        return kassenzeichenNummerFromBean;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aenderungsanfrageBean  DOCUMENT ME!
     * @param   kassenzeichennummer    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private AenderungsanfrageJson createAenderungsanfrageOrig(final CidsBean aenderungsanfrageBean,
            final Integer kassenzeichennummer) throws Exception {
        final String aenderungsanfrageOrigJson = (String)aenderungsanfrageBean.getProperty(
                VerdisConstants.PROP.AENDERUNGSANFRAGE.CHANGES_JSON);
        final AenderungsanfrageJson aenderungsanfrageOrig = (aenderungsanfrageOrigJson != null)
            ? AenderungsanfrageUtils.getInstance().createAenderungsanfrageJson(aenderungsanfrageOrigJson)
            : new AenderungsanfrageJson(kassenzeichennummer);
        return aenderungsanfrageOrig;
    }

    @Override
    public Object execute(final Object boxy, final ServerActionParameter... params) {
        try {
            final Map<Parameter, Object> extractedParams = extractParams(params);
            preValidateInput(extractedParams);

            final String userName = getUser().getName();
            final boolean citizenOrClerk = "stac".equals(userName);

            final String stac = (String)extractedParams.get(Parameter.STAC);
            final Integer stacId = (Integer)extractedParams.get(Parameter.STAC_ID);
            final Object aenderungsanfrage = extractedParams.get(Parameter.CHANGEREQUEST_JSON);
            final Boolean veranlagt = (!citizenOrClerk) ? (Boolean)extractedParams.get(Parameter.CLERK_IS_SAVING)
                                                        : null;

            final StacEntry stacEntry = createStacEntry(stac, stacId);
            final AenderungsanfrageJson aenderungsanfrageJson = createAenderungsanfrageNew(aenderungsanfrage);
            final CidsBean kassenzeichenBean = createKassenzeichenBean(stacEntry);
            final Integer kassenzeichennummer = createKassenzeichennummer(kassenzeichenBean, aenderungsanfrageJson);
            final Boolean submission = aenderungsanfrageJson.getSubmission();

            removeDoublesFromAenderungsanfrage(aenderungsanfrageJson);

            final Map<String, CidsBean> existingFlaechen = new HashMap<>();
            for (final CidsBean flaecheBean
                        : kassenzeichenBean.getBeanCollectionProperty(VerdisConstants.PROP.KASSENZEICHEN.FLAECHEN)) {
                final String flaechenBezeichnung = (String)flaecheBean.getProperty(
                        VerdisConstants.PROP.FLAECHE.FLAECHENBEZEICHNUNG);
                if (flaechenBezeichnung != null) {
                    existingFlaechen.put(flaechenBezeichnung.toUpperCase(), flaecheBean);
                }
            }
            synchronized (this) {
                final Date now = new Date();

                final CidsBean existingAenderungsanfrageBean = AenderungsanfrageUtils.getInstance()
                            .getAenderungsanfrageBean(
                                stacEntry,
                                getMetaService(),
                                getConnectionContext());

                final CidsBean aenderungsanfrageBean;
                if (existingAenderungsanfrageBean != null) {
                    aenderungsanfrageBean = existingAenderungsanfrageBean;
                } else {
                    aenderungsanfrageBean = CidsBean.createNewCidsBeanFromTableName(
                            VerdisConstants.DOMAIN,
                            VerdisConstants.MC.AENDERUNGSANFRAGE,
                            getConnectionContext());
                    aenderungsanfrageBean.setProperty(
                        VerdisConstants.PROP.AENDERUNGSANFRAGE.STATUS,
                        AenderungsanfrageUtils.getInstance().getStatusBean(
                            AenderungsanfrageUtils.Status.NONE,
                            stacEntry,
                            getMetaService(),
                            getConnectionContext()));
                    aenderungsanfrageBean.setProperty(
                        VerdisConstants.PROP.AENDERUNGSANFRAGE.CLERK_USERNAME,
                        stacEntry.getStacOptions().getCreatorUserName());
                }

                final String statusSchluessel =
                    (aenderungsanfrageBean.getProperty(VerdisConstants.PROP.AENDERUNGSANFRAGE.STATUS) != null)
                    ? (String)aenderungsanfrageBean.getProperty(
                        VerdisConstants.PROP.AENDERUNGSANFRAGE.STATUS
                                + ".schluessel") : null;

                // PROCESSING
                if (AenderungsanfrageUtils.Status.CLOSED.toString().equals(statusSchluessel)) {
                    throw new Exception("status is CLOSED");
                }

                final AenderungsanfrageJson aenderungsanfrageOrig = createAenderungsanfrageOrig(
                        aenderungsanfrageBean,
                        kassenzeichennummer);

                final AenderungsanfrageJson aenderungsanfrageProcessed = AenderungsanfrageUtils.getInstance()
                            .doProcessing(
                                stacEntry,
                                kassenzeichennummer,
                                existingFlaechen,
                                aenderungsanfrageOrig,
                                aenderungsanfrageJson,
                                citizenOrClerk,
                                veranlagt,
                                userName,
                                now,
                                getMetaService(),
                                getConnectionContext());

                // STATUS
                final AenderungsanfrageUtils.Status oldStatus = (statusSchluessel != null)
                    ? AenderungsanfrageUtils.Status.valueOf(statusSchluessel) : null;
                final AenderungsanfrageUtils.Status status = AenderungsanfrageUtils.getInstance()
                            .identifyNewStatus(
                                oldStatus,
                                existingFlaechen,
                                aenderungsanfrageOrig,
                                aenderungsanfrageProcessed,
                                citizenOrClerk,
                                veranlagt,
                                userName,
                                now);
                AenderungsanfrageUtils.getInstance()
                        .addStatusChangedSystemMessage(oldStatus, status, aenderungsanfrageProcessed, now, userName);
                final AenderungsanfrageUtils.Status newStatus = (status != null)
                    ? status : ((oldStatus != null) ? oldStatus : AenderungsanfrageUtils.Status.PENDING);

                if (!((AenderungsanfrageUtils.Status.ARCHIVED == oldStatus)
                                && (AenderungsanfrageUtils.Status.ARCHIVED == newStatus))) {
                    // PERSISTING
                    persistAenderungsanfrage(
                        aenderungsanfrageBean,
                        stacEntry,
                        aenderungsanfrageProcessed,
                        kassenzeichennummer,
                        newStatus,
                        existingAenderungsanfrageBean
                                != null);

                    if (!Objects.equals(oldStatus, newStatus)) {
                        StacUtils.prolongExpiration(stacEntry, getMetaService(), getConnectionContext());
                        AenderungsanfrageUtils.getInstance()
                                .sendStatusChangedMail(aenderungsanfrageProcessed, newStatus);
                    }
                }

                if (Boolean.TRUE.equals(submission)) {
                    AenderungsanfrageUtils.getInstance().sendSubmissionMail(aenderungsanfrageProcessed);
                }

                final AenderungsanfrageJson anderungsanfrageFilteredForClerk = AenderungsanfrageUtils.getInstance()
                            .doFilteringOutWhatIShouldntSee(aenderungsanfrageProcessed, false);

                CidsServerMessageManagerImpl.getInstance()
                        .publishMessage(
                            CSM_NEWREQUEST,
                            new ServerMessage(
                                (Integer)aenderungsanfrageBean.getProperty(
                                    VerdisConstants.PROP.AENDERUNGSANFRAGE.STAC_ID),
                                (anderungsanfrageFilteredForClerk != null) ? anderungsanfrageFilteredForClerk.toJson()
                                                                           : null,
                                newStatus),
                            false,
                            getConnectionContext());

                final AenderungsanfrageJson anderungsanfrageFiltered = AenderungsanfrageUtils.getInstance()
                            .doFilteringOutWhatIShouldntSee(
                                aenderungsanfrageProcessed,
                                citizenOrClerk);

                // RESULT
                return new AenderungsanfrageResultJson(
                        AenderungsanfrageResultJson.ResultStatus.SUCCESS,
                        anderungsanfrageFiltered,
                        null).toJson();
            }
        } catch (final Exception ex) {
            LOG.info(ex, ex);
            try {
                return new AenderungsanfrageResultJson(
                        AenderungsanfrageResultJson.ResultStatus.ERROR,
                        null,
                        ex.getMessage()).toJson();
            } catch (final Exception ex1) {
                LOG.error(ex1, ex1);
                return null;
            }
        }
    }

    /**
     * Sometimes, messages of the type seen will be double. This method removes doubled messages.
     *
     * @param  aenderungsanfrage  DOCUMENT ME!
     */
    private void removeDoublesFromAenderungsanfrage(final AenderungsanfrageJson aenderungsanfrage) {
        final List<NachrichtJson> nachrichten = aenderungsanfrage.getNachrichten();
        NachrichtJson lastMessage = nachrichten.get(nachrichten.size() - 1);

        for (int i = (nachrichten.size() - 2); i > 0; --i) {
            final NachrichtJson currentMessage = nachrichten.get(i);
            final NachrichtParameterJson.Type currentType = ((currentMessage.getNachrichtenParameter() != null)
                    ? currentMessage.getNachrichtenParameter().getType() : null);
            final NachrichtParameterJson.Type lastType = ((lastMessage.getNachrichtenParameter() != null)
                    ? lastMessage.getNachrichtenParameter().getType() : null);

            if ((currentType != null) && (lastType != null) && currentType.equals(NachrichtParameterJson.Type.SEEN)
                        && lastType.equals(NachrichtParameterJson.Type.SEEN) && currentMessage.equals(lastMessage)) {
                nachrichten.remove(i);
            } else {
                lastMessage = currentMessage;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aenderungsanfrageBean           DOCUMENT ME!
     * @param   stacEntry                       DOCUMENT ME!
     * @param   aenderungsanfrageProcessed      DOCUMENT ME!
     * @param   kassenzeichennumer              DOCUMENT ME!
     * @param   status                          DOCUMENT ME!
     * @param   aenderungsanfrageAlreadyExists  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void persistAenderungsanfrage(
            final CidsBean aenderungsanfrageBean,
            final StacEntry stacEntry,
            final AenderungsanfrageJson aenderungsanfrageProcessed,
            final Integer kassenzeichennumer,
            final AenderungsanfrageUtils.Status status,
            final boolean aenderungsanfrageAlreadyExists) throws Exception {
        removeDoublesFromAenderungsanfrage(aenderungsanfrageProcessed);

        aenderungsanfrageBean.setProperty(
            VerdisConstants.PROP.AENDERUNGSANFRAGE.CHANGES_JSON,
            aenderungsanfrageProcessed.toJson());
        aenderungsanfrageBean.setProperty(VerdisConstants.PROP.AENDERUNGSANFRAGE.STAC_ID, stacEntry.getId());
        aenderungsanfrageBean.setProperty(
            VerdisConstants.PROP.AENDERUNGSANFRAGE.KASSENZEICHEN_NUMMER,
            kassenzeichennumer);
        aenderungsanfrageBean.setProperty(
            VerdisConstants.PROP.AENDERUNGSANFRAGE.TIMESTAMP,
            new Timestamp(new Date().getTime()));
        aenderungsanfrageBean.setProperty(
            VerdisConstants.PROP.AENDERUNGSANFRAGE.STATUS,
            AenderungsanfrageUtils.getStatusBean(
                status,
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @AllArgsConstructor
    @Getter
    public static class ServerMessage implements Serializable {

        //~ Instance fields ----------------------------------------------------

        private final Integer stacId;
        private final String aenderungsanfrage;
        private final AenderungsanfrageUtils.Status status;
    }
}
