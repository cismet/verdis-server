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

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.newuser.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import org.geojson.Feature;
import org.geojson.GeoJsonObject;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.json.AenderungsanfrageJson;
import de.cismet.verdis.server.json.AenderungsanfrageResultJson;
import de.cismet.verdis.server.json.FlaecheAenderungJson;
import de.cismet.verdis.server.json.FlaecheAnschlussgradJson;
import de.cismet.verdis.server.json.FlaecheFlaechenartJson;
import de.cismet.verdis.server.json.FlaechePruefungJson;
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

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Status {

        //~ Enum constants -----------------------------------------------------

        NONE, NEW_CITIZEN_MESSAGE, PENDING, PROCESSING, CLOSED;
    }

    //~ Instance fields --------------------------------------------------------

    @Setter private boolean unitTestContext = false;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<Integer, EmailVerification> emailVerificationMap = new HashMap();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenChangeRequestServerAction object.
     */
    private AenderungsanfrageUtils() {
        try {
            final SimpleModule module = new SimpleModule();
            module.addDeserializer(PruefungGroesseJson.class, new PruefungGroesseDeserializer(mapper));
            module.addDeserializer(PruefungFlaechenartJson.class, new PruefungFlaechenartDeserializer(mapper));
            module.addDeserializer(
                PruefungAnschlussgradJson.class,
                new PruefungAnschlussgradDeserializer(mapper));
            module.addDeserializer(NachrichtParameterJson.class,
                new NachrichtParameterDeserializer(mapper));
            module.addDeserializer(PruefungGroesseJson.class, new PruefungGroesseDeserializer(mapper));
            module.addDeserializer(FlaechePruefungJson.class, new FlaechePruefungDeserializer(mapper));
            module.addDeserializer(FlaecheAenderungJson.class, new FlaecheAenderungDeserializer(mapper));
            module.addDeserializer(FlaecheAnschlussgradJson.class, new FlaecheAnschlussgradDeserializer(mapper));
            module.addDeserializer(FlaecheFlaechenartJson.class, new FlaecheFlaechenartDeserializer(mapper));
            module.addDeserializer(NachrichtAnhangJson.class, new NachrichtAnhangDeserializer(mapper));
            module.addDeserializer(NachrichtJson.class, new NachrichtDeserializer(mapper));
            module.addDeserializer(AenderungsanfrageJson.class, new AenderungsanfrageDeserializer(mapper));
            module.addDeserializer(AenderungsanfrageResultJson.class, new AenderungsanfrageResultDeserializer(mapper));
            mapper.registerModule(module);
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
        return mapper;
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
     * @param   timestamp        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<NachrichtJson> processNachrichten(final List<NachrichtJson> nachrichtenOrig,
            final List<NachrichtJson> nachrichtenNew,
            final Boolean citizenOrClerk,
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
            final boolean isOwnMessage = isCitizen ? isCitizenMessage : (isClerk ? isClerkMessage : false);
            final boolean isNew = (newNachricht.getIdentifier() == null)
                        || !origNachrichtenMap.containsKey(newNachricht.getIdentifier());
            final boolean wasPrefiouslyDraft = (origNachrichtenMap.get(newNachricht.getIdentifier()) != null)
                        && Boolean.TRUE.equals(origNachrichtenMap.get(newNachricht.getIdentifier()).getDraft());

            // nur eigene neue Nachrichten betrachten
            if (isOwnMessage && (isNew || wasPrefiouslyDraft)) {
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
     * @param   username          DOCUMENT ME!
     * @param   timestamp         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Map<String, FlaecheAenderungJson> processFlaechen(
            final Set<String> existingFlaechen,
            final Map<String, FlaecheAenderungJson> flaechenOrig,
            final Map<String, FlaecheAenderungJson> flaechenNew,
            final Boolean citizenOrClerk,
            final String username,
            final Date timestamp) throws Exception {
        final boolean isCitizen = Boolean.TRUE.equals(citizenOrClerk);
        final boolean isClerk = Boolean.FALSE.equals(citizenOrClerk);
        final Map<String, FlaecheAenderungJson> flaechenProcessed = new HashMap<>();

        if (flaechenOrig == null) { // ???
            // keine pruefung übernehmen (ist neu, kann noch nicht geprueft worden sein)
            for (final String bezeichnung : flaechenNew.keySet()) {
                if (existingFlaechen.contains(bezeichnung)) {
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
                if (existingFlaechen.contains(bezeichnung)) {
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
                if (existingFlaechen.contains(bezeichnung)) {
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

                        // nur der eigentümer darf die flächenanfragen verändern
                        // tut er dies, hat es ein zurücksetzen der Prüfung zur Folge
                        if (isCitizen) {
                            flaecheProcessed.setDraft(flaecheNew.getDraft());
                            // veränderte Flächen übernehmen, und pruefung entfernen falls vorhanden
                            if (!Objects.equals(flaecheNew.getGroesse(), flaecheOrig.getGroesse())) {
                                flaecheProcessed.setGroesse(flaecheNew.getGroesse());
                                if (flaecheProcessed.getPruefung() != null) {
                                    flaecheProcessed.getPruefung().setGroesse(null);
                                }
                            }
                            if (!Objects.equals(flaecheNew.getFlaechenart(), flaecheOrig.getFlaechenart())) {
                                flaecheProcessed.setFlaechenart(flaecheNew.getFlaechenart());
                                if (flaecheProcessed.getPruefung() != null) {
                                    flaecheProcessed.getPruefung().setFlaechenart(null);
                                }
                            }
                            if (!Objects.equals(flaecheNew.getAnschlussgrad(), flaecheOrig.getAnschlussgrad())) {
                                flaecheProcessed.setAnschlussgrad(flaecheNew.getAnschlussgrad());
                                if (flaecheProcessed.getPruefung() != null) {
                                    flaecheProcessed.getPruefung().setAnschlussgrad(null);
                                }
                            }
                        }

                        // nur der Bearbeiter darf die Prüfung verändern
                        // tut er dies, wird der Timestamp korrekt gesetzt
                        if (isClerk) {
                            final PruefungGroesseJson pruefungGroesseOrig = (flaecheOrig.getPruefung() != null)
                                ? flaecheOrig.getPruefung().getGroesse() : null;
                            final PruefungFlaechenartJson pruefungFlaechenartOrig = (flaecheOrig.getPruefung() != null)
                                ? flaecheOrig.getPruefung().getFlaechenart() : null;
                            final PruefungAnschlussgradJson pruefungAnschlussgradOrig =
                                (flaecheOrig.getPruefung() != null) ? flaecheOrig.getPruefung().getAnschlussgrad()
                                                                    : null;

                            final PruefungGroesseJson pruefungGroesseNew = (flaecheNew.getPruefung() != null)
                                ? flaecheNew.getPruefung().getGroesse() : null;
                            final PruefungFlaechenartJson pruefungFlaechenartNew = (flaecheNew.getPruefung() != null)
                                ? flaecheNew.getPruefung().getFlaechenart() : null;
                            final PruefungAnschlussgradJson pruefungAnschlussgradNew =
                                (flaecheNew.getPruefung() != null) ? flaecheNew.getPruefung().getAnschlussgrad() : null;

                            // // gabs vorher keine Prüfung aber jetzt schon, dann neue Prüfung übernehmen
                            // if ((flaecheOrig.getPruefung() == null)
                            // && ((pruefungGroesseNew != null) || (pruefungFlaechenartNew != null)
                            // || (pruefungAnschlussgradNew != null))) {
                            flaecheProcessed.setPruefung(
                                new FlaechePruefungJson(
                                    (pruefungGroesseNew != null)
                                        ? getMapper().readValue(pruefungGroesseNew.toJson(), PruefungGroesseJson.class)
                                        : null,
                                    (pruefungFlaechenartNew != null)
                                        ? getMapper().readValue(
                                            pruefungFlaechenartNew.toJson(),
                                            PruefungFlaechenartJson.class) : null,
                                    (pruefungAnschlussgradNew != null)
                                        ? getMapper().readValue(
                                            pruefungAnschlussgradNew.toJson(),
                                            PruefungAnschlussgradJson.class) : null));
                            // }

                            // hat sich prüfung geändert, dann neuen timestamp übernehmen
                            if (!Objects.equals(pruefungGroesseOrig, pruefungGroesseNew)
                                        && (pruefungGroesseNew != null)) {
                                flaecheProcessed.getPruefung().getGroesse().setTimestamp(timestamp);
                                flaecheProcessed.getPruefung().getGroesse().setVon(username);
                            }
                            if (!Objects.equals(pruefungFlaechenartOrig, pruefungFlaechenartNew)
                                        && (pruefungFlaechenartNew != null)) {
                                flaecheProcessed.getPruefung().getFlaechenart().setTimestamp(timestamp);
                                flaecheProcessed.getPruefung().getFlaechenart().setVon(username);
                            }
                            if (!Objects.equals(pruefungAnschlussgradOrig, pruefungAnschlussgradNew)
                                        && (pruefungAnschlussgradNew != null)) {
                                flaecheProcessed.getPruefung().getAnschlussgrad().setTimestamp(timestamp);
                                flaecheProcessed.getPruefung().getAnschlussgrad().setVon(username);
                            }
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
     * @param   kassenzeichennumer  DOCUMENT ME!
     * @param   existingFlaechen    DOCUMENT ME!
     * @param   anfrageOrig         DOCUMENT ME!
     * @param   anfrageNew          DOCUMENT ME!
     * @param   username            DOCUMENT ME!
     * @param   timestamp           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageJson processAnfrageCitizen(final Integer kassenzeichennumer,
            final Set<String> existingFlaechen,
            final AenderungsanfrageJson anfrageOrig,
            final AenderungsanfrageJson anfrageNew,
            final String username,
            final Date timestamp) throws Exception {
        return processAnfrage(
                kassenzeichennumer,
                existingFlaechen,
                anfrageOrig,
                anfrageNew,
                Boolean.TRUE,
                username,
                timestamp);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichennumer  DOCUMENT ME!
     * @param   existingFlaechen    DOCUMENT ME!
     * @param   anfrageOrig         DOCUMENT ME!
     * @param   anfrageNew          DOCUMENT ME!
     * @param   username            DOCUMENT ME!
     * @param   timestamp           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageJson processAnfrageClerk(final Integer kassenzeichennumer,
            final Set<String> existingFlaechen,
            final AenderungsanfrageJson anfrageOrig,
            final AenderungsanfrageJson anfrageNew,
            final String username,
            final Date timestamp) throws Exception {
        return processAnfrage(
                kassenzeichennumer,
                existingFlaechen,
                anfrageOrig,
                anfrageNew,
                Boolean.FALSE,
                username,
                timestamp);
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
     * @param   emailAdresse  DOCUMENT ME!
     * @param   betreff       DOCUMENT ME!
     * @param   inhalt        DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static void sendMail(final String emailAdresse, final String betreff, final String inhalt)
            throws Exception {
        final AenderungsanfrageConf conf = AenderungsanfrageUtils.getConfFromServerResource();
        final String cmdTemplate = conf.getMailCmd();
        if (cmdTemplate != null) {
            executeCmd(String.format(cmdTemplate, emailAdresse, betreff, inhalt));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichenNummer  DOCUMENT ME!
     * @param   emailAdresse         DOCUMENT ME!
     * @param   code                 DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void sendVerificationMail(final Integer kassenzeichenNummer,
            final String emailAdresse,
            final String code) {
        try {
            final AenderungsanfrageConf conf = AenderungsanfrageUtils.getConfFromServerResource();
            final String betreff = conf.getMailbetreffVerifikation();
            final String template = conf.getMailtemplateVerifikation();
            final String inhalt = FileUtils.readFileToString(new File(template), "UTF-8")
                        .replaceAll(Pattern.quote("{KASSENZEICHEN}"),
                                (kassenzeichenNummer != null) ? kassenzeichenNummer.toString() : "-")
                        .replaceAll(Pattern.quote("{CODE}"), (code != null) ? code : "-");

            LOG.info("BETREFF:\n" + betreff);
            LOG.info("INHALT:\n" + inhalt);
            sendMail(emailAdresse, betreff, inhalt);
        } catch (final Exception ex) {
            LOG.error(ex, ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichenNummer  DOCUMENT ME!
     * @param   emailAdresse         DOCUMENT ME!
     * @param   code                 DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void sendStatusChangedMail(final Integer kassenzeichenNummer,
            final String emailAdresse,
            final String code) {
        try {
            final AenderungsanfrageConf conf = AenderungsanfrageUtils.getConfFromServerResource();
            final String betreff = conf.getMailbetreffStatusupdate();
            final String template = conf.getMailtemplateStatusupdate();
            final String inhalt = FileUtils.readFileToString(new File(template), "UTF-8")
                        .replaceAll(Pattern.quote("{KASSENZEICHEN}"),
                                (kassenzeichenNummer != null) ? kassenzeichenNummer.toString() : "-")
                        .replaceAll(Pattern.quote("{CODE}"), (code != null) ? code : "-");

            LOG.info("BETREFF:\n" + betreff);
            LOG.info("INHALT:\n" + inhalt);
            sendMail(emailAdresse, betreff, inhalt);
        } catch (final Exception ex) {
            LOG.error(ex, ex);
        }        
    }
    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichennumer  DOCUMENT ME!
     * @param   existingFlaechen    DOCUMENT ME!
     * @param   anfrageOrig         DOCUMENT ME!
     * @param   anfrageNew          DOCUMENT ME!
     * @param   citizenOrClerk      DOCUMENT ME!
     * @param   username            DOCUMENT ME!
     * @param   timestamp           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private AenderungsanfrageJson processAnfrage(final Integer kassenzeichennumer,
            final Set<String> existingFlaechen,
            final AenderungsanfrageJson anfrageOrig,
            final AenderungsanfrageJson anfrageNew,
            final Boolean citizenOrClerk,
            final String username,
            final Date timestamp) throws Exception {
        Boolean emailVerifiziert = anfrageOrig.getEmailVerifiziert();
        String emailAdresse = anfrageOrig.getEmailAdresse();

        // process email change
        if (anfrageNew.getEmailVerifiziert() == null) {
            emailAdresse = anfrageNew.getEmailAdresse();
            if (emailAdresse != null) {
                emailVerifiziert = false;
                final String code = addEmailVerification(kassenzeichennumer, emailAdresse);
                if (code != null) {
                    sendVerificationMail(kassenzeichennumer, emailAdresse, code);
                }
            } else {
                emailVerifiziert = null;
                removeEmail(kassenzeichennumer, anfrageOrig.getEmailAdresse());
            }
        }

        // process email verification
        final String emailVerifikation = anfrageNew.getEmailVerifikation();
        if ((emailAdresse != null) && (emailVerifikation != null) && !Boolean.TRUE.equals(emailVerifiziert)) {
            final EmailVerification emailVerification = getEmailVerification(kassenzeichennumer);
            if (emailVerification != null) {
                emailVerifiziert = emailVerifikation.equals(emailVerification.getCode());
                if (emailVerifiziert) {
                    LOG.info(String.format(
                            "validation of %s with code %s SUCCESFULL",
                            emailAdresse,
                            emailVerifikation));
                } else {
                    LOG.info(String.format("validation of %s with code %s FAILED", emailAdresse, emailVerifikation));
                }
            } else {
                // emailAdresse = null; // wirklich mail "vergessen" wenn Server neugestartet wurde
                emailVerifiziert = null;
            }
        }

        return new AenderungsanfrageJson(
                kassenzeichennumer,
                emailAdresse,
                null,
                emailVerifiziert,
                processFlaechen(
                    existingFlaechen,
                    anfrageOrig.getFlaechen(),
                    anfrageNew.getFlaechen(),
                    citizenOrClerk,
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
                    timestamp));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   oldStatus                DOCUMENT ME!
     * @param   existingFlaechen         DOCUMENT ME!
     * @param   aenderungsanfrageBefore  DOCUMENT ME!
     * @param   aenderungsanfrageAfter   DOCUMENT ME!
     * @param   citizenOrClerk           DOCUMENT ME!
     * @param   username                 DOCUMENT ME!
     * @param   timestamp                DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageUtils.Status identifyNewStatus(final Status oldStatus,
            final Set<String> existingFlaechen,
            final AenderungsanfrageJson aenderungsanfrageBefore,
            final AenderungsanfrageJson aenderungsanfrageAfter,
            final Boolean citizenOrClerk,
            final String username,
            final Date timestamp) throws Exception {
        final boolean isCitizen = Boolean.TRUE.equals(citizenOrClerk);
        final boolean isClerk = Boolean.FALSE.equals(citizenOrClerk);

        final Status changeStatusTo;
        if (isCitizen) {
            boolean newMessage = false;

            final HashMap<String, NachrichtJson> nachrichtenPerUUid = new HashMap<>();
            if (aenderungsanfrageBefore.getNachrichten() != null) {
                for (final NachrichtJson nachricht : aenderungsanfrageBefore.getNachrichten()) {
                    if ((nachricht != null) && (nachricht.getIdentifier() != null)) {
                        nachrichtenPerUUid.put(nachricht.getIdentifier(), nachricht);
                    }
                }
            }

            if (aenderungsanfrageAfter.getNachrichten() != null) {
                for (final NachrichtJson nachricht : aenderungsanfrageAfter.getNachrichten()) {
                    if (nachricht != null) {
                        if (
                            !nachrichtenPerUUid.containsKey(
                                        (nachricht.getIdentifier() != null) ? nachricht.getIdentifier().toUpperCase()
                                                                            : null)) {
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
                changeStatusTo = AenderungsanfrageUtils.Status.PENDING;
            } else if (newMessage) {
                changeStatusTo = AenderungsanfrageUtils.Status.NEW_CITIZEN_MESSAGE;
            } else {
                changeStatusTo = null;
            }
        } else if (isClerk) {
            if (aenderungsanfrageBefore.getFlaechen().size() < aenderungsanfrageAfter.getFlaechen().size()) {
                throw new Exception("flaeche added. clerk is not allowed to add flaeche");
            }

            int doneAndPendingPruefungCount = 0;
            int acceptedOrRejectedCount = 0;
            int aenderungCount = 0;

            for (final String bezeichnung : aenderungsanfrageBefore.getFlaechen().keySet()) {
                final FlaecheAenderungJson flaecheAenderungBefore = aenderungsanfrageBefore.getFlaechen()
                            .get(bezeichnung);
                final FlaecheAenderungJson flaecheAenderungAfter = aenderungsanfrageAfter.getFlaechen()
                            .get(bezeichnung);
                if (existingFlaechen.contains(bezeichnung) && (flaecheAenderungAfter == null)) {
                    throw new Exception("flaeche disappeared. clerk is not allowed to delete flaeche");
                }

                if (flaecheAenderungAfter != null) {
                    final Integer groesseBefore = flaecheAenderungBefore.getGroesse();
                    final FlaecheAnschlussgradJson anschlussgradBefore = flaecheAenderungBefore.getAnschlussgrad();
                    final FlaecheFlaechenartJson flaechenartBefore = flaecheAenderungBefore.getFlaechenart();

                    final Integer groesseAfter = flaecheAenderungAfter.getGroesse();
                    final FlaecheAnschlussgradJson anschlussgradAfter = flaecheAenderungAfter.getAnschlussgrad();
                    final FlaecheFlaechenartJson flaechenartAfter = flaecheAenderungAfter.getFlaechenart();

                    if (!Objects.equals(groesseBefore, groesseAfter)
                                || !Objects.equals(anschlussgradBefore, anschlussgradAfter)
                                || !Objects.equals(flaechenartBefore, flaechenartAfter)) {
                        throw new Exception(
                            "groesse, anschlussgrad or flachenart request did change. clerk is not allowed to do this");
                    }

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
                    if (flaecheAenderungAfter.getPruefung() != null) {
                        if (flaecheAenderungAfter.getPruefung().getGroesse() != null) {
                            doneAndPendingPruefungCount++;
                        }
                        if (flaecheAenderungAfter.getPruefung().getAnschlussgrad() != null) {
                            doneAndPendingPruefungCount++;
                        }
                        if (flaecheAenderungAfter.getPruefung().getFlaechenart() != null) {
                            doneAndPendingPruefungCount++;
                        }
                    }

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

                    final boolean isGroesseAcceptedOrRejected = (groesseBefore != null)
                                && (pruefungGroesseAfter != null);
                    final boolean isAnschlussgradAcceptedOrRejected = (anschlussgradBefore != null)
                                && (pruefungAnschlussgradAfter != null);
                    final boolean isFlaechenartAcceptedOrRejected = (flaechenartBefore != null)
                                && (pruefungFlaechenartAfter != null);

                    if (isGroesseAcceptedOrRejected) {
                        acceptedOrRejectedCount++;
                    }
                    if (isAnschlussgradAcceptedOrRejected) {
                        acceptedOrRejectedCount++;
                    }
                    if (isFlaechenartAcceptedOrRejected) {
                        acceptedOrRejectedCount++;
                    }

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
            }

            for (final String bezeichnung : aenderungsanfrageAfter.getGeometrien().keySet()) {
                final org.geojson.Feature anmerkungAfter = (org.geojson.Feature)aenderungsanfrageAfter
                            .getGeometrien().get(bezeichnung);

                if ((anmerkungAfter != null) && !Boolean.TRUE.equals(anmerkungAfter.getProperty("draft"))) {
                    aenderungCount++;

                    final Boolean pruefungAfter = (Boolean)anmerkungAfter.getProperty("pruefung");
                    if (pruefungAfter != null) {
                        doneAndPendingPruefungCount++;
                        acceptedOrRejectedCount++;
                    }
                }
            }

            if (acceptedOrRejectedCount == aenderungCount) {
                changeStatusTo = AenderungsanfrageUtils.Status.NONE;
            } else if (doneAndPendingPruefungCount == aenderungCount) {
                changeStatusTo = AenderungsanfrageUtils.Status.PROCESSING;
            } else {
                changeStatusTo = null;
            }
        } else {
            changeStatusTo = null;
        }
        final boolean statusChanged = (changeStatusTo != null) && !changeStatusTo.equals(oldStatus);
        if (statusChanged
                    && ((Status.NEW_CITIZEN_MESSAGE != changeStatusTo) && (Status.NEW_CITIZEN_MESSAGE != oldStatus))) {
            aenderungsanfrageAfter.getNachrichten()
                    .add(new NachrichtSystemJson(
                            createIdentifier(null),
                            timestamp,
                            null,
                            new NachrichtParameterStatusJson(changeStatusTo),
                            username));
        }
        return changeStatusTo;
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
                    aenderungsanfrage.getKassenzeichen(), aenderungsanfrage.getEmailAdresse(), aenderungsanfrage.getEmailVerifiziert());

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
     * @param   kassenzeichennummer    DOCUMENT ME!
     * @param   existingFlaechen       DOCUMENT ME!
     * @param   aenderungsanfrageOrig  DOCUMENT ME!
     * @param   aenderungsanfrageNew   DOCUMENT ME!
     * @param   citizenOrClerk         DOCUMENT ME!
     * @param   username               DOCUMENT ME!
     * @param   timestamp              DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageJson doProcessing(final Integer kassenzeichennummer,
            final Set<String> existingFlaechen,
            final AenderungsanfrageJson aenderungsanfrageOrig,
            final AenderungsanfrageJson aenderungsanfrageNew,
            final boolean citizenOrClerk,
            final String username,
            final Date timestamp) throws Exception {
        final AenderungsanfrageJson aenderungsanfrageProcessed;
        if (citizenOrClerk) {
            aenderungsanfrageProcessed = processAnfrageCitizen(
                    kassenzeichennummer,
                    existingFlaechen,
                    aenderungsanfrageOrig,
                    aenderungsanfrageNew,
                    username,
                    timestamp);
        } else {
            aenderungsanfrageProcessed = processAnfrageClerk(
                    kassenzeichennummer,
                    existingFlaechen,
                    aenderungsanfrageOrig,
                    aenderungsanfrageNew,
                    username,
                    timestamp);
        }
        return aenderungsanfrageProcessed;
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
            final Map localServers = new HashMap<>();
            localServers.put(VerdisConstants.DOMAIN, metaService);
            final AenderungsanfrageSearchStatement search = new AenderungsanfrageSearchStatement();
            search.setActiveLocalServers(localServers);
            search.setUser(user);
            search.setStacId(stacEntry.getId());
            final Collection<MetaObjectNode> mons = search.performServerSearch();
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

            final Map localServers = new HashMap<>();
            localServers.put(VerdisConstants.DOMAIN, metaService);
            final AenderungsanfrageStatusSearchStatement search = new AenderungsanfrageStatusSearchStatement();
            search.setActiveLocalServers(localServers);
            search.setUser(user);
            search.setSchluessel(status.toString());

            final Collection<MetaObjectNode> mons = search.performServerSearch();

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
        return null;
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
