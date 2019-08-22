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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

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
public class KassenzeichenChangeRequestServerAction implements MetaServiceStore,
    UserAwareServerAction,
    ServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(KassenzeichenChangeRequestServerAction.class);
    public static final String TASKNAME = "kassenzeichenChangeRequest";

    //~ Instance fields --------------------------------------------------------

    private User user;
    private MetaService metaService;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object object, final ServerActionParameter... params) {
        return null;
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

            final Map<String, FlaecheJson> flaechen = new HashMap<>();
            flaechen.put("5", new FlaecheGroesseJson(12d));
            final AnfrageJson anfrage = new AnfrageJson(
                    "60004629",
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
        } catch (final JsonProcessingException ex) {
            System.err.println(ex.getMessage());
            LOG.error(ex, ex);
        }
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
    public static class AnfrageJson {

        //~ Instance fields ----------------------------------------------------

        private String kassenzeichen;
        private Map<String, FlaecheJson> flaechen;
        private BemerkungJson bemerkung;
        private String pruefungStatus;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AnfrageJson object.
         *
         * @param  kassenzeichen  DOCUMENT ME!
         */
        public AnfrageJson(final String kassenzeichen) {
            this(kassenzeichen, null, null, null);
        }

        /**
         * Creates a new AnfrageJson object.
         *
         * @param  kassenzeichen  DOCUMENT ME!
         * @param  flaechen       DOCUMENT ME!
         */
        public AnfrageJson(final String kassenzeichen, final Map<String, FlaecheJson> flaechen) {
            this(kassenzeichen, flaechen, null, null);
        }

        /**
         * Creates a new AnfrageJson object.
         *
         * @param  kassenzeichen  DOCUMENT ME!
         * @param  flaechen       DOCUMENT ME!
         * @param  bemerkung      DOCUMENT ME!
         */
        public AnfrageJson(final String kassenzeichen,
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
}
