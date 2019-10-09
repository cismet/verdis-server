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
import Sirius.server.middleware.types.MetaObject;
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

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.json.aenderungsanfrage.AenderungsanfrageJson;
import de.cismet.verdis.server.utils.AenderungsanfrageUtils;
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

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Status {

        //~ Enum constants -----------------------------------------------------

        NONE, DRAFT, PENDING, PROCESSING, DONE, CLOSED
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
        String aenderungsanfrageJson = null;

        try {
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    final String key = sap.getKey();
                    final Object value = sap.getValue();
                    if (Parameter.STAC.toString().equals(key)) {
                        stac = (String)value;
                    } else if (Parameter.CHANGEREQUEST_JSON.toString().equals(key)) {
                        aenderungsanfrageJson = AenderungsanfrageUtils.getInstance().getMapper()
                                    .writeValueAsString((Map<String, Object>)value);
                    } else if (Parameter.EMAIL.toString().equals(key)) {
                        email = (String)value;
                    }
                }
            }

            if (stac == null) {
                LOG.info("stac is null, returning false");
                return false;
            }
            if (aenderungsanfrageJson == null) {
                LOG.info("aenderungsanfrageJson is null, returning false");
                return false;
            }

            final StacUtils.StacEntry stacEntry = StacUtils.getStacEntry(
                    stac,
                    getMetaService(),
                    getConnectionContext());
            if (stacEntry == null) {
                LOG.info("stacEntry not found, returning false");
                return false;
            }

            final CidsBean kassenzeichenBean = StacUtils.getKassenzeichenBean(
                    stacEntry,
                    getMetaService(),
                    getConnectionContext());
            if (kassenzeichenBean == null) {
                LOG.info("kassenzeichen for stacEntry not found, returning false");
                return false;
            }

            final AenderungsanfrageJson aenderungsanfrage = AenderungsanfrageUtils.createAenderungsanfrageJson(
                    aenderungsanfrageJson);

            final Integer kassenzeichenNummerFromBean = (Integer)kassenzeichenBean.getProperty(
                    VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER);
            final Integer kassenzeichenNummerFromObject = aenderungsanfrage.getKassenzeichen();
            if (!Objects.equals(kassenzeichenNummerFromBean, kassenzeichenNummerFromObject)) {
                LOG.info(String.format(
                        "kassenzeichennummer from json (%d) not equals kassenzeichennummer from bean (%d), returning false",
                        kassenzeichenNummerFromBean,
                        kassenzeichenNummerFromObject));
                return false;
            }

            final CidsBean existingAenderungsanfrageBean = AenderungsanfrageUtils.getAenderungsanfrageBean(
                    stacEntry,
                    getMetaService(),
                    getConnectionContext());
            final CidsBean aenderungsanfrageBean = (existingAenderungsanfrageBean != null)
                ? existingAenderungsanfrageBean
                : CidsBean.createNewCidsBeanFromTableName(
                    VerdisConstants.DOMAIN,
                    VerdisConstants.MC.AENDERUNGSANFRAGE,
                    getConnectionContext());

            aenderungsanfrageBean.setProperty(
                VerdisConstants.PROP.AENDERUNGSANFRAGE.CHANGES_JSON,
                aenderungsanfrage.toJson());
            aenderungsanfrageBean.setProperty(VerdisConstants.PROP.AENDERUNGSANFRAGE.STAC_ID, stacEntry.getId());
            aenderungsanfrageBean.setProperty(
                VerdisConstants.PROP.AENDERUNGSANFRAGE.KASSENZEICHEN_NUMMER,
                (Integer)kassenzeichenBean.getProperty(VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER));
            aenderungsanfrageBean.setProperty(VerdisConstants.PROP.AENDERUNGSANFRAGE.EMAIL, email);
            aenderungsanfrageBean.setProperty(
                VerdisConstants.PROP.AENDERUNGSANFRAGE.TIMESTAMP,
                new Timestamp(new Date().getTime()));
            aenderungsanfrageBean.setProperty(VerdisConstants.PROP.AENDERUNGSANFRAGE.STATUS, "PENDING");

            if (MetaObject.NEW == aenderungsanfrageBean.getMetaObject().getStatus()) {
                DomainServerImpl.getServerInstance()
                        .insertMetaObject(getUser(), aenderungsanfrageBean.getMetaObject(), getConnectionContext());
            } else {
                DomainServerImpl.getServerInstance()
                        .updateMetaObject(getUser(), aenderungsanfrageBean.getMetaObject(), getConnectionContext());
            }
            return true;
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
