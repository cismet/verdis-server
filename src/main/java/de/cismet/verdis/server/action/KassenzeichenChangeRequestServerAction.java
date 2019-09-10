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

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
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
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaecheGroesseJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaecheJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.NachrichtJson;
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
                final AnfrageJson anfrage = AnfrageJson.readValue(changerequestJson);
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

/*
{
  "kassenzeichen" : 60004629,
  "flaechen" : {
    "5" : {
      "groesse" : 12
    }
  },
  "nachrichten" : {
    "buerger" : {
      "nachricht" : "Da passt was nicht weil isso, siehe lustiges pdf !",
      "timestamp" : 1566915257744,
      "anhang" : "http://meine.domain.de/lustiges.pdf"
    }
  }
}
{
  "kassenzeichen" : 60004629,
  "flaechen" : {
    "5" : {
      "groesse" : 12,
      "pruefung" : {
        "status" : "REJECTED",
        "von" : "test",
        "timestamp" : 1566915257854
      }
    }
  },
  "nachrichten" : {
    "buerger" : {
      "nachricht" : "Da passt was nicht weil isso, siehe lustiges pdf !",
      "timestamp" : 1566915257744,
      "anhang" : "http://meine.domain.de/lustiges.pdf"
    },
    "sachbearbeiter" : {
      "nachricht" : "Konnte nichts feststellen, alles in Ordnung.",
      "timestamp" : 1566915257854
    }
  }
}
{
  "kassenzeichen" : 60004629,
  "flaechen" : {
    "5" : {
      "groesse" : 12,
      "pruefung" : {
        "status" : "REJECTED",
        "von" : "test",
        "timestamp" : 1566915257854,
        "next" : {
          "status" : "ACCEPTED",
          "von" : "test",
          "timestamp" : 1566915257858
        }
      }
    }
  },
  "nachrichten" : {
    "buerger" : {
      "nachricht" : "Da passt was nicht weil isso, siehe lustiges pdf !",
      "timestamp" : 1566915257744,
      "anhang" : "http://meine.domain.de/lustiges.pdf"
    },
    "sachbearbeiter" : {
      "nachricht" : "Konnte nichts feststellen, alles in Ordnung.",
      "timestamp" : 1566915257854
    },
    "next" : {
      "buerger" : {
        "nachricht" : "Oh, falsches PDF, siehe richtiges pdf.",
        "timestamp" : 1566915257858,
        "anhang" : "http://meine.domain.de/richtiges.pdf"
      },
      "sachbearbeiter" : {
        "nachricht" : "Ach so, verstehe. Alles Klar !",
        "timestamp" : 1566915257858
      },
      "next" : {
        "buerger" : {
          "nachricht" : "Geht doch, danke.",
          "timestamp" : 1566915257858
        }
      }
    }
  }
}
*/
}
