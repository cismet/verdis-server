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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.log4j.Logger;

import java.sql.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.utils.StacUtils;
import de.cismet.verdis.server.utils.aenderungsanfrage.AnfrageJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.BemerkungJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaecheGroesseJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaecheJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.PruefungJson;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenChangeRequestServerAction object.
     */
    public KassenzeichenChangeRequestServerAction() {
        try {
            final SimpleModule module = new SimpleModule();
            module.addDeserializer(FlaecheJson.class, new FlaecheJson.Deserializer(objectMapper));
            module.addDeserializer(BemerkungJson.class, new BemerkungJson.Deserializer(objectMapper));
            module.addDeserializer(AnfrageJson.class, new AnfrageJson.Deserializer(objectMapper));
            objectMapper.registerModule(module);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        } catch (final Throwable t) {
            LOG.fatal("this should never happen", t);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object object, final ServerActionParameter... params) {
        String stac = null;
        String email = null;
        String changerequestJson = null;

        try {
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    final String key = sap.getKey();
                    final Object value = sap.getValue();
                    if (Parameter.STAC.toString().equals(key)) {
                        stac = (String)value;
                    } else if (Parameter.CHANGEREQUEST_JSON.toString().equals(key)) {
                        changerequestJson = objectMapper.writeValueAsString(value);
                    } else if (Parameter.EMAIL.toString().equals(key)) {
                        email = (String)value;
                    }
                }
            }

            if ((stac != null) && (changerequestJson != null)) {
                final AnfrageJson anfrage = objectMapper.readValue(changerequestJson, AnfrageJson.class);
                final StacUtils.StacEntry stacEntry = StacUtils.getStacEntry(
                        stac,
                        getMetaService(),
                        getConnectionContext());
                final CidsBean kassenzeichenBean = StacUtils.getKassenzeichenBean(
                        stacEntry,
                        getMetaService(),
                        getConnectionContext());
                final Integer kassenzeichenNummerFromBean = (Integer)kassenzeichenBean.getProperty(
                        VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER);
                final Integer kassenzeichenNummerFromJson = anfrage.getKassenzeichen();
                final CidsBean aenderungsanfrageSearchBean = StacUtils.getAenderungsanfrageBean(
                        stacEntry,
                        getMetaService(),
                        getConnectionContext());
                final CidsBean aenderungsanfrageBean = (aenderungsanfrageSearchBean != null)
                    ? aenderungsanfrageSearchBean
                    : CidsBean.createNewCidsBeanFromTableName(
                        VerdisConstants.DOMAIN,
                        VerdisConstants.MC.AENDERUNGSANFRAGE,
                        getConnectionContext());
                aenderungsanfrageBean.setProperty(
                    VerdisConstants.PROP.AENDERUNGSANFRAGE.CHANGES_JSON,
                    objectMapper.writeValueAsString(anfrage));
                aenderungsanfrageBean.setProperty(VerdisConstants.PROP.AENDERUNGSANFRAGE.STAC_ID, stacEntry.getId());
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

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            final SimpleModule module = new SimpleModule();
            module.addDeserializer(FlaecheJson.class, new FlaecheJson.Deserializer(mapper));
            module.addDeserializer(BemerkungJson.class, new BemerkungJson.Deserializer(mapper));
            module.addDeserializer(AnfrageJson.class, new AnfrageJson.Deserializer(mapper));
            mapper.registerModule(module);

            final Map<String, FlaecheJson> flaechen = new HashMap<>();
            flaechen.put("5", new FlaecheGroesseJson(12d));
            final AnfrageJson anfrage = new AnfrageJson(
                    60004629,
                    flaechen,
                    new BemerkungJson(
                        "Da passt was nicht weil isso, siehe lustiges pdf !",
                        "http://meine.domain.de/lustiges.pdf"));
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrage));

            anfrage.getBemerkung().setBemerkungSachbearbeiter("Konnte nichts feststellen, alles in Ordnung.");
            anfrage.getFlaechen()
                    .get("5")
                    .addPruefung(new PruefungJson(PruefungJson.Status.REJECTED, "test", new Date()));
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrage));

            anfrage.addBemerkung(new BemerkungJson(
                    "Oh, falsches PDF, siehe richtiges pdf.",
                    "http://meine.domain.de/richtiges.pdf",
                    "Ach so, verstehe. Alles Klar !",
                    new BemerkungJson("Geht doch, danke.")));
            anfrage.getFlaechen()
                    .get("5")
                    .addPruefung(new PruefungJson(PruefungJson.Status.ACCEPTED, "test", new Date()));

            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrage));
            mapper.readValue(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrage), AnfrageJson.class);
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
            LOG.error(ex, ex);
        }

/*
{
  "kassenzeichen" : 60004629,
  "flaechen" : {
    "5" : {
      "groesse" : 12.0
    }
  },
  "bemerkung" : {
    "bemerkungBuerger" : "Da passt was nicht weil isso, siehe lustiges pdf !",
    "anhangBuerger" : "http://meine.domain.de/lustiges.pdf"
  },
  "lastBemerkung" : {
    "bemerkungBuerger" : "Da passt was nicht weil isso, siehe lustiges pdf !",
    "anhangBuerger" : "http://meine.domain.de/lustiges.pdf"
  }
}
{
  "kassenzeichen" : 60004629,
  "flaechen" : {
    "5" : {
      "groesse" : 12.0,
      "pruefung" : {
        "status" : "REJECTED",
        "von" : "test",
        "timestamp" : 1566911577668
      },
      "lastPruefung" : {
        "status" : "REJECTED",
        "von" : "test",
        "timestamp" : 1566911577668
      }
    }
  },
  "bemerkung" : {
    "bemerkungBuerger" : "Da passt was nicht weil isso, siehe lustiges pdf !",
    "anhangBuerger" : "http://meine.domain.de/lustiges.pdf",
    "bemerkungSachbearbeiter" : "Konnte nichts feststellen, alles in Ordnung."
  },
  "lastBemerkung" : {
    "bemerkungBuerger" : "Da passt was nicht weil isso, siehe lustiges pdf !",
    "anhangBuerger" : "http://meine.domain.de/lustiges.pdf",
    "bemerkungSachbearbeiter" : "Konnte nichts feststellen, alles in Ordnung."
  }
}
{
  "kassenzeichen" : 60004629,
  "flaechen" : {
    "5" : {
      "groesse" : 12.0,
      "pruefung" : {
        "status" : "REJECTED",
        "von" : "test",
        "timestamp" : 1566911577668,
        "next" : {
          "status" : "ACCEPTED",
          "von" : "test",
          "timestamp" : 1566911577677
        }
      },
      "lastPruefung" : {
        "status" : "ACCEPTED",
        "von" : "test",
        "timestamp" : 1566911577677
      }
    }
  },
  "bemerkung" : {
    "bemerkungBuerger" : "Da passt was nicht weil isso, siehe lustiges pdf !",
    "anhangBuerger" : "http://meine.domain.de/lustiges.pdf",
    "bemerkungSachbearbeiter" : "Konnte nichts feststellen, alles in Ordnung.",
    "next" : {
      "bemerkungBuerger" : "Oh, falsches PDF, siehe richtiges pdf.",
      "anhangBuerger" : "http://meine.domain.de/richtiges.pdf",
      "bemerkungSachbearbeiter" : "Ach so, verstehe. Alles Klar !",
      "next" : {
        "bemerkungBuerger" : "Geht doch, danke."
      }
    }
  },
  "lastBemerkung" : {
    "bemerkungBuerger" : "Geht doch, danke."
  }
}
*/
    }
}
