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
package de.cismet.verdis.server.utils;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.newuser.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import org.geojson.Feature;
import org.geojson.GeoJsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.sql.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.search.CidsServerSearch;
import de.cismet.cids.server.search.SearchException;

import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.json.AenderungsanfrageJson;
import de.cismet.verdis.server.json.AenderungsanfrageResultJson;
import de.cismet.verdis.server.json.ContactInfoJson;
import de.cismet.verdis.server.json.ContactInfosJson;
import de.cismet.verdis.server.json.FlaecheAenderungJson;
import de.cismet.verdis.server.json.FlaecheAnschlussgradJson;
import de.cismet.verdis.server.json.FlaecheFlaechenartJson;
import de.cismet.verdis.server.json.FlaechePruefungJson;
import de.cismet.verdis.server.json.MessageConfigJson;
import de.cismet.verdis.server.json.NachrichtAnhangJson;
import de.cismet.verdis.server.json.NachrichtJson;
import de.cismet.verdis.server.json.NachrichtParameterAnschlussgradJson;
import de.cismet.verdis.server.json.NachrichtParameterFlaechenartJson;
import de.cismet.verdis.server.json.NachrichtParameterGroesseJson;
import de.cismet.verdis.server.json.NachrichtParameterJson;
import de.cismet.verdis.server.json.NachrichtParameterStatusJson;
import de.cismet.verdis.server.json.NachrichtSystemJson;
import de.cismet.verdis.server.json.PruefungAnschlussgradJson;
import de.cismet.verdis.server.json.PruefungFlaechenartJson;
import de.cismet.verdis.server.json.PruefungGroesseJson;
import de.cismet.verdis.server.jsondeserializer.AenderungsanfrageDeserializer;
import de.cismet.verdis.server.jsondeserializer.AenderungsanfrageResultDeserializer;
import de.cismet.verdis.server.jsondeserializer.FlaecheAenderungDeserializer;
import de.cismet.verdis.server.jsondeserializer.FlaecheAnschlussgradDeserializer;
import de.cismet.verdis.server.jsondeserializer.FlaecheFlaechenartDeserializer;
import de.cismet.verdis.server.jsondeserializer.FlaechePruefungDeserializer;
import de.cismet.verdis.server.jsondeserializer.NachrichtAnhangDeserializer;
import de.cismet.verdis.server.jsondeserializer.NachrichtDeserializer;
import de.cismet.verdis.server.jsondeserializer.NachrichtParameterDeserializer;
import de.cismet.verdis.server.jsondeserializer.PruefungAnschlussgradDeserializer;
import de.cismet.verdis.server.jsondeserializer.PruefungFlaechenartDeserializer;
import de.cismet.verdis.server.jsondeserializer.PruefungGroesseDeserializer;
import de.cismet.verdis.server.search.AenderungsanfrageSearchStatement;
import de.cismet.verdis.server.search.AenderungsanfrageStatusSearchStatement;

import static de.cismet.verdis.server.utils.StacUtils.getUser;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class AenderungsanfrageUtils {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AenderungsanfrageUtils.class);

    private static final Comparator<NachrichtJson> NACHRICHTEN_TIMEORDER_COMPARATOR = new Comparator<NachrichtJson>() {

            @Override
            public int compare(final NachrichtJson o1, final NachrichtJson o2) {
                final Date t1 = (o1 != null) ? o1.getTimestamp() : null;
                final Date t2 = (o2 != null) ? o2.getTimestamp() : null;
                final Integer r1 = (o1 != null) ? o1.getOrder() : null;
                final Integer r2 = (o2 != null) ? o2.getOrder() : null;

                if (!Objects.equals(t1, t2)) {        // können nicht beide null sein
                    return ObjectUtils.compare(t1, t2);
                } else if (!Objects.equals(r1, r2)) { // können nicht beide null sein
                    return ObjectUtils.compare(r1, r2);
                } else {                              // beide sind gleich
                    return 0;
                }
            }
        };

    public static final String CMDREPLACER_CLERK_EMAIL = "{CLERK_MAIL}";
    public static final String CMDREPLACER_CITIZEN_EMAIL = "{CITIZEN_MAIL}";
    public static final String CMDREPLACER_TOPIC = "{TOPIC}";
    public static final String CMDREPLACER_MESSAGE = "{MESSAGE}";

    public static final String DEFAULT_CMDTEMPLATE = "sendEmail"
                + " -s   smtp.wuppertal-intra.de"
                + " -f   regengeld@stadt.wuppertal.de"
                + " -t   \"" + CMDREPLACER_CITIZEN_EMAIL + "\""
                + " -bcc \"" + CMDREPLACER_CLERK_EMAIL + "\""
                + " -u   \"" + CMDREPLACER_TOPIC + "\""
                + " -m   \"" + CMDREPLACER_MESSAGE + "\"";

    public static final String MESSAGETYPE_MAILVERIFICATION = "MAILVERIFICATION";
    public static final String MESSAGETYPE_MAILCONFIRMATION = "MAILCONFIRMATION";
    public static final String MESSAGETYPE_NOTIFY = "NOTIFY";
    public static final String MESSAGETYPE_SUBMISSION = "SUBMISSION";

    private static final String CONFIG_JSON_FORMAT = "config.%s.json";
    private static final String TEMPLATEREPLACER_KASSENZEICHEN = "{KASSENZEICHEN}";
    private static final String TEMPLATEREPLACER_CODE = "{CODE}";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Status {

        //~ Enum constants -----------------------------------------------------

        NONE, NEW_CITIZEN_MESSAGE, PENDING, PROCESSING, CLOSED, ARCHIVED;
    }

    //~ Instance fields --------------------------------------------------------

    @Setter private boolean unitTestContext = false;

    private final Map<Integer, EmailVerification> emailVerificationMap = new HashMap();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenChangeRequestServerAction object.
     */
    private AenderungsanfrageUtils() {
        try {
            final SimpleModule module = new SimpleModule();
            module.addDeserializer(PruefungGroesseJson.class, new PruefungGroesseDeserializer(MAPPER));
            module.addDeserializer(PruefungFlaechenartJson.class, new PruefungFlaechenartDeserializer(MAPPER));
            module.addDeserializer(PruefungAnschlussgradJson.class,
                new PruefungAnschlussgradDeserializer(MAPPER));
            module.addDeserializer(NachrichtParameterJson.class,
                new NachrichtParameterDeserializer(MAPPER));
            module.addDeserializer(PruefungGroesseJson.class, new PruefungGroesseDeserializer(MAPPER));
            module.addDeserializer(FlaechePruefungJson.class, new FlaechePruefungDeserializer(MAPPER));
            module.addDeserializer(FlaecheAenderungJson.class, new FlaecheAenderungDeserializer(MAPPER));
            module.addDeserializer(FlaecheAnschlussgradJson.class, new FlaecheAnschlussgradDeserializer(MAPPER));
            module.addDeserializer(FlaecheFlaechenartJson.class, new FlaecheFlaechenartDeserializer(MAPPER));
            module.addDeserializer(NachrichtAnhangJson.class, new NachrichtAnhangDeserializer(MAPPER));
            module.addDeserializer(NachrichtJson.class, new NachrichtDeserializer(MAPPER));
            module.addDeserializer(AenderungsanfrageJson.class, new AenderungsanfrageDeserializer(MAPPER));
            module.addDeserializer(AenderungsanfrageResultJson.class, new AenderungsanfrageResultDeserializer(MAPPER));
            MAPPER.registerModule(module);
        } catch (final Throwable t) {
            LOG.fatal("this should never happen", t);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  kassenzeichen  DOCUMENT ME!
     * @param  email          DOCUMENT ME!
     */
    private void removeEmail(final Integer kassenzeichen, final String email) {
        synchronized (emailVerificationMap) {
            emailVerificationMap.remove(kassenzeichen);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichen  DOCUMENT ME!
     * @param   email          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String addEmailVerification(final Integer kassenzeichen, final String email) {
        if (email != null) {
            final String code = RandomStringUtils.randomAlphanumeric(6);
            synchronized (emailVerificationMap) {
                emailVerificationMap.put(kassenzeichen, new EmailVerification(email, code));
            }
            return code;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichen  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private EmailVerification getEmailVerification(final Integer kassenzeichen) {
        synchronized (emailVerificationMap) {
            return emailVerificationMap.get(kassenzeichen);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static AenderungsanfrageConf getConfFromServerResource() throws Exception {
        final Properties properties = ServerResourcesLoader.getInstance()
                    .loadProperties(VerdisServerResources.AENDERUNTSANFRAGE.getValue());
        return new AenderungsanfrageConf(properties);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nachricht  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private String createIdentifier(final NachrichtJson nachricht) throws Exception {
        if (unitTestContext) {
            return (nachricht != null) ? DigestUtils.md5Hex(nachricht.toJson()) : null;
        } else {
            return UUID.randomUUID().toString();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nachrichtenOrig  DOCUMENT ME!
     * @param   nachrichtenNew   DOCUMENT ME!
     * @param   citizenOrClerk   DOCUMENT ME!
     * @param   username         DOCUMENT ME!
     * @param   timestamp        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<NachrichtJson> processNachrichten(final List<NachrichtJson> nachrichtenOrig,
            final List<NachrichtJson> nachrichtenNew,
            final Boolean citizenOrClerk,
            final String username,
            final Date timestamp) throws Exception {
        final boolean isCitizen = Boolean.TRUE.equals(citizenOrClerk);
        final boolean isClerk = Boolean.FALSE.equals(citizenOrClerk);
        final List<NachrichtJson> nachrichtenProcessed = new ArrayList<>();
        // zum identifizieren alter drafts die neu eingereicht wurden
        final Map<String, NachrichtJson> origNachrichtenMap = new HashMap<>();

        nachrichtenOrig.sort(NACHRICHTEN_TIMEORDER_COMPARATOR);
        for (final NachrichtJson origNachricht : nachrichtenOrig) {
            // Altnachrichten ohne UUID eine UUID verpassen
            // Sollte aber normalerweise nicht passieren
            // Außer bei alten Fällen. Ist nur aus abwärtskompatibilität nötig
            if (origNachricht.getIdentifier() == null) {
                origNachricht.setIdentifier(createIdentifier(origNachricht));
            }
            origNachrichtenMap.put(origNachricht.getIdentifier(), origNachricht);

            final boolean isDraft = Boolean.TRUE.equals(origNachricht.getDraft());
            final boolean isCitizenMessage = NachrichtJson.Typ.CITIZEN.equals(origNachricht.getTyp());
            final boolean isClerkMessage = NachrichtJson.Typ.CLERK.equals(origNachricht.getTyp());
            final boolean isOwnMessage = isCitizen ? isCitizenMessage : (isClerk ? isClerkMessage : false);

            // alle vorhandenen Fremdnachrichten, die kein Draft sind, werden auf alle Fälle übernommen
            if (!isDraft || !isOwnMessage) {
                nachrichtenProcessed.add(origNachricht);
            }
        }

        nachrichtenNew.sort(NACHRICHTEN_TIMEORDER_COMPARATOR);
        for (final NachrichtJson newNachricht : nachrichtenNew) {
            final boolean isCitizenMessage = NachrichtJson.Typ.CITIZEN.equals(newNachricht.getTyp());
            final boolean isClerkMessage = NachrichtJson.Typ.CLERK.equals(newNachricht.getTyp());
            final boolean isSystemMessage = NachrichtJson.Typ.SYSTEM.equals(newNachricht.getTyp());
            final boolean isOwnMessage = isCitizen ? isCitizenMessage : (isClerk ? isClerkMessage : false);
            final boolean isNew = (newNachricht.getIdentifier() == null)
                        || !origNachrichtenMap.containsKey(newNachricht.getIdentifier());
            final boolean wasPrefiouslyDraft = (origNachrichtenMap.get(newNachricht.getIdentifier()) != null)
                        && Boolean.TRUE.equals(origNachrichtenMap.get(newNachricht.getIdentifier()).getDraft());
            final NachrichtParameterJson nachrichtenParameter = newNachricht.getNachrichtenParameter();
            final boolean isClerksNotifyMessage = isSystemMessage && (nachrichtenParameter != null)
                        && NachrichtParameterJson.Type.NOTIFY.equals(nachrichtenParameter.getType())
                        && Boolean.FALSE.equals(nachrichtenParameter.getBenachrichtigt())
                        && username.equals(newNachricht.getAbsender());
            final boolean isClerksProlongMessage = isSystemMessage && (nachrichtenParameter != null)
                        && NachrichtParameterJson.Type.PROLONG.equals(nachrichtenParameter.getType())
                        && Boolean.FALSE.equals(nachrichtenParameter.getVerlaengert())
                        && username.equals(newNachricht.getAbsender());

            // nur eigene neue Nachrichten betrachten
            if ((isOwnMessage && (isNew || wasPrefiouslyDraft)) || isClerksNotifyMessage || isClerksProlongMessage) {
                final NachrichtJson origNachricht =
                    ((newNachricht.getIdentifier() != null)
                                && origNachrichtenMap.containsKey(newNachricht.getIdentifier()))
                    ? origNachrichtenMap.get(newNachricht.getIdentifier()) : null;
                final boolean draftBecomesReal = !Boolean.TRUE.equals(newNachricht.getDraft())
                            && (origNachricht != null) && Boolean.TRUE.equals(origNachricht.getDraft());
                final boolean timestampIsNull = newNachricht.getTimestamp() == null;

                if (isNew || draftBecomesReal || timestampIsNull) {
                    // bekommen den aktuellen Timestamp
                    newNachricht.setTimestamp(timestamp);
                }

                if (newNachricht.getIdentifier() == null) {
                    newNachricht.setIdentifier(createIdentifier(newNachricht));
                }
                nachrichtenProcessed.add(newNachricht);
            }
        }
        nachrichtenProcessed.sort(NACHRICHTEN_TIMEORDER_COMPARATOR);
        return nachrichtenProcessed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   existingFlaechen  DOCUMENT ME!
     * @param   flaechenOrig      DOCUMENT ME!
     * @param   flaechenNew       DOCUMENT ME!
     * @param   citizenOrClerk    DOCUMENT ME!
     * @param   veranlagt         DOCUMENT ME!
     * @param   username          DOCUMENT ME!
     * @param   timestamp         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Map<String, FlaecheAenderungJson> processFlaechenUndPruefung(
            final Map<String, CidsBean> existingFlaechen,
            final Map<String, FlaecheAenderungJson> flaechenOrig,
            final Map<String, FlaecheAenderungJson> flaechenNew,
            final Boolean citizenOrClerk,
            final Boolean veranlagt,
            final String username,
            final Date timestamp) throws Exception {
        final boolean isCitizen = Boolean.TRUE.equals(citizenOrClerk);
        final boolean isClerk = Boolean.FALSE.equals(citizenOrClerk);
        final Map<String, FlaecheAenderungJson> flaechenProcessed = new HashMap<>();

        if (flaechenOrig == null) { // ???
            // keine pruefung übernehmen (ist neu, kann noch nicht geprueft worden sein)
            for (final String bezeichnung : flaechenNew.keySet()) {
                if (existingFlaechen.containsKey(bezeichnung)) {
                    final FlaecheAenderungJson flaeche = flaechenNew.get(bezeichnung);
                    flaeche.setPruefung(null);
                    flaechenProcessed.put(bezeichnung, flaeche);
                } else {
                    LOG.info("ignoring flaeche " + bezeichnung + " because it does not exist in the bean");
                }
            }
        } else {
            // alle Originalflaechen erst einmal pauschal übernehmen
            for (final String bezeichnung : flaechenOrig.keySet()) {
                if (existingFlaechen.containsKey(bezeichnung)) {
                    final FlaecheAenderungJson flaecheOrig = flaechenOrig.get(bezeichnung);
                    final FlaecheAenderungJson flaecheProcessed = getMapper().readValue(flaecheOrig.toJson(),
                            FlaecheAenderungJson.class);
                    flaechenProcessed.put(bezeichnung, flaecheProcessed);
                } else {
                    LOG.info("ignoring flaeche " + bezeichnung + " because it does not exist in the bean");
                }
            }

            // anfragen untersuchen und selektiv bearbeiten
            for (final String bezeichnung : flaechenNew.keySet()) {
                if (existingFlaechen.containsKey(bezeichnung)) {
                    final FlaecheAenderungJson flaecheOrig = flaechenOrig.containsKey(bezeichnung)
                        ? flaechenOrig.get(bezeichnung) : null;
                    final FlaecheAenderungJson flaecheNew = flaechenNew.get(bezeichnung);

                    final FlaecheAenderungJson flaecheProcessed;
                    if (flaecheOrig == null) {
                        // neue Änderung ünernehmen, aber ohne pruefung (kann es nicht gegeben haben)
                        flaecheProcessed = getMapper().readValue(flaecheNew.toJson(), FlaecheAenderungJson.class);
                        flaecheProcessed.setPruefung(null);
                    } else {
                        flaecheProcessed = getMapper().readValue(flaecheOrig.toJson(), FlaecheAenderungJson.class);

                        final Integer groesseNew = (flaecheNew != null) ? flaecheNew.getGroesse() : null;
                        final FlaecheFlaechenartJson flaechenartNew = (flaecheNew != null) ? flaecheNew
                                        .getFlaechenart() : null;
                        final FlaecheAnschlussgradJson anschlussgradNew = (flaecheNew != null)
                            ? flaecheNew.getAnschlussgrad() : null;

                        final Integer groesseOrig = flaecheOrig.getGroesse();
                        final FlaecheFlaechenartJson flaechenartOrig = flaecheOrig.getFlaechenart();
                        final FlaecheAnschlussgradJson anschlussgradOrig = flaecheOrig.getAnschlussgrad();

                        // nur der eigentümer darf die flächenanfragen verändern
                        // tut er dies, hat es ein zurücksetzen der Prüfung zur Folge
                        if (isCitizen) {
                            flaecheProcessed.setDraft(flaecheNew.getDraft());
                            // veränderte Flächen übernehmen, und pruefung entfernen falls vorhanden
                            if (!Objects.equals(groesseNew, groesseOrig)) {
                                flaecheProcessed.setGroesse(groesseNew);
                                if (flaecheProcessed.getPruefung() != null) {
                                    flaecheProcessed.getPruefung().setGroesse(null);
                                }
                            }
                            if (!Objects.equals(flaechenartNew, flaechenartOrig)) {
                                flaecheProcessed.setFlaechenart(flaechenartNew);
                                if (flaecheProcessed.getPruefung() != null) {
                                    flaecheProcessed.getPruefung().setFlaechenart(null);
                                }
                            }
                            if (!Objects.equals(anschlussgradNew, anschlussgradOrig)) {
                                flaecheProcessed.setAnschlussgrad(anschlussgradNew);
                                if (flaecheProcessed.getPruefung() != null) {
                                    flaecheProcessed.getPruefung().setAnschlussgrad(null);
                                }
                            }
                        } else
                        // nur der Bearbeiter darf die Prüfung verändern
                        // tut er dies, wird der Timestamp korrekt gesetzt
                        if (isClerk) {
                            final CidsBean flaecheBean = existingFlaechen.get(bezeichnung);

                            final Integer groesseCids = (flaecheBean != null)
                                ? (Integer)flaecheBean.getProperty(
                                    VerdisConstants.PROP.FLAECHE.FLAECHENINFO
                                            + "."
                                            + VerdisConstants.PROP.FLAECHENINFO.GROESSE_KORREKTUR) : null;
                            final FlaecheFlaechenartJson flaechenartCids =
                                ((flaecheBean != null)
                                            && (flaecheBean.getProperty(
                                                    VerdisConstants.PROP.FLAECHE.FLAECHENINFO
                                                    + "."
                                                    + VerdisConstants.PROP.FLAECHENINFO.FLAECHENART) != null))
                                ? new FlaecheFlaechenartJson((String)flaecheBean.getProperty(
                                        VerdisConstants.PROP.FLAECHE.FLAECHENINFO
                                                + "."
                                                + VerdisConstants.PROP.FLAECHENINFO.FLAECHENART
                                                + "."
                                                + VerdisConstants.PROP.FLAECHENART.ART),
                                    (String)flaecheBean.getProperty(
                                        VerdisConstants.PROP.FLAECHE.FLAECHENINFO
                                                + "."
                                                + VerdisConstants.PROP.FLAECHENINFO.FLAECHENART
                                                + "."
                                                + VerdisConstants.PROP.FLAECHENART.ART_ABKUERZUNG)) : null;
                            final FlaecheAnschlussgradJson anschlussgradCids =
                                ((flaecheBean != null)
                                            && (flaecheBean.getProperty(
                                                    VerdisConstants.PROP.FLAECHE.FLAECHENINFO
                                                    + "."
                                                    + VerdisConstants.PROP.FLAECHENINFO.ANSCHLUSSGRAD) != null))
                                ? new FlaecheAnschlussgradJson((String)flaecheBean.getProperty(
                                        VerdisConstants.PROP.FLAECHE.FLAECHENINFO
                                                + "."
                                                + VerdisConstants.PROP.FLAECHENINFO.ANSCHLUSSGRAD
                                                + "."
                                                + VerdisConstants.PROP.ANSCHLUSSGRAD.GRAD),
                                    (String)flaecheBean.getProperty(
                                        VerdisConstants.PROP.FLAECHE.FLAECHENINFO
                                                + "."
                                                + VerdisConstants.PROP.FLAECHENINFO.ANSCHLUSSGRAD
                                                + "."
                                                + VerdisConstants.PROP.ANSCHLUSSGRAD.GRAD_ABKUERZUNG)) : null;

                            final FlaechePruefungJson pruefungOrig = flaecheOrig.getPruefung();
                            final PruefungGroesseJson pruefungGroesseOrig = (pruefungOrig != null)
                                ? pruefungOrig.getGroesse() : null;
                            final PruefungFlaechenartJson pruefungFlaechenartOrig = (pruefungOrig != null)
                                ? pruefungOrig.getFlaechenart() : null;
                            final PruefungAnschlussgradJson pruefungAnschlussgradOrig = (pruefungOrig != null)
                                ? pruefungOrig.getAnschlussgrad() : null;

                            final FlaechePruefungJson pruefungNew = (flaecheNew != null) ? flaecheNew.getPruefung()
                                                                                         : null;
                            final PruefungGroesseJson pruefungGroesseNew = (pruefungNew != null)
                                ? pruefungNew.getGroesse() : null;
                            final PruefungFlaechenartJson pruefungFlaechenartNew = (pruefungNew != null)
                                ? pruefungNew.getFlaechenart() : null;
                            final PruefungAnschlussgradJson pruefungAnschlussgradNew = (pruefungNew != null)
                                ? pruefungNew.getAnschlussgrad() : null;

                            final PruefungGroesseJson pruefungGroesseAutoaccept =
                                (Objects.equals(groesseCids, groesseOrig)) ? new PruefungGroesseJson(groesseCids)
                                                                           : null;
                            final PruefungFlaechenartJson pruefungFlaechenartAutoaccept =
                                (Objects.equals(flaechenartCids, flaechenartOrig))
                                ? new PruefungFlaechenartJson(flaechenartCids) : null;
                            final PruefungAnschlussgradJson pruefungAnschlussgradAutoaccept =
                                (Objects.equals(anschlussgradCids, anschlussgradOrig))
                                ? new PruefungAnschlussgradJson(anschlussgradCids) : null;

                            final PruefungGroesseJson pruefungGroesseProcessed = (veranlagt == null)
                                ? pruefungGroesseOrig
                                : ((pruefungGroesseAutoaccept != null)
                                    ? pruefungGroesseAutoaccept
                                    : (((pruefungGroesseNew != null)
                                                    && Boolean.TRUE.equals(pruefungGroesseNew.getPending()))
                                        ? pruefungGroesseNew : pruefungGroesseOrig));
                            final PruefungFlaechenartJson pruefungFlaechenartProcessed = (veranlagt == null)
                                ? pruefungFlaechenartOrig
                                : ((pruefungFlaechenartAutoaccept != null)
                                    ? pruefungFlaechenartAutoaccept
                                    : (((pruefungFlaechenartNew != null)
                                                    && Boolean.TRUE.equals(pruefungFlaechenartNew.getPending()))
                                        ? pruefungFlaechenartNew : pruefungFlaechenartOrig));
                            final PruefungAnschlussgradJson pruefungAnschlussgradProcessed = (veranlagt == null)
                                ? pruefungAnschlussgradOrig
                                : ((pruefungAnschlussgradAutoaccept != null)
                                    ? pruefungAnschlussgradAutoaccept
                                    : (((pruefungAnschlussgradNew != null)
                                                    && Boolean.TRUE.equals(pruefungAnschlussgradNew.getPending()))
                                        ? pruefungAnschlussgradNew : pruefungAnschlussgradOrig));

                            if (pruefungGroesseProcessed != null) {
                                pruefungGroesseProcessed.setPending(null);
                                if (!Objects.equals(pruefungGroesseOrig, pruefungGroesseProcessed)) {
                                    pruefungGroesseProcessed.setTimestamp(timestamp);
                                    pruefungGroesseProcessed.setVon(username);
                                }
                            }
                            if (pruefungFlaechenartProcessed != null) {
                                pruefungFlaechenartProcessed.setPending(null);
                                if (!Objects.equals(pruefungFlaechenartOrig, pruefungFlaechenartProcessed)) {
                                    pruefungFlaechenartProcessed.setTimestamp(timestamp);
                                    pruefungFlaechenartProcessed.setVon(username);
                                }
                            }
                            if (pruefungAnschlussgradProcessed != null) {
                                pruefungAnschlussgradProcessed.setPending(null);
                                if (!Objects.equals(pruefungAnschlussgradOrig, pruefungAnschlussgradProcessed)) {
                                    pruefungAnschlussgradProcessed.setTimestamp(timestamp);
                                    pruefungAnschlussgradProcessed.setVon(username);
                                }
                            }

                            flaecheProcessed.setPruefung(new FlaechePruefungJson(
                                    pruefungGroesseProcessed,
                                    pruefungFlaechenartProcessed,
                                    pruefungAnschlussgradProcessed));
                        }
                    }
                    flaechenProcessed.put(bezeichnung, flaecheProcessed);
                } else {
                    LOG.info("ignoring flaeche " + bezeichnung + " because it does not exist in the bean");
                }
            }
        }
        return flaechenProcessed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geometrienOrig  DOCUMENT ME!
     * @param   geometrienNew   DOCUMENT ME!
     * @param   citizenOrClerk  DOCUMENT ME!
     * @param   username        DOCUMENT ME!
     * @param   timestamp       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Map<String, GeoJsonObject> processAnmerkungen(final Map<String, GeoJsonObject> geometrienOrig,
            final Map<String, GeoJsonObject> geometrienNew,
            final Boolean citizenOrClerk,
            final String username,
            final Date timestamp) throws Exception {
        final boolean isCitizen = Boolean.TRUE.equals(citizenOrClerk);
        final boolean isClerk = Boolean.FALSE.equals(citizenOrClerk);
        final Map<String, GeoJsonObject> geometrienProcessed = new HashMap<>();

        if (isCitizen) {
            for (final String bezeichnung : geometrienNew.keySet()) {
                final Feature featureOrig = (Feature)geometrienOrig.get(bezeichnung);
                final Feature featureNew = (Feature)geometrienNew.get(bezeichnung);

                if (featureNew != null) {
                    final Feature featureProcessed = new Feature();

                    featureProcessed.setId(featureNew.getId());
                    featureProcessed.setGeometry(featureNew.getGeometry());
                    featureProcessed.setProperties(featureNew.getProperties());

                    // bei jeder Änderung => pruefung zurücksetzen
                    final String geoJsonOrigString = (featureOrig != null)
                        ? new ObjectMapper().writeValueAsString(featureOrig) : null;
                    final String geoJsonNewString = new ObjectMapper().writeValueAsString(featureNew);
                    if (!Boolean.TRUE.equals(featureNew.getProperty("draft"))
                                && !Objects.equals(geoJsonOrigString, geoJsonNewString)
                                && (featureProcessed.getProperties() != null)) {
                        featureProcessed.getProperties().remove("pruefung");
                        featureProcessed.getProperties().remove("pruefungVon");
                        featureProcessed.getProperties().remove("pruefungTimestamp");
                    }

                    geometrienProcessed.put(bezeichnung, featureProcessed);
                }
            }
        } else {
            // alle original anmerkungen übernehmen und ggf pruefung
            for (final String bezeichnung : geometrienOrig.keySet()) {
                final Feature featureOrig = (Feature)geometrienOrig.get(bezeichnung);
                final Feature featureNew = (Feature)geometrienNew.get(bezeichnung);

                final Feature featureProcessed = new Feature();
                featureProcessed.setId(featureOrig.getId());
                featureProcessed.setGeometry(featureOrig.getGeometry());
                featureProcessed.setProperties(featureOrig.getProperties());

                if (isClerk && (featureNew != null)) { // Prüfer darf prüfen
                    final Boolean pruefungOrig = featureOrig.getProperty("pruefung");
                    final Boolean pruefungNew = featureNew.getProperty("pruefung");
                    if (pruefungNew != null) {
                        featureProcessed.setProperty("pruefung", pruefungNew);
                    }
                    if (!Objects.equals(pruefungOrig, pruefungNew)) {
                        if (pruefungNew != null) {
                            featureProcessed.setProperty("pruefungVon", username);
                            featureProcessed.setProperty("pruefungTimestamp", timestamp);
                        }
                    }
                }

                geometrienProcessed.put(bezeichnung, featureProcessed);
            }
        }
        return geometrienProcessed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cmd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static String executeCmd(final String cmd) throws Exception {
        final ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", cmd);
        final Process process = builder.start();
        final InputStream is = process.getInputStream();
        return IOUtils.toString(new InputStreamReader(is));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cmdTemplate   DOCUMENT ME!
     * @param   emailAdresse  DOCUMENT ME!
     * @param   betreff       DOCUMENT ME!
     * @param   inhalt        DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static void sendMail(final String cmdTemplate,
            final String emailAdresse,
            final String betreff,
            final String inhalt) throws Exception {
        if ((emailAdresse != null) && (cmdTemplate != null)) {
            final String cmd =
                cmdTemplate.replaceAll(Pattern.quote(CMDREPLACER_CITIZEN_EMAIL), Matcher.quoteReplacement(emailAdresse)) //
                .replaceAll(Pattern.quote(CMDREPLACER_TOPIC), Matcher.quoteReplacement(betreff))                         //
                .replaceAll(Pattern.quote(CMDREPLACER_MESSAGE), Matcher.quoteReplacement(inhalt))                        //
                ;
            LOG.info(String.format("executing sendMail CMD: %s", cmd));
            executeCmd(cmd);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aenderungsanfrage  kassenzeichenNummer DOCUMENT ME!
     * @param   code               DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean sendVerificationMail(final AenderungsanfrageJson aenderungsanfrage, final String code) {
        if ((aenderungsanfrage != null) && (code != null)) {
            final Integer kassenzeichenNummer = aenderungsanfrage.getKassenzeichen();
            final String emailAdresse = aenderungsanfrage.getEmailAdresse();
            if (emailAdresse != null) {
                try {
                    final AenderungsanfrageConf conf = getConfFromServerResource();
                    final File configDir = (conf.getMessageconfigDir() != null) ? new File(conf.getMessageconfigDir())
                                                                                : null;
                    final MessageConfigJson messageConfig = getMessageConfig(MESSAGETYPE_MAILVERIFICATION, configDir);
                    if (messageConfig != null) {
                        final String cmdTemplate = messageConfig.getCmdTemplate();
                        final String betreff = messageConfig.getTopic();
                        final String messageTemplate = (messageConfig.getMessageTemplateFile() != null)
                            ? readMessageTemplate(new File(configDir, messageConfig.getMessageTemplateFile())) : null;
                        if (messageTemplate != null) {
                            final String inhalt = messageTemplate.replaceAll(Pattern.quote(
                                            TEMPLATEREPLACER_KASSENZEICHEN),
                                        Matcher.quoteReplacement(
                                            (kassenzeichenNummer != null) ? kassenzeichenNummer.toString() : "-"))
                                        .replaceAll(Pattern.quote(TEMPLATEREPLACER_CODE),
                                            Matcher.quoteReplacement(code));
                            sendMail((cmdTemplate != null) ? cmdTemplate : DEFAULT_CMDTEMPLATE,
                                emailAdresse,
                                betreff,
                                inhalt);
                            return true;
                        }
                    }
                } catch (final Exception ex) {
                    LOG.error("error while sendVerificationMail", ex);
                }
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aenderungsanfrage  kassenzeichenNummer DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean sendConfirmationMail(final AenderungsanfrageJson aenderungsanfrage) {
        if (aenderungsanfrage != null) {
            final Integer kassenzeichenNummer = aenderungsanfrage.getKassenzeichen();
            final String emailAdresse = aenderungsanfrage.getEmailAdresse();
            if (emailAdresse != null) {
                try {
                    final AenderungsanfrageConf conf = getConfFromServerResource();
                    final File configDir = (conf.getMessageconfigDir() != null) ? new File(conf.getMessageconfigDir())
                                                                                : null;
                    final MessageConfigJson messageConfig = getMessageConfig(MESSAGETYPE_MAILCONFIRMATION, configDir);
                    if (messageConfig != null) {
                        final String cmdTemplate = messageConfig.getCmdTemplate();
                        final String betreff = messageConfig.getTopic();
                        final String messageTemplate = (messageConfig.getMessageTemplateFile() != null)
                            ? readMessageTemplate(new File(configDir, messageConfig.getMessageTemplateFile())) : null;
                        if (messageTemplate != null) {
                            final String inhalt = messageTemplate.replaceAll(Pattern.quote(
                                        TEMPLATEREPLACER_KASSENZEICHEN),
                                    Matcher.quoteReplacement(
                                        (kassenzeichenNummer != null) ? kassenzeichenNummer.toString() : "-"));
                            sendMail((cmdTemplate != null) ? cmdTemplate : DEFAULT_CMDTEMPLATE,
                                emailAdresse,
                                betreff,
                                inhalt);
                            return true;
                        }
                    }
                } catch (final Exception ex) {
                    LOG.error("error while sendConfirmationMail", ex);
                }
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   messageTemplateFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private String readMessageTemplate(final File messageTemplateFile) throws Exception {
        final String messageTemplate =
            ((messageTemplateFile != null) && messageTemplateFile.exists() && messageTemplateFile.isFile()
                        && messageTemplateFile.canRead()) ? IOUtils.toString(new FileReader(messageTemplateFile))
                                                          : null;
        return messageTemplate;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   messageType  DOCUMENT ME!
     * @param   configDir    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private MessageConfigJson getMessageConfig(final String messageType, final File configDir) {
        try {
            final File configFile = (configDir != null)
                ? new File(configDir, String.format(CONFIG_JSON_FORMAT, messageType)) : null;
            final boolean configFileOk = (configFile != null) && configFile.exists() && configFile.isFile()
                        && configFile.canRead();
            final String configJson = configFileOk ? IOUtils.toString(new FileReader(configFile)) : null;
            return (configJson != null) ? getMapper().readValue(configJson, MessageConfigJson.class) : null;
        } catch (final Exception ex) {
            LOG.error(String.format("error while loading config file for %s, ", messageType), ex);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aenderungsanfrage  kassenzeichenNummer DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean sendNotifyMail(final AenderungsanfrageJson aenderungsanfrage) {
        if (aenderungsanfrage != null) {
            final Integer kassenzeichenNummer = aenderungsanfrage.getKassenzeichen();
            final String emailAdresse = aenderungsanfrage.getEmailAdresse();
            final Boolean verifiziert = aenderungsanfrage.getEmailVerifiziert();
            if ((emailAdresse != null) && Boolean.TRUE.equals(verifiziert)) {
                try {
                    final AenderungsanfrageConf conf = getConfFromServerResource();
                    final File configDir = (conf.getMessageconfigDir() != null) ? new File(conf.getMessageconfigDir())
                                                                                : null;
                    final MessageConfigJson messageConfig = getMessageConfig(MESSAGETYPE_NOTIFY, configDir);
                    if (messageConfig != null) {
                        final String cmdTemplate = messageConfig.getCmdTemplate();
                        final String betreff = messageConfig.getTopic();
                        final String messageTemplate = (messageConfig.getMessageTemplateFile() != null)
                            ? readMessageTemplate(new File(configDir, messageConfig.getMessageTemplateFile())) : null;
                        if (messageTemplate != null) {
                            final String inhalt = messageTemplate.replaceAll(Pattern.quote(
                                        TEMPLATEREPLACER_KASSENZEICHEN),
                                    Matcher.quoteReplacement(
                                        (kassenzeichenNummer != null) ? kassenzeichenNummer.toString() : "-"));
                            sendMail((cmdTemplate != null) ? cmdTemplate : DEFAULT_CMDTEMPLATE,
                                emailAdresse,
                                betreff,
                                inhalt);
                            return true;
                        }
                    }
                } catch (final Exception ex) {
                    LOG.error("error while sendNotifyMail", ex);
                }
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aenderungsanfrage  DOCUMENT ME!
     * @param   status             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean sendStatusChangedMail(final AenderungsanfrageJson aenderungsanfrage, final Status status) {
        if (aenderungsanfrage != null) {
            final Integer kassenzeichenNummer = aenderungsanfrage.getKassenzeichen();
            final String emailAdresse = aenderungsanfrage.getEmailAdresse();
            final Boolean verifiziert = aenderungsanfrage.getEmailVerifiziert();
            if ((emailAdresse != null) && Boolean.TRUE.equals(verifiziert)) {
                try {
                    final AenderungsanfrageConf conf = getConfFromServerResource();
                    final File configDir = (conf.getMessageconfigDir() != null) ? new File(conf.getMessageconfigDir())
                                                                                : null;
                    final MessageConfigJson messageConfig = getMessageConfig(status.toString(), configDir);
                    if (messageConfig != null) {
                        final String cmdTemplate = messageConfig.getCmdTemplate();
                        final String betreff = messageConfig.getTopic();
                        final String messageTemplate = (messageConfig.getMessageTemplateFile() != null)
                            ? readMessageTemplate(new File(configDir, messageConfig.getMessageTemplateFile())) : null;
                        if (messageTemplate != null) {
                            final String inhalt = messageTemplate.replaceAll(Pattern.quote(
                                        TEMPLATEREPLACER_KASSENZEICHEN),
                                    Matcher.quoteReplacement(
                                        (kassenzeichenNummer != null) ? kassenzeichenNummer.toString() : "-"));
                            sendMail((cmdTemplate != null) ? cmdTemplate : DEFAULT_CMDTEMPLATE,
                                emailAdresse,
                                betreff,
                                inhalt);
                            return true;
                        }
                    }
                } catch (final Exception ex) {
                    LOG.error("error while sendStatusChangedMail", ex);
                }
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aenderungsanfrage  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean sendSubmissionMail(final AenderungsanfrageJson aenderungsanfrage) {
        if (aenderungsanfrage != null) {
            final Integer kassenzeichenNummer = aenderungsanfrage.getKassenzeichen();
            final String emailAdresse = aenderungsanfrage.getEmailAdresse();
            final Boolean verifiziert = aenderungsanfrage.getEmailVerifiziert();
            if ((emailAdresse != null) && Boolean.TRUE.equals(verifiziert)) {
                try {
                    final AenderungsanfrageConf conf = getConfFromServerResource();
                    final File configDir = (conf.getMessageconfigDir() != null) ? new File(conf.getMessageconfigDir())
                                                                                : null;
                    final MessageConfigJson messageConfig = getMessageConfig(MESSAGETYPE_SUBMISSION, configDir);
                    if (messageConfig != null) {
                        final String cmdTemplate = messageConfig.getCmdTemplate();
                        final String betreff = messageConfig.getTopic();
                        final String messageTemplate = (messageConfig.getMessageTemplateFile() != null)
                            ? readMessageTemplate(new File(configDir, messageConfig.getMessageTemplateFile())) : null;
                        if (messageTemplate != null) {
                            final String inhalt = messageTemplate.replaceAll(Pattern.quote(
                                        TEMPLATEREPLACER_KASSENZEICHEN),
                                    Matcher.quoteReplacement(
                                        (kassenzeichenNummer != null) ? kassenzeichenNummer.toString() : "-"));
                            sendMail((cmdTemplate != null) ? cmdTemplate : DEFAULT_CMDTEMPLATE,
                                emailAdresse,
                                betreff,
                                inhalt);
                            return true;
                        }
                    }
                } catch (final Exception ex) {
                    LOG.error("error while sendSubmissionMail", ex);
                }
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stacEntry           DOCUMENT ME!
     * @param   kassenzeichennumer  DOCUMENT ME!
     * @param   existingFlaechen    DOCUMENT ME!
     * @param   anfrageOrig         DOCUMENT ME!
     * @param   anfrageNew          DOCUMENT ME!
     * @param   citizenOrClerk      DOCUMENT ME!
     * @param   veranlagt           DOCUMENT ME!
     * @param   username            DOCUMENT ME!
     * @param   timestamp           DOCUMENT ME!
     * @param   metaService         DOCUMENT ME!
     * @param   connectionContext   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageJson doProcessing(
            final StacEntry stacEntry,
            final Integer kassenzeichennumer,
            final Map<String, CidsBean> existingFlaechen,
            final AenderungsanfrageJson anfrageOrig,
            final AenderungsanfrageJson anfrageNew,
            final Boolean citizenOrClerk,
            final Boolean veranlagt,
            final String username,
            final Date timestamp,
            final MetaService metaService,
            final ConnectionContext connectionContext) throws Exception {
        Boolean emailVerifiziert = anfrageOrig.getEmailVerifiziert();
        String emailAdresse = anfrageOrig.getEmailAdresse();

        // process email change
        if (Boolean.TRUE.equals(citizenOrClerk) && (anfrageNew.getEmailVerifiziert() == null)) {
            emailAdresse = anfrageNew.getEmailAdresse();
            if (emailAdresse != null) {
                emailVerifiziert = false;
                final String code = addEmailVerification(kassenzeichennumer, emailAdresse);
                sendVerificationMail(anfrageNew, code);
            } else {
                emailVerifiziert = null;
                removeEmail(kassenzeichennumer, emailAdresse);
            }
        }

        // process email verification
        final String emailVerifikation = anfrageNew.getEmailVerifikation();
        if (Boolean.TRUE.equals(citizenOrClerk) && (emailAdresse != null) && (emailVerifikation != null)
                    && !Boolean.TRUE.equals(emailVerifiziert)) {
            final EmailVerification emailVerification = getEmailVerification(kassenzeichennumer);
            if (emailVerification != null) {
                emailVerifiziert = emailVerifikation.equals(emailVerification.getCode());
                if (emailVerifiziert) {
                    LOG.info(String.format(
                            "validation of %s with code %s SUCCESFULL",
                            emailAdresse,
                            emailVerifikation));
                    sendConfirmationMail(anfrageNew);
                } else {
                    LOG.info(String.format("validation of %s with code %s FAILED", emailAdresse, emailVerifikation));
                }
            } else {
                // emailAdresse = null; // wirklich mail "vergessen" wenn Server neugestartet wurde
                emailVerifiziert = null;
            }
        }

        final AenderungsanfrageJson anfrageProcessed = new AenderungsanfrageJson(
                kassenzeichennumer,
                emailAdresse,
                null,
                emailVerifiziert,
                processFlaechenUndPruefung(
                    existingFlaechen,
                    anfrageOrig.getFlaechen(),
                    anfrageNew.getFlaechen(),
                    citizenOrClerk,
                    veranlagt,
                    username,
                    timestamp),
                processAnmerkungen(
                    anfrageOrig.getGeometrien(),
                    anfrageNew.getGeometrien(),
                    citizenOrClerk,
                    username,
                    timestamp),
                processNachrichten(
                    anfrageOrig.getNachrichten(),
                    anfrageNew.getNachrichten(),
                    citizenOrClerk,
                    username,
                    timestamp),
                null);

        if (anfrageProcessed.getNachrichten() != null) {
            boolean notified = false;
            for (final NachrichtJson nachrichtJson : anfrageProcessed.getNachrichten()) {
                if (nachrichtJson != null) {
                    final NachrichtParameterJson nachrichtenParameter = nachrichtJson.getNachrichtenParameter();
                    if ((nachrichtenParameter != null)
                                && Boolean.FALSE.equals(nachrichtenParameter.getBenachrichtigt())) {
                        if (!notified) {
                            notified = sendNotifyMail(anfrageNew);
                        }
                        if (notified) {
                            nachrichtenParameter.setBenachrichtigt(Boolean.TRUE);
                        }
                    }
                    if ((nachrichtenParameter != null) && Boolean.FALSE.equals(nachrichtenParameter.getVerlaengert())) {
                        StacUtils.prolongExpiration(stacEntry, metaService, connectionContext);
                        nachrichtenParameter.setVerlaengert(Boolean.TRUE);
                    }
                }
            }
        }
        return anfrageProcessed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   oldStatus                DOCUMENT ME!
     * @param   existingFlaechen         DOCUMENT ME!
     * @param   aenderungsanfrageBefore  DOCUMENT ME!
     * @param   aenderungsanfrageAfter   DOCUMENT ME!
     * @param   citizenOrClerk           DOCUMENT ME!
     * @param   veranlagt                DOCUMENT ME!
     * @param   username                 DOCUMENT ME!
     * @param   timestamp                DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Status identifyNewStatus(final Status oldStatus,
            final Map<String, CidsBean> existingFlaechen,
            final AenderungsanfrageJson aenderungsanfrageBefore,
            final AenderungsanfrageJson aenderungsanfrageAfter,
            final Boolean citizenOrClerk,
            final Boolean veranlagt,
            final String username,
            final Date timestamp) throws Exception {
        final boolean isCitizen = Boolean.TRUE.equals(citizenOrClerk);
        final boolean isClerk = Boolean.FALSE.equals(citizenOrClerk);
        final boolean isArchived = Status.ARCHIVED.equals(oldStatus);

        final HashMap<String, NachrichtJson> nachrichtenPerUUid = new HashMap<>();
        if (aenderungsanfrageBefore.getNachrichten() != null) {
            for (final NachrichtJson nachricht : aenderungsanfrageBefore.getNachrichten()) {
                if ((nachricht != null) && (nachricht.getIdentifier() != null)) {
                    nachrichtenPerUUid.put(nachricht.getIdentifier(), nachricht);
                }
            }
        }

        if (isCitizen && !isArchived) {
            boolean newMessage = false;

            if (aenderungsanfrageAfter.getNachrichten() != null) {
                for (final NachrichtJson nachricht : aenderungsanfrageAfter.getNachrichten()) {
                    if ((nachricht != null) && NachrichtJson.Typ.CITIZEN.equals(nachricht.getTyp())) {
                        final String identifier = nachricht.getIdentifier();
                        final NachrichtJson nachrichtBefore =
                            ((identifier != null) && nachrichtenPerUUid.containsKey(identifier))
                            ? nachrichtenPerUUid.get(identifier) : null;
                        if (!Boolean.TRUE.equals(nachricht.getDraft())
                                    && ((nachrichtBefore == null) || Boolean.TRUE.equals(nachrichtBefore.getDraft()))) {
                            newMessage = true;
                            break;
                        }
                    }
                }
            }

            boolean anyChanges = false;
            if (aenderungsanfrageAfter.getFlaechen() != null) {
                for (final String bezeichnung : aenderungsanfrageAfter.getFlaechen().keySet()) {
                    final FlaecheAenderungJson flaecheAenderungBefore = aenderungsanfrageBefore.getFlaechen()
                                .get(bezeichnung);
                    final FlaecheAenderungJson flaecheAenderungAfter = aenderungsanfrageAfter.getFlaechen()
                                .get(bezeichnung);

                    final FlaecheAenderungJson flaecheAenderungBeforeNoDraft =
                        ((flaecheAenderungBefore != null) && !Boolean.TRUE.equals(flaecheAenderungBefore.getDraft()))
                        ? flaecheAenderungBefore : null;
                    final FlaecheAenderungJson flaecheAenderungAfterNoDraft =
                        ((flaecheAenderungAfter != null) && !Boolean.TRUE.equals(flaecheAenderungAfter.getDraft()))
                        ? flaecheAenderungAfter : null;

                    final Integer groesseBefore = (flaecheAenderungBeforeNoDraft != null)
                        ? flaecheAenderungBeforeNoDraft.getGroesse() : null;
                    final FlaecheAnschlussgradJson anschlussgradBefore = (flaecheAenderungBeforeNoDraft != null)
                        ? flaecheAenderungBeforeNoDraft.getAnschlussgrad() : null;
                    final FlaecheFlaechenartJson flaechenartBefore = (flaecheAenderungBeforeNoDraft != null)
                        ? flaecheAenderungBeforeNoDraft.getFlaechenart() : null;

                    final Integer groesseAfter = (flaecheAenderungAfterNoDraft != null)
                        ? flaecheAenderungAfterNoDraft.getGroesse() : null;
                    final FlaecheAnschlussgradJson anschlussgradAfter = (flaecheAenderungAfterNoDraft != null)
                        ? flaecheAenderungAfterNoDraft.getAnschlussgrad() : null;
                    final FlaecheFlaechenartJson flaechenartAfter = (flaecheAenderungAfterNoDraft != null)
                        ? flaecheAenderungAfterNoDraft.getFlaechenart() : null;

                    if (!Objects.equals(groesseBefore, groesseAfter)
                                || !Objects.equals(anschlussgradBefore, anschlussgradAfter)
                                || !Objects.equals(flaechenartBefore, flaechenartAfter)) {
                        anyChanges = true;
                        break;
                    }
                }
            }

            if (aenderungsanfrageAfter.getGeometrien() != null) {
                for (final String bezeichnung : aenderungsanfrageAfter.getGeometrien().keySet()) {
                    final org.geojson.Feature anmerkungBefore = (org.geojson.Feature)
                        aenderungsanfrageBefore.getGeometrien().get(bezeichnung);
                    final org.geojson.Feature anmerkungAfter = (org.geojson.Feature)
                        aenderungsanfrageAfter.getGeometrien().get(bezeichnung);

                    final org.geojson.Feature anmerkungBeforeNoDraft =
                        ((anmerkungBefore != null) && !Boolean.TRUE.equals(anmerkungBefore.getProperty("draft")))
                        ? anmerkungBefore : null;
                    final org.geojson.Feature anmerkungAfterNoDraft =
                        ((anmerkungAfter != null) && !Boolean.TRUE.equals(anmerkungAfter.getProperty("draft")))
                        ? anmerkungAfter : null;

                    final Feature anmerkungBeforeWithoutPruefung;
                    if (anmerkungBeforeNoDraft != null) {
                        anmerkungBeforeWithoutPruefung = new Feature();
                        anmerkungBeforeWithoutPruefung.setId(anmerkungBeforeNoDraft.getId());
                        anmerkungBeforeWithoutPruefung.setGeometry(anmerkungBeforeNoDraft.getGeometry());
                        anmerkungBeforeWithoutPruefung.setProperties(anmerkungBeforeNoDraft.getProperties());
                        if (anmerkungBeforeWithoutPruefung.getProperties() != null) {
                            anmerkungBeforeWithoutPruefung.getProperties().remove("pruefung");
                            anmerkungBeforeWithoutPruefung.getProperties().remove("pruefungVon");
                            anmerkungBeforeWithoutPruefung.getProperties().remove("pruefungTimestamp");
                        }
                    } else {
                        anmerkungBeforeWithoutPruefung = null;
                    }

                    final String anmerkungBeforeString = (anmerkungBeforeWithoutPruefung != null)
                        ? new ObjectMapper().writeValueAsString(anmerkungBeforeWithoutPruefung) : null;
                    final String anmerkungAfterString = (anmerkungAfterNoDraft != null)
                        ? new ObjectMapper().writeValueAsString(anmerkungAfterNoDraft) : null;

                    if ((anmerkungBeforeWithoutPruefung == null)
                                || !Objects.equals(anmerkungBeforeString, anmerkungAfterString)) {
                        anyChanges = true;
                        break;
                    }
                }
            }

            if (anyChanges) {
                return Status.PENDING;
            } else if (newMessage) {
                return Status.NEW_CITIZEN_MESSAGE;
            }
        } else if (isClerk) {
            boolean prolong = false;
            if (aenderungsanfrageAfter.getNachrichten() != null) {
                for (final NachrichtJson nachricht : aenderungsanfrageAfter.getNachrichten()) {
                    if ((nachricht != null) && NachrichtJson.Typ.SYSTEM.equals(nachricht.getTyp())
                                && Boolean.TRUE.equals(nachricht.getNachrichtenParameter().getVerlaengert())) {
                        final String identifier = nachricht.getIdentifier();
                        final NachrichtJson nachrichtBefore =
                            ((identifier != null) && nachrichtenPerUUid.containsKey(identifier))
                            ? nachrichtenPerUUid.get(identifier) : null;
                        if ((nachrichtBefore == null)
                                    || ((nachrichtBefore.getNachrichtenParameter() != null)
                                        && !Boolean.TRUE.equals(
                                            nachrichtBefore.getNachrichtenParameter().getVerlaengert()))) {
                            prolong = true;
                            break;
                        }
                    }
                }
            }
            if (((veranlagt != null) && !isArchived) || prolong) {
                if (aenderungsanfrageBefore.getFlaechen().size() < aenderungsanfrageAfter.getFlaechen().size()) {
                    throw new Exception("flaeche added. clerk is not allowed to add flaeche");
                }

                int pruefungDoneCount = 0;
                int aenderungCount = 0;

                for (final String bezeichnung : aenderungsanfrageBefore.getFlaechen().keySet()) {
                    final FlaecheAenderungJson flaecheAenderungBefore = aenderungsanfrageBefore.getFlaechen()
                                .get(bezeichnung);
                    final FlaecheAenderungJson flaecheAenderungAfter = aenderungsanfrageAfter.getFlaechen()
                                .get(bezeichnung);
                    if (existingFlaechen.containsKey(bezeichnung) && (flaecheAenderungAfter == null)) {
                        throw new Exception("flaeche disappeared. clerk is not allowed to delete flaeche");
                    }

                    if (flaecheAenderungAfter == null) {
                        continue;
                    }

                    // Werte before
                    final Integer groesseBefore = flaecheAenderungBefore.getGroesse();
                    final FlaecheAnschlussgradJson anschlussgradBefore = flaecheAenderungBefore.getAnschlussgrad();
                    final FlaecheFlaechenartJson flaechenartBefore = flaecheAenderungBefore.getFlaechenart();
                    final Integer pruefungGroesseBefore =
                        ((flaecheAenderungBefore.getPruefung() != null)
                                    && (flaecheAenderungBefore.getPruefung().getGroesse() != null)
                                    && !Boolean.TRUE.equals(
                                        flaecheAenderungBefore.getPruefung().getGroesse().getPending()))
                        ? flaecheAenderungBefore.getPruefung().getGroesse().getValue() : null;
                    final FlaecheAnschlussgradJson pruefungAnschlussgradBefore =
                        ((flaecheAenderungBefore.getPruefung() != null)
                                    && (flaecheAenderungBefore.getPruefung().getAnschlussgrad() != null)
                                    && !Boolean.TRUE.equals(
                                        flaecheAenderungBefore.getPruefung().getAnschlussgrad().getPending()))
                        ? flaecheAenderungBefore.getPruefung().getAnschlussgrad().getValue() : null;
                    final FlaecheFlaechenartJson pruefungflaechenartBefore =
                        ((flaecheAenderungBefore.getPruefung() != null)
                                    && (flaecheAenderungBefore.getPruefung().getFlaechenart() != null)
                                    && !Boolean.TRUE.equals(
                                        flaecheAenderungBefore.getPruefung().getFlaechenart().getPending()))
                        ? flaecheAenderungBefore.getPruefung().getFlaechenart().getValue() : null;

                    // Werte after
                    final Integer groesseAfter = flaecheAenderungAfter.getGroesse();
                    final FlaecheAnschlussgradJson anschlussgradAfter = flaecheAenderungAfter.getAnschlussgrad();
                    final FlaecheFlaechenartJson flaechenartAfter = flaecheAenderungAfter.getFlaechenart();
                    final Integer pruefungGroesseAfter =
                        ((flaecheAenderungAfter.getPruefung() != null)
                                    && (flaecheAenderungAfter.getPruefung().getGroesse() != null)
                                    && !Boolean.TRUE.equals(
                                        flaecheAenderungAfter.getPruefung().getGroesse().getPending()))
                        ? flaecheAenderungAfter.getPruefung().getGroesse().getValue() : null;
                    final FlaecheAnschlussgradJson pruefungAnschlussgradAfter =
                        ((flaecheAenderungAfter.getPruefung() != null)
                                    && (flaecheAenderungAfter.getPruefung().getAnschlussgrad() != null)
                                    && !Boolean.TRUE.equals(
                                        flaecheAenderungAfter.getPruefung().getAnschlussgrad().getPending()))
                        ? flaecheAenderungAfter.getPruefung().getAnschlussgrad().getValue() : null;
                    final FlaecheFlaechenartJson pruefungFlaechenartAfter =
                        ((flaecheAenderungAfter.getPruefung() != null)
                                    && (flaecheAenderungAfter.getPruefung().getFlaechenart() != null)
                                    && !Boolean.TRUE.equals(
                                        flaecheAenderungAfter.getPruefung().getFlaechenart().getPending()))
                        ? flaecheAenderungAfter.getPruefung().getFlaechenart().getValue() : null;

                    // Sachbearbeiter darf keine Werte verändern
                    if (!Objects.equals(groesseBefore, groesseAfter)
                                || !Objects.equals(anschlussgradBefore, anschlussgradAfter)
                                || !Objects.equals(flaechenartBefore, flaechenartAfter)) {
                        throw new Exception(
                            "groesse, anschlussgrad or flachenart request did change. clerk is not allowed to do this");
                    }

                    // Änderungen zählen die keine Drafts sind
                    if (!Boolean.TRUE.equals(flaecheAenderungBefore.getDraft())) {
                        if (flaecheAenderungBefore.getGroesse() != null) {
                            aenderungCount++;
                        }
                        if (flaecheAenderungBefore.getAnschlussgrad() != null) {
                            aenderungCount++;
                        }
                        if (flaecheAenderungBefore.getFlaechenart() != null) {
                            aenderungCount++;
                        }
                    }

                    // Änderungen zählen die geprüft wurden oder werden
                    if (flaecheAenderungAfter.getPruefung() != null) {
                        if (flaecheAenderungAfter.getPruefung().getGroesse() != null) {
                            pruefungDoneCount++;
                        }
                        if (flaecheAenderungAfter.getPruefung().getAnschlussgrad() != null) {
                            pruefungDoneCount++;
                        }
                        if (flaecheAenderungAfter.getPruefung().getFlaechenart() != null) {
                            pruefungDoneCount++;
                        }
                    }

                    final boolean isGroesseAcceptedOrRejected = (groesseBefore != null)
                                && (pruefungGroesseAfter != null);
                    final boolean isAnschlussgradAcceptedOrRejected = (anschlussgradBefore != null)
                                && (pruefungAnschlussgradAfter != null);
                    final boolean isFlaechenartAcceptedOrRejected = (flaechenartBefore != null)
                                && (pruefungFlaechenartAfter != null);

                    if (isGroesseAcceptedOrRejected && !Objects.equals(pruefungGroesseBefore, pruefungGroesseAfter)) {
                        aenderungsanfrageAfter.getNachrichten()
                                .add(new NachrichtSystemJson(
                                        createIdentifier(null),
                                        timestamp,
                                        null,
                                        new NachrichtParameterGroesseJson(
                                            Objects.equals(groesseBefore, pruefungGroesseAfter)
                                                ? NachrichtParameterJson.Type.CHANGED
                                                : NachrichtParameterJson.Type.REJECTED,
                                            bezeichnung,
                                            groesseBefore),
                                        username));
                    }
                    if (isAnschlussgradAcceptedOrRejected
                                && !Objects.equals(pruefungAnschlussgradBefore, pruefungAnschlussgradAfter)) {
                        aenderungsanfrageAfter.getNachrichten()
                                .add(new NachrichtSystemJson(
                                        createIdentifier(null),
                                        timestamp,
                                        null,
                                        new NachrichtParameterAnschlussgradJson(
                                            Objects.equals(anschlussgradBefore, pruefungAnschlussgradAfter)
                                                ? NachrichtParameterJson.Type.CHANGED
                                                : NachrichtParameterJson.Type.REJECTED,
                                            bezeichnung,
                                            anschlussgradBefore),
                                        username));
                    }
                    if (isFlaechenartAcceptedOrRejected
                                && !Objects.equals(pruefungflaechenartBefore, pruefungFlaechenartAfter)) {
                        aenderungsanfrageAfter.getNachrichten()
                                .add(new NachrichtSystemJson(
                                        createIdentifier(null),
                                        timestamp,
                                        null,
                                        new NachrichtParameterFlaechenartJson(
                                            Objects.equals(flaechenartBefore, pruefungFlaechenartAfter)
                                                ? NachrichtParameterJson.Type.CHANGED
                                                : NachrichtParameterJson.Type.REJECTED,
                                            bezeichnung,
                                            flaechenartBefore),
                                        username));
                    }
                }

                for (final String bezeichnung : aenderungsanfrageAfter.getGeometrien().keySet()) {
                    final org.geojson.Feature anmerkungAfter = (org.geojson.Feature)
                        aenderungsanfrageAfter.getGeometrien().get(bezeichnung);

                    if ((anmerkungAfter != null) && !Boolean.TRUE.equals(anmerkungAfter.getProperty("draft"))) {
                        aenderungCount++;

                        final Boolean pruefungAfter = (Boolean)anmerkungAfter.getProperty("pruefung");
                        if (pruefungAfter != null) {
                            pruefungDoneCount++;
                        }
                    }
                }

                if (pruefungDoneCount == aenderungCount) {
                    if (Boolean.TRUE.equals(veranlagt) || prolong) {
                        return Status.NONE;
                    }
                } else {
                    return Status.PROCESSING;
                }
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   oldStatus               DOCUMENT ME!
     * @param   changeStatusTo          DOCUMENT ME!
     * @param   aenderungsanfrageAfter  DOCUMENT ME!
     * @param   timestamp               DOCUMENT ME!
     * @param   username                DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void addStatusChangedSystemMessage(final Status oldStatus,
            final Status changeStatusTo,
            final AenderungsanfrageJson aenderungsanfrageAfter,
            final Date timestamp,
            final String username) throws Exception {
        final boolean statusChanged = (changeStatusTo != null) && !changeStatusTo.equals(oldStatus);
        if (statusChanged
                    && ((Status.NEW_CITIZEN_MESSAGE != changeStatusTo) && (Status.NEW_CITIZEN_MESSAGE != oldStatus))) {
            aenderungsanfrageAfter.getNachrichten()
                    .add(new NachrichtSystemJson(
                            createIdentifier(null),
                            Status.PROCESSING.equals(changeStatusTo)
                                ? new Date(timestamp.getTime() - 1)
                                : (Status.NONE.equals(changeStatusTo) ? new Date(timestamp.getTime() + 1) : timestamp), // assuring that the processing message comes first and the done status last
                            null,
                            new NachrichtParameterStatusJson(changeStatusTo),
                            username));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aenderungsanfrage  DOCUMENT ME!
     * @param   citizenOrClerk     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageJson doFilteringOutWhatIShouldntSee(final AenderungsanfrageJson aenderungsanfrage,
            final boolean citizenOrClerk) throws Exception {
        final boolean isCitizen = Boolean.TRUE.equals(citizenOrClerk);
        final boolean isClerk = Boolean.FALSE.equals(citizenOrClerk);

        if (aenderungsanfrage == null) {
            return null;
        } else {
            final AenderungsanfrageJson aenderungsanfrageFiltered = new AenderungsanfrageJson(
                    aenderungsanfrage.getKassenzeichen(),
                    aenderungsanfrage.getEmailAdresse(),
                    aenderungsanfrage.getEmailVerifiziert());

            // Nachrichten filtern
            if (aenderungsanfrage.getNachrichten() != null) {
                for (final NachrichtJson nachricht : aenderungsanfrage.getNachrichten()) {
                    if (nachricht != null) {
                        // kopieren
                        final NachrichtJson nachrichtFiltered = getMapper().readValue(nachricht.toJson(),
                                NachrichtJson.class);

                        final boolean isDraft = Boolean.TRUE.equals(nachricht.getDraft());
                        final boolean isCitizenMessage = NachrichtJson.Typ.CITIZEN.equals(nachricht.getTyp());
                        final boolean isClerkMessage = NachrichtJson.Typ.CLERK.equals(nachricht.getTyp());
                        final boolean isOwnMessage = isCitizen ? isCitizenMessage : (isClerk ? isClerkMessage : false);

                        // nur eigene oder nicht gedraftete Nachrichten raus geben
                        final boolean include = isOwnMessage || !isDraft;
                        if (include) {
                            if (isCitizen) {
                                nachrichtFiltered.setAbsender(null); // Absender anonymisieren
                            }
                            aenderungsanfrageFiltered.getNachrichten().add(nachrichtFiltered);
                        }
                    }
                }
            }

            // Flächenänderungen filtern
            if (aenderungsanfrage.getFlaechen() != null) {
                for (final String bezeichnung : aenderungsanfrage.getFlaechen().keySet()) {
                    final FlaecheAenderungJson flaeche = aenderungsanfrage.getFlaechen().get(bezeichnung);
                    if (flaeche != null) {
                        // kopieren
                        final FlaecheAenderungJson flaecheFiltered = getMapper().readValue(flaeche.toJson(),
                                FlaecheAenderungJson.class);

                        final boolean isDraft = Boolean.TRUE.equals(flaeche.getDraft());

                        // Eigentümer sieht alle seine Änderungswünsche
                        // Sachbearbeiter sieht nur ungedraftete
                        final boolean include = isCitizen || (isClerk && !isDraft);
                        if (include) {
                            if (isCitizen) {
                                // pending prüfungen raus nehmen
                                // und prüfer ("von") anonymisieren
                                if (flaecheFiltered.getPruefung() != null) {
                                    if (flaecheFiltered.getPruefung().getGroesse() != null) {
                                        flaecheFiltered.getPruefung().getGroesse().setVon(null);
                                        if (Boolean.TRUE.equals(
                                                        flaecheFiltered.getPruefung().getGroesse().getPending())) {
                                            flaecheFiltered.getPruefung().setGroesse(null);
                                        }
                                    }
                                    if (flaecheFiltered.getPruefung().getFlaechenart() != null) {
                                        flaecheFiltered.getPruefung().getFlaechenart().setVon(null);
                                        if (Boolean.TRUE.equals(
                                                        flaecheFiltered.getPruefung().getFlaechenart().getPending())) {
                                            flaecheFiltered.getPruefung().setFlaechenart(null);
                                        }
                                    }
                                    if (flaecheFiltered.getPruefung().getAnschlussgrad() != null) {
                                        flaecheFiltered.getPruefung().getAnschlussgrad().setVon(null);
                                        if (Boolean.TRUE.equals(
                                                        flaecheFiltered.getPruefung().getAnschlussgrad().getPending())) {
                                            flaecheFiltered.getPruefung().setAnschlussgrad(null);
                                        }
                                    }
                                    // wenn alle Prüfungen null sind, kann das
                                    // Prüfungsobjekt komplett raus fallen.
                                    if ((flaecheFiltered.getPruefung().getGroesse() == null)
                                                && (flaecheFiltered.getPruefung().getFlaechenart() == null)
                                                && (flaecheFiltered.getPruefung().getAnschlussgrad() == null)) {
                                        flaecheFiltered.setPruefung(null);
                                    }
                                }
                            }
                            aenderungsanfrageFiltered.getFlaechen().put(bezeichnung, flaecheFiltered);
                        }
                    }
                }
            }

            // Anmerkungen filtern
            if (aenderungsanfrage.getGeometrien() != null) {
                for (final String bezeichnung : aenderungsanfrage.getGeometrien().keySet()) {
                    final org.geojson.Feature anmerkung = (org.geojson.Feature)aenderungsanfrage.getGeometrien()
                                .get(bezeichnung);
                    if (anmerkung != null) {
                        // kopieren
                        final org.geojson.Feature anmerkungFitlered = getMapper().readValue(getMapper()
                                        .writeValueAsString(anmerkung),
                                org.geojson.Feature.class);

                        final boolean isDraft = Boolean.TRUE.equals(anmerkung.getProperty("draft"));

                        // Eigentümer sieht alle seine Anmerkungen
                        // Sachbearbeiter sieht nur ungedraftete
                        final boolean include = isCitizen || !isDraft;
                        if (include) {
                            if (isCitizen && (anmerkungFitlered.getProperties() != null)) {
                                anmerkungFitlered.getProperties().remove("pruefungTimestamp");
                            }
                            aenderungsanfrageFiltered.getGeometrien().put(bezeichnung, anmerkungFitlered);
                        }
                    }
                }
            }

            return aenderungsanfrageFiltered;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   search       DOCUMENT ME!
     * @param   user         DOCUMENT ME!
     * @param   metaService  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SearchException  DOCUMENT ME!
     */
    private static Collection execSearch(final CidsServerSearch search, final User user, final MetaService metaService)
            throws SearchException {
        final Map localServers = new HashMap<>();
        localServers.put(VerdisConstants.DOMAIN, metaService);
        search.setActiveLocalServers(localServers);
        search.setUser(user);
        return search.performServerSearch();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stacEntry          DOCUMENT ME!
     * @param   metaService        DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public CidsBean getAenderungsanfrageBean(final StacEntry stacEntry,
            final MetaService metaService,
            final ConnectionContext connectionContext) throws Exception {
        if (stacEntry != null) {
            final User user = getUser(stacEntry, metaService, connectionContext);

            final AenderungsanfrageSearchStatement search = new AenderungsanfrageSearchStatement();
            search.setStacId(stacEntry.getId());
            final Collection<MetaObjectNode> mons = execSearch(search, user, metaService);
            for (final MetaObjectNode mon : mons) {
                if (mon != null) {
                    return metaService.getMetaObject(
                                user,
                                mon.getObjectId(),
                                mon.getClassId(),
                                connectionContext).getBean();
                }
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   map  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageJson createAenderungsanfrageJson(final Map<String, Object> map) throws Exception {
        return createAenderungsanfrageJson(getMapper().writeValueAsString(map));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   json  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageJson createAenderungsanfrageJson(final String json) throws Exception {
        return getMapper().readValue(json, AenderungsanfrageJson.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   json  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageResultJson createAenderungsanfrageResultJson(final String json) throws Exception {
        return getMapper().readValue(json, AenderungsanfrageResultJson.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   json  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public NachrichtAnhangJson createNachrichtAnhangJson(final String json) throws Exception {
        return getMapper().readValue(json, NachrichtAnhangJson.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   json  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public NachrichtParameterJson createNachrichtParameterJson(final String json) throws Exception {
        return getMapper().readValue(json, NachrichtParameterJson.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ContactInfoJson getContactInfo(final String user) {
        return getContactInfo(user, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   defaultUser  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ContactInfoJson getContactInfo(final String user, final String defaultUser) {
        final ContactInfosJson contactInfos = getContactInfos();
        if ((contactInfos != null) && (contactInfos.getMap() != null)) {
            if (contactInfos.getMap().containsKey(user)) {
                return contactInfos.getMap().get(user);
            } else {
                return (defaultUser != null) ? contactInfos.getMap().get(defaultUser) : null;
            }
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ContactInfosJson getContactInfos() {
        try {
            final AenderungsanfrageConf conf = getConfFromServerResource();
            final File contactInfoFile = (conf.getSachbearbeiterKontaktdaten() != null)
                ? new File(conf.getSachbearbeiterKontaktdaten()) : null;
            final String json = ((contactInfoFile != null) && contactInfoFile.isFile() && contactInfoFile.canRead())
                ? IOUtils.toString(new FileReader(contactInfoFile)) : null;
            return (json != null) ? getMapper().readValue(json, ContactInfosJson.class) : null;
        } catch (final Exception ex) {
            LOG.error("error while loading contact infos", ex);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   status             DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   metaService        DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static CidsBean getStatusBean(final Status status,
            final User user,
            final MetaService metaService,
            final ConnectionContext connectionContext) throws Exception {
        final AenderungsanfrageStatusSearchStatement search = new AenderungsanfrageStatusSearchStatement();
        search.setSchluessel(status.toString());

        final Collection<MetaObjectNode> mons = execSearch(search, user, metaService);

        if ((mons != null) && !mons.isEmpty()) {
            for (final MetaObjectNode mon : mons) {
                if (mon != null) {
                    return metaService.getMetaObject(
                                user,
                                mon.getObjectId(),
                                mon.getClassId(),
                                connectionContext).getBean();
                }
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   status             DOCUMENT ME!
     * @param   stacEntry          DOCUMENT ME!
     * @param   metaService        DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static CidsBean getStatusBean(final Status status,
            final StacEntry stacEntry,
            final MetaService metaService,
            final ConnectionContext connectionContext) throws Exception {
        if (stacEntry != null) {
            final User user = getUser(stacEntry, metaService, connectionContext);
            return getStatusBean(status, user, metaService, connectionContext);
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  user               DOCUMENT ME!
     * @param  metaService        DOCUMENT ME!
     * @param  connectionContext  DOCUMENT ME!
     */
    public static void archiveOldAenderungsanfragen(final User user,
            final MetaService metaService,
            final ConnectionContext connectionContext) {
        try {
            final Timestamp now = Timestamp.valueOf(LocalDate.now().minusMonths(2).atStartOfDay());

            final CidsBean archivedBean = getStatusBean(Status.ARCHIVED, user, metaService, connectionContext);

            final AenderungsanfrageSearchStatement search = new AenderungsanfrageSearchStatement();
            search.setActive(Boolean.TRUE);
            final Collection<MetaObjectNode> mons = execSearch(search, user, metaService);
            for (final MetaObjectNode mon : mons) {
                final CidsBean aenderungsanfrageBean = metaService.getMetaObject(
                            user,
                            mon.getObjectId(),
                            mon.getClassId(),
                            connectionContext)
                            .getBean();

                final Integer stacId = (Integer)aenderungsanfrageBean.getProperty(
                        VerdisConstants.PROP.AENDERUNGSANFRAGE.STAC_ID);
                if (stacId == null) {
                    continue;
                }
                final StacEntry stacEntry = StacUtils.getStacEntry(stacId, metaService, connectionContext);
                if (stacEntry == null) {
                    continue;
                }
                final Timestamp expiration = stacEntry.getExpiration();
                if (expiration == null) {
                    continue;
                }
                if (now.after(expiration)) {
                    aenderungsanfrageBean.setProperty(VerdisConstants.PROP.AENDERUNGSANFRAGE.STATUS, archivedBean);
                    DomainServerImpl.getServerInstance()
                            .updateMetaObject(user, aenderungsanfrageBean.getMetaObject(), connectionContext);
                }
            }
        } catch (final Exception ex) {
            LOG.error(ex, ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   anfrage       DOCUMENT ME!
     * @param   fontSize      DOCUMENT ME!
     * @param   showSystem    DOCUMENT ME!
     * @param   linkOrButton  DOCUMENT ME!
     * @param   anon          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static String createChatHtmlFromAenderungsanfrage(final AenderungsanfrageJson anfrage,
            final int fontSize,
            final boolean showSystem,
            final boolean linkOrButton,
            final boolean anon) throws Exception {
        final String mainTemplate = IOUtils.toString(AenderungsanfrageUtils.class.getResource(
                    "/de/cismet/verdis/server/utils/main.template"),
                "UTF-8");
        final String msgTemplate = IOUtils.toString(AenderungsanfrageUtils.class.getResource(
                    "/de/cismet/verdis/server/utils/msg.template"),
                "UTF-8");
        final String timeHeaderTemplate = IOUtils.toString(AenderungsanfrageUtils.class.getResource(
                    "/de/cismet/verdis/server/utils/timeHeader.template"),
                "UTF-8");
        final String attachmentLinkTemplate = IOUtils.toString(AenderungsanfrageUtils.class.getResource(
                    "/de/cismet/verdis/server/utils/attachmentLink.template"),
                "UTF-8");
        final String attachmentButtonTemplate = IOUtils.toString(AenderungsanfrageUtils.class.getResource(
                    "/de/cismet/verdis/server/utils/attachmentButton.template"),
                "UTF-8");
        final String styleTemplate = IOUtils.toString(AenderungsanfrageUtils.class.getResource(
                    "/de/cismet/verdis/server/utils/style.template"),
                "UTF-8");
        final String attachmentPngbase64 = Base64.getEncoder()
                    .encodeToString(IOUtils.toByteArray(
                            AenderungsanfrageUtils.class.getResource("/de/cismet/verdis/server/utils/attachment.png")));
        final DateFormat dateFormat = new SimpleDateFormat("dd.MM.YYYY - hh:mm");
        final DateFormat sameDateFormat = new SimpleDateFormat("hh:mm");

        final StringBuffer chatSb = new StringBuffer();
        if (anfrage.getNachrichten() != null) {
            NachrichtJson nachrichtBefore = null;
            for (final NachrichtJson nachricht : anfrage.getNachrichten()) {
                if (NachrichtJson.Typ.CITIZEN.equals(nachricht.getTyp())
                            && Boolean.TRUE.equals(nachricht.getDraft())) {
                    continue;
                }
                if (NachrichtJson.Typ.SYSTEM.equals(nachricht.getTyp()) && !showSystem) {
                    continue;
                }
                final String clazz;
                switch (nachricht.getTyp()) {
                    case CITIZEN: {
                        clazz = "sent";
                        break;
                    }
                    case CLERK: {
                        clazz = "rcvd";
                        break;
                    }
                    case SYSTEM: {
                        clazz = "sys";
                        break;
                    }
                    default: {
                        clazz = null;
                    }
                }

                final String content;
                if (NachrichtJson.Typ.SYSTEM.equals(nachricht.getTyp())) {
                    final NachrichtParameterJson nachrichtenParameter = nachricht.getNachrichtenParameter();

                    if ((nachrichtenParameter != null) && (nachrichtenParameter.getType() != null)) {
                        final Integer groesse = nachrichtenParameter.getGroesse();
                        final FlaecheFlaechenartJson flaechenart = nachrichtenParameter.getFlaechenart();
                        final FlaecheAnschlussgradJson anschlussgrad = nachrichtenParameter.getAnschlussgrad();
                        final Boolean benachrichtigt = nachrichtenParameter.getBenachrichtigt();
                        final boolean accepted = NachrichtParameterJson.Type.CHANGED.equals(
                                nachrichtenParameter.getType());
                        final AenderungsanfrageUtils.Status status = nachrichtenParameter.getStatus();
                        if (status != null) {
                            switch (status) {
                                case CLOSED: {
                                    content = anon
                                        ? "Die Bearbeitung wurde gesperrt."
                                        : String.format(
                                            "Die Bearbeitung wurde durch '%s' gesperrt.",
                                            nachricht.getAbsender());
                                }
                                break;
                                case NONE: {        // FINISHED
                                    content = anon
                                        ? "Die Bearbeitung wurde abgeschlossen."
                                        : String.format(
                                            "Die Bearbeitung wurde von '%s' abgeschlossen.",
                                            nachricht.getAbsender());
                                }
                                break;
                                case PROCESSING: {
                                    content = anon
                                        ? "Die Bearbeitung wurde aufgenommen."
                                        : String.format(
                                            "Die Bearbeitung wurde von '%s' aufgenommen.",
                                            nachricht.getAbsender());
                                }
                                break;
                                case PENDING: {
                                    content = "Es wurden neue Änderungen eingereicht.";
                                }
                                break;
                                default: {
                                    content = null; // unreachable
                                }
                            }
                        } else if (groesse != null) {
                            content = anon
                                ? String.format(
                                    "Die Änderung der Größe der Fläche '%s' auf %dm² wurde %s.",
                                    nachrichtenParameter.getFlaeche(),
                                    groesse,
                                    accepted ? "angenommen" : "abgelehnt")
                                : String.format(
                                    "Die Änderung der Größe der Fläche '%s' auf %dm² wurde von '%s' %s.",
                                    nachrichtenParameter.getFlaeche(),
                                    groesse,
                                    nachricht.getAbsender(),
                                    accepted ? "angenommen" : "abgelehnt");
                        } else if (flaechenart != null) {
                            content = anon
                                ? String.format(
                                    "Die Änderung der Flächenart der Fläche '%s' auf '%s' wurde %s.",
                                    nachrichtenParameter.getFlaeche(),
                                    flaechenart.getArt(),
                                    accepted ? "angenommen" : "abgelehnt")
                                : String.format(
                                    "Die Änderung der Flächenart der Fläche '%s' auf '%s' wurde von '%s' %s.",
                                    nachrichtenParameter.getFlaeche(),
                                    flaechenart.getArt(),
                                    nachricht.getAbsender(),
                                    accepted ? "angenommen" : "abgelehnt");
                        } else if (anschlussgrad != null) {
                            content = anon
                                ? String.format(
                                    "Die Änderung des Anschlussgrads der Fläche '%s' auf '%s' wurde %s.",
                                    nachrichtenParameter.getFlaeche(),
                                    anschlussgrad.getGrad(),
                                    accepted ? "angenommen" : "abgelehnt")
                                : String.format(
                                    "Die Änderung des Anschlussgrads der Fläche '%s' auf '%s' wurde von '%s' %s.",
                                    nachrichtenParameter.getFlaeche(),
                                    anschlussgrad.getGrad(),
                                    nachricht.getAbsender(),
                                    accepted ? "angenommen" : "abgelehnt");
                        } else if (benachrichtigt != null) {
                            content = anon
                                ? String.format(
                                    "Eine Änderungs-Benachrichtigung wurde %s.",
                                    benachrichtigt ? "versandt" : "angefordet")
                                : String.format(
                                    "Eine Änderungs-Benachrichtigung von '%s' wurde %s.",
                                    nachricht.getAbsender(),
                                    benachrichtigt ? "versandt" : "angefordet");
                        } else {
                            content = null;
                        }
                    } else {
                        content = null;
                    }
                } else {
                    content = nachricht.getNachricht().replaceAll(Pattern.quote("\n"), "<br/>\n");
                }

                final String timeHeader;
                if (Boolean.TRUE.equals(nachricht.getDraft())) {
                    timeHeader = "Entwurf";
                } else if ((nachrichtBefore != null)
                            && ((nachricht.getTimestamp().getTime() - nachrichtBefore.getTimestamp().getTime()) < 60000)
                            && (nachrichtBefore.getTyp() == nachricht.getTyp())) {
                    timeHeader = "";
                } else if ((nachrichtBefore != null)
                            && DateUtils.isSameDay(nachricht.getTimestamp(), nachrichtBefore.getTimestamp())
                            && (nachrichtBefore.getTyp() == nachricht.getTyp())) {
                    timeHeader = timeHeaderTemplate.replaceAll(
                            "<!--time-->",
                            sameDateFormat.format(nachricht.getTimestamp()));
                } else {
                    timeHeader = timeHeaderTemplate.replaceAll(
                            "<!--time-->",
                            dateFormat.format(nachricht.getTimestamp()));
                }

                final StringBuffer attachmentsSb = new StringBuffer();
                for (final NachrichtAnhangJson anhang : nachricht.getAnhang()) {
                    final String anhangJson = anhang.toJson().replaceAll("\"", "&quot;"); // sieht komisch aus, muss
                                                                                          // aber so

                    attachmentsSb.append(
                        linkOrButton
                            ? attachmentLinkTemplate.replaceAll(Pattern.quote("<!--name-->"), anhang.getName())
                            : attachmentButtonTemplate.replaceAll(Pattern.quote("<!--json-->"), anhangJson).replaceAll(
                                Pattern.quote("<!--name-->"),
                                anhang.getName()));
                }
                final String attachments = attachmentsSb.toString();
                final String msg = msgTemplate.replaceAll(Pattern.quote("<!--time-header-->"), timeHeader)
                            .replaceAll(Pattern.quote("<!--class-->"), clazz)
                            .replaceAll(Pattern.quote("<!--content-->"), content)
                            .replaceAll(Pattern.quote("<!--attachments-->"), attachments);
                chatSb.append(msg);

                nachrichtBefore = nachricht;
            }
        }

        final String title = String.format("%d", anfrage.getKassenzeichen());
        final String css = styleTemplate.replaceAll(Pattern.quote("/*font-size*/"), String.format("%dpx", fontSize))
                    .replaceAll(Pattern.quote("/*attachmentBase64*/"), attachmentPngbase64);
        final String chat = chatSb.toString();

        final String html = mainTemplate.replaceAll(Pattern.quote("<!--title-->"), title)
                    .replaceAll(Pattern.quote("/*css*/"), css)
                    .replaceAll(Pattern.quote("<!--info-->"), "")
                    .replaceAll(Pattern.quote("<!--chat-->"), chat);
        return html;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   args  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void main(final String[] args) throws Exception {
        final AenderungsanfrageJson anfrage = AenderungsanfrageUtils.getInstance().createAenderungsanfrageJson("...");
        final String html = createChatHtmlFromAenderungsanfrage(anfrage, 14, true, false, true);
        System.out.println(html);
        IOUtils.write(html, new FileOutputStream("/tmp/chat/index.html"), "UTF-8");
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static AenderungsanfrageUtils getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    @AllArgsConstructor
    public static class EmailVerification {

        //~ Instance fields ----------------------------------------------------

        private final String email;
        private final String code;
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final AenderungsanfrageUtils INSTANCE = new AenderungsanfrageUtils();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
