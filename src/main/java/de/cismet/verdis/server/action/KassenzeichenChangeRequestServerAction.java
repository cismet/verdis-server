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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.io.IOException;

import java.sql.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

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
            module.addDeserializer(FlaecheJson.class, new FlaecheJsonDeserializer(objectMapper));
            module.addDeserializer(BemerkungJson.class, new BemerkungJsonDeserializer(objectMapper));
            module.addDeserializer(AnfrageJson.class, new AnfrageJsonDeserializer(objectMapper));
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
            module.addDeserializer(FlaecheJson.class, new FlaecheJsonDeserializer(mapper));
            module.addDeserializer(BemerkungJson.class, new BemerkungJsonDeserializer(mapper));
            module.addDeserializer(AnfrageJson.class, new AnfrageJsonDeserializer(mapper));
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

            anfrage.getBemerkung().setSachbearbeiter("Konnte nichts feststellen, alles in Ordnung.");
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrage));

            anfrage.getBemerkung()
                    .setBemerkung(new BemerkungJson(
                            "Oh, falsches PDF, siehe richtiges pdf.",
                            "http://meine.domain.de/richtiges.pdf",
                            "Ach so, verstehe. Alles Klar !",
                            new BemerkungJson("Geht doch, danke.")));

            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrage));
            mapper.readValue(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrage), AnfrageJson.class);
        } catch (final Exception ex) {
            System.err.println(ex.getMessage());
            LOG.error(ex, ex);
        }

/*
{
  "kassenzeichen" : "60004629",
  "flaechen" : {
    "5" : {
      "groesse" : 12.0
    }
  },
  "bemerkung" : {
    "buerger" : "Da passt was nicht weil isso, siehe lustiges pdf !",
    "anhang" : "http://meine.domain.de/lustiges.pdf"
  }
}
{
  "kassenzeichen" : "60004629",
  "flaechen" : {
    "5" : {
      "groesse" : 12.0
    }
  },
  "bemerkung" : {
    "buerger" : "Da passt was nicht weil isso, siehe lustiges pdf !",
    "anhang" : "http://meine.domain.de/lustiges.pdf",
    "sachbearbeiter" : "Konnte nichts feststellen, alles in Ordnung."
  }
}
{
  "kassenzeichen" : "60004629",
  "flaechen" : {
    "5" : {
      "groesse" : 12.0
    }
  },
  "bemerkung" : {
    "buerger" : "Da passt was nicht weil isso, siehe lustiges pdf !",
    "anhang" : "http://meine.domain.de/lustiges.pdf",
    "sachbearbeiter" : "Konnte nichts feststellen, alles in Ordnung.",
    "bemerkung" : {
      "buerger" : "Oh, falsches PDF, siehe richtiges pdf.",
      "anhang" : "http://meine.domain.de/richtiges.pdf",
      "sachbearbeiter" : "Ach so, verstehe. Alles Klar !",
      "bemerkung" : {
        "buerger" : "Geht doch, danke."
      }
    }
  }
}
*/
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class AnfrageJson {

        //~ Instance fields ----------------------------------------------------

        private Integer kassenzeichen;
        private Map<String, FlaecheJson> flaechen;
        private BemerkungJson bemerkung;
        private String pruefungStatus;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AnfrageJson object.
         *
         * @param  kassenzeichen  DOCUMENT ME!
         */
        public AnfrageJson(final Integer kassenzeichen) {
            this(kassenzeichen, null, null, null);
        }

        /**
         * Creates a new AnfrageJson object.
         *
         * @param  kassenzeichen  DOCUMENT ME!
         * @param  flaechen       DOCUMENT ME!
         */
        public AnfrageJson(final Integer kassenzeichen, final Map<String, FlaecheJson> flaechen) {
            this(kassenzeichen, flaechen, null, null);
        }

        /**
         * Creates a new AnfrageJson object.
         *
         * @param  kassenzeichen  DOCUMENT ME!
         * @param  flaechen       DOCUMENT ME!
         * @param  bemerkung      DOCUMENT ME!
         */
        public AnfrageJson(final Integer kassenzeichen,
                final Map<String, FlaecheJson> flaechen,
                final BemerkungJson bemerkung) {
            this(kassenzeichen, flaechen, bemerkung, null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class BemerkungJson {

        //~ Instance fields ----------------------------------------------------

        private String buerger;
        private String anhang;
        private String sachbearbeiter;
        private BemerkungJson bemerkung;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new BemerkungJson object.
         *
         * @param  buerger  DOCUMENT ME!
         */
        public BemerkungJson(final String buerger) {
            this(buerger, null, null, null);
        }

        /**
         * Creates a new BemerkungJson object.
         *
         * @param  buerger  DOCUMENT ME!
         * @param  anhang   DOCUMENT ME!
         */
        public BemerkungJson(final String buerger, final String anhang) {
            this(buerger, anhang, null, null);
        }

        /**
         * Creates a new BemerkungJson object.
         *
         * @param  buerger         DOCUMENT ME!
         * @param  anhang          DOCUMENT ME!
         * @param  sachbearbeiter  DOCUMENT ME!
         */
        public BemerkungJson(final String buerger, final String anhang, final String sachbearbeiter) {
            this(buerger, anhang, sachbearbeiter, null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class FlaecheJson {

        //~ Instance fields ----------------------------------------------------

        private Double groesse;
        private String anschlussgrad;
        private String flaechenart;
        private BemerkungJson bemerkung;
        private String pruefungStatus;
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class FlaecheGroesseJson extends FlaecheJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheGroesseJson object.
         *
         * @param  groesse  DOCUMENT ME!
         */
        public FlaecheGroesseJson(final Double groesse) {
            super(groesse, null, null, null, null);
        }

        /**
         * Creates a new FlaecheGroesseJson object.
         *
         * @param  groesse    DOCUMENT ME!
         * @param  bemerkung  DOCUMENT ME!
         */
        public FlaecheGroesseJson(final Double groesse, final BemerkungJson bemerkung) {
            super(groesse, null, null, bemerkung, null);
        }

        /**
         * Creates a new FlaecheGroesseJson object.
         *
         * @param  groesse         DOCUMENT ME!
         * @param  bemerkung       DOCUMENT ME!
         * @param  pruefungStatus  DOCUMENT ME!
         */
        public FlaecheGroesseJson(final Double groesse, final BemerkungJson bemerkung, final String pruefungStatus) {
            super(groesse, null, null, bemerkung, pruefungStatus);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class FlaecheAnschlussgradJson extends FlaecheJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheAnschlussgradJson object.
         *
         * @param  anschlussgrad  DOCUMENT ME!
         */
        public FlaecheAnschlussgradJson(final String anschlussgrad) {
            super(null, anschlussgrad, null, null, null);
        }

        /**
         * Creates a new FlaecheAnschlussgradJson object.
         *
         * @param  anschlussgrad  DOCUMENT ME!
         * @param  bemerkung      DOCUMENT ME!
         */
        public FlaecheAnschlussgradJson(final String anschlussgrad, final BemerkungJson bemerkung) {
            super(null, anschlussgrad, null, bemerkung, null);
        }

        /**
         * Creates a new FlaecheAnschlussgradJson object.
         *
         * @param  anschlussgrad   DOCUMENT ME!
         * @param  bemerkung       DOCUMENT ME!
         * @param  pruefungStatus  DOCUMENT ME!
         */
        public FlaecheAnschlussgradJson(final String anschlussgrad,
                final BemerkungJson bemerkung,
                final String pruefungStatus) {
            super(null, anschlussgrad, null, bemerkung, pruefungStatus);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class FlaecheArtJson extends FlaecheJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheArtJson object.
         *
         * @param  art  DOCUMENT ME!
         */
        public FlaecheArtJson(final String art) {
            super(null, null, art, null, null);
        }

        /**
         * Creates a new FlaecheArtJson object.
         *
         * @param  art        DOCUMENT ME!
         * @param  bemerkung  DOCUMENT ME!
         */
        public FlaecheArtJson(final String art, final BemerkungJson bemerkung) {
            super(null, null, art, bemerkung, null);
        }

        /**
         * Creates a new FlaecheArtJson object.
         *
         * @param  art             DOCUMENT ME!
         * @param  bemerkung       DOCUMENT ME!
         * @param  pruefungStatus  DOCUMENT ME!
         */
        public FlaecheArtJson(final String art, final BemerkungJson bemerkung, final String pruefungStatus) {
            super(null, null, art, bemerkung, pruefungStatus);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    static class AnfrageJsonDeserializer extends StdDeserializer<AnfrageJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AnfrageJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public AnfrageJsonDeserializer(final ObjectMapper objectMapper) {
            super(AnfrageJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public AnfrageJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final Integer kassenzeichen = on.has("kassenzeichen") ? on.get("kassenzeichen").asInt() : null;
            final BemerkungJson bemerkung = on.has("bemerkung")
                ? objectMapper.treeToValue(on.get("bemerkung"), BemerkungJson.class) : null;
            final String pruefungStatus = on.has("pruefungStatus") ? on.get("pruefungStatus").textValue() : null;
            final Map<String, FlaecheJson> flaechen;
            if (on.has("flaechen") && on.get("flaechen").isObject()) {
                flaechen = new HashMap<>();
                final Iterator<Entry<String, JsonNode>> fieldIterator = on.get("flaechen").fields();
                while (fieldIterator.hasNext()) {
                    final Entry<String, JsonNode> fieldEntry = fieldIterator.next();
                    final String bezeichnung = fieldEntry.getKey();
                    // TODO: check for valid bezeichnung.
                    flaechen.put(bezeichnung, objectMapper.treeToValue(fieldEntry.getValue(), FlaecheJson.class));
                }
            } else {
                flaechen = null;
            }

            if (kassenzeichen == null) {
                throw new RuntimeException("invalid AnfrageJson: kassenzeichen is missing");
            }
            return new AnfrageJson(kassenzeichen, flaechen, bemerkung, pruefungStatus);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    static class FlaecheJsonDeserializer extends StdDeserializer<FlaecheJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public FlaecheJsonDeserializer(final ObjectMapper objectMapper) {
            super(FlaecheJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public FlaecheJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final String anschlussgrad = on.has("anschlussgrad") ? on.get("anschlussgrad").textValue() : null;
            final String flaechenart = on.has("flaechenart") ? on.get("flaechenart").textValue() : null;
            final Double groesse = on.has("groesse") ? on.get("groesse").doubleValue() : null;
            final BemerkungJson bemerkung = on.has("bemerkung")
                ? objectMapper.treeToValue(on.get("bemerkung"), BemerkungJson.class) : null;
            final String pruefungStatus = on.has("pruefungStatus") ? on.get("pruefungStatus").textValue() : null;
            if ((anschlussgrad == null) && (flaechenart == null) && (groesse == null)) {
                throw new RuntimeException(
                    "invalid BemerkungJson: neither anschlussgrad nor flaechenart nor groesse is set");
            }
            if ((groesse != null) && (groesse < 0)) {
                throw new RuntimeException("invalid BemerkungJson: groesse can't be negative");
            }
            // TODO: check for valid anschlussgrad
            // TODO: check for valid flaechenart
            return new FlaecheJson(groesse, anschlussgrad, flaechenart, bemerkung, pruefungStatus);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    static class BemerkungJsonDeserializer extends StdDeserializer<BemerkungJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new BemerkungJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public BemerkungJsonDeserializer(final ObjectMapper objectMapper) {
            super(BemerkungJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public BemerkungJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final String buerger = on.has("buerger") ? on.get("buerger").textValue() : null;
            final String sachbearbeiter = on.has("sachbearbeiter") ? on.get("sachbearbeiter").textValue() : null;
            final String anhang = on.has("anhang") ? on.get("anhang").textValue() : null;
            final BemerkungJson bemerkung = on.has("bemerkung")
                ? objectMapper.treeToValue(on.get("bemerkung"), BemerkungJson.class) : null;
            if ((buerger == null) && (sachbearbeiter == null)) {
                throw new RuntimeException("invalid BemerkungJson: neither buerger nor sachbearbeiter is set");
            }
            return new BemerkungJson(buerger, anhang, sachbearbeiter, bemerkung);
        }
    }
}
