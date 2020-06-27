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

import lombok.Setter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import org.geojson.GeoJsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.json.AenderungsanfrageJson;
import de.cismet.verdis.server.json.FlaecheAenderungJson;
import de.cismet.verdis.server.json.FlaecheAnschlussgradJson;
import de.cismet.verdis.server.json.FlaecheFlaechenartJson;
import de.cismet.verdis.server.json.FlaechePruefungJson;
import de.cismet.verdis.server.json.NachrichtAnhangJson;
import de.cismet.verdis.server.json.NachrichtJson;
import de.cismet.verdis.server.json.NachrichtParameterJson;
import de.cismet.verdis.server.json.PruefungAnschlussgradJson;
import de.cismet.verdis.server.json.PruefungFlaechenartJson;
import de.cismet.verdis.server.json.PruefungGroesseJson;
import de.cismet.verdis.server.jsondeserializer.AenderungsanfrageDeserializer;
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

        NONE, PENDING, PROCESSING, CLOSED;
    }

    //~ Instance fields --------------------------------------------------------

    @Setter private boolean unitTestContext = false;

    private final ObjectMapper mapper = new ObjectMapper();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenChangeRequestServerAction object.
     */
    public AenderungsanfrageUtils() {
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
            mapper.registerModule(module);
        } catch (final Throwable t) {
            LOG.fatal("this should never happen", t);
        }
    }

    //~ Methods ----------------------------------------------------------------

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
     * @return  DOCUMENT ME!
     */
    private Date getNow() {
        if (unitTestContext) {
            return new Date(2500000000000L);
        } else {
            return new Date();
        }
    }
    /**
     * DOCUMENT ME!
     *
     * @param   nachrichtenOrig  DOCUMENT ME!
     * @param   nachrichtenNew   DOCUMENT ME!
     * @param   citizenOrClerk   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<NachrichtJson> processNachrichten(final List<NachrichtJson> nachrichtenOrig,
            final List<NachrichtJson> nachrichtenNew,
            final Boolean citizenOrClerk) throws Exception {
        final Date now = getNow();

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
            final boolean isCitizen = NachrichtJson.Typ.CITIZEN.equals(origNachricht.getTyp());
            final boolean isClerk = NachrichtJson.Typ.CLERK.equals(origNachricht.getTyp());
            final boolean isOwn = Boolean.TRUE.equals(citizenOrClerk)
                ? isCitizen : (Boolean.FALSE.equals(citizenOrClerk) ? isClerk : false);

            // alle vorhandenen Fremdnachrichten, die kein Draft sind, werden auf alle Fälle übernommen
            if (!isDraft || !isOwn) {
                nachrichtenProcessed.add(origNachricht);
            }
        }

        nachrichtenNew.sort(NACHRICHTEN_TIMEORDER_COMPARATOR);
        for (final NachrichtJson newNachricht : nachrichtenNew) {
            final boolean isCitizen = NachrichtJson.Typ.CITIZEN.equals(newNachricht.getTyp());
            final boolean isClerk = NachrichtJson.Typ.CLERK.equals(newNachricht.getTyp());
            final boolean isOwn = Boolean.TRUE.equals(citizenOrClerk)
                ? isCitizen : (Boolean.FALSE.equals(citizenOrClerk) ? isClerk : false);
            final boolean isNew = (newNachricht.getIdentifier() == null)
                        || !origNachrichtenMap.containsKey(newNachricht.getIdentifier());
            final boolean wasPrefiouslyDraft = (origNachrichtenMap.get(newNachricht.getIdentifier()) != null)
                        && Boolean.TRUE.equals(origNachrichtenMap.get(newNachricht.getIdentifier()).getDraft());

            // nur eigene neue Nachrichten betrachten
            if (isOwn && (isNew || wasPrefiouslyDraft)) {
                final NachrichtJson origNachricht =
                    ((newNachricht.getIdentifier() != null)
                                && origNachrichtenMap.containsKey(newNachricht.getIdentifier()))
                    ? origNachrichtenMap.get(newNachricht.getIdentifier()) : null;
                final boolean draftBecomesReal = !Boolean.TRUE.equals(newNachricht.getDraft())
                            && (origNachricht != null) && Boolean.TRUE.equals(origNachricht.getDraft());
                final boolean timestampIsNullOrInThePast = (newNachricht.getTimestamp() == null)
                            || newNachricht.getTimestamp().before(now);

                if (isNew || draftBecomesReal || timestampIsNullOrInThePast) {
                    // bekommen den aktuellen Timestamp
                    newNachricht.setTimestamp(now);
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
     * @param   flaechenOrig  DOCUMENT ME!
     * @param   flaechenNew   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Map<String, FlaecheAenderungJson> processFlaechen(final Map<String, FlaecheAenderungJson> flaechenOrig,
            final Map<String, FlaecheAenderungJson> flaechenNew) throws Exception {
        final Map<String, FlaecheAenderungJson> flaechenProcessed = new HashMap<>();

        if (flaechenOrig == null) { // ???
            // keine pruefung übernehmen (ist neu, kann noch nicht geprueft worden sein)
            for (final String bezeichnung : flaechenNew.keySet()) {
                final FlaecheAenderungJson flaeche = flaechenNew.get(bezeichnung);
                flaeche.setPruefung(null);
                flaechenProcessed.put(bezeichnung, flaeche);
            }
        } else {
            // alle Originalflaechen erst einmal pauschal übernehmen
            for (final String bezeichnung : flaechenOrig.keySet()) {
                final FlaecheAenderungJson flaecheOrig = flaechenOrig.get(bezeichnung);
                flaechenProcessed.put(bezeichnung, flaecheOrig);
            }

            // anfrage untersuchen und selektiv bearbeiten
            for (final String bezeichnung : flaechenNew.keySet()) {
                final FlaecheAenderungJson flaecheOrig = flaechenOrig.containsKey(bezeichnung)
                    ? flaechenOrig.get(bezeichnung) : null;
                final FlaecheAenderungJson flaecheNew = flaechenNew.get(bezeichnung);

                if (flaecheOrig == null) {
                    // neue Änderung ünernehmen, aber ohne pruefung (kann es nicht gegeben haben)
                    flaecheNew.setPruefung(null);
                    flaechenProcessed.put(bezeichnung, flaecheNew);
                } else {
                    // veränderte Flächen übernehmen, und pruefung entfernen falls vorhanden
                    if (!Objects.equals(flaecheNew.getGroesse(), flaecheOrig.getGroesse())) {
                        flaecheOrig.setGroesse(flaecheNew.getGroesse());
                        if (flaecheOrig.getPruefung() != null) {
                            flaecheOrig.getPruefung().setGroesse(null);
                        }
                    }
                    if (!Objects.equals(flaecheNew.getFlaechenart(), flaecheOrig.getFlaechenart())) {
                        flaecheOrig.setFlaechenart(flaecheNew.getFlaechenart());
                        if (flaecheOrig.getPruefung() != null) {
                            flaecheOrig.getPruefung().setFlaechenart(null);
                        }
                    }
                    if (!Objects.equals(flaecheNew.getAnschlussgrad(), flaecheOrig.getAnschlussgrad())) {
                        flaecheOrig.setAnschlussgrad(flaecheNew.getAnschlussgrad());
                        if (flaecheOrig.getPruefung() != null) {
                            flaecheOrig.getPruefung().setAnschlussgrad(null);
                        }
                    }
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
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Map<String, GeoJsonObject> processAnmerkungen(final Map<String, GeoJsonObject> geometrienOrig,
            final Map<String, GeoJsonObject> geometrienNew) throws Exception {
        final Map<String, GeoJsonObject> geometrienProcessed = new HashMap<>();

        // alle Originalgeometrien erst einmal pauschal übernehmen
        for (final String bezeichnung : geometrienOrig.keySet()) {
            final GeoJsonObject geoJson = geometrienOrig.get(bezeichnung);
            geometrienProcessed.put(bezeichnung, geoJson);
        }

        for (final String bezeichnung : geometrienNew.keySet()) {
            if (!geometrienOrig.containsKey(bezeichnung)) {
                // neue Geometrien übernehmen
                final GeoJsonObject geoJson = geometrienNew.get(bezeichnung);
                geometrienProcessed.put(bezeichnung, geoJson);
            }
        }
        return geometrienProcessed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichennumer  DOCUMENT ME!
     * @param   anfrageOrig         DOCUMENT ME!
     * @param   anfrage             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageJson processAnfrageCitizen(final Integer kassenzeichennumer,
            final AenderungsanfrageJson anfrageOrig,
            final AenderungsanfrageJson anfrage) throws Exception {
        return processAnfrage(kassenzeichennumer, anfrageOrig, anfrage, Boolean.TRUE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichennumer  DOCUMENT ME!
     * @param   anfrageOrig         DOCUMENT ME!
     * @param   anfrage             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageJson processAnfrageClerk(final Integer kassenzeichennumer,
            final AenderungsanfrageJson anfrageOrig,
            final AenderungsanfrageJson anfrage) throws Exception {
        return processAnfrage(kassenzeichennumer, anfrageOrig, anfrage, Boolean.FALSE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichennumer  DOCUMENT ME!
     * @param   anfrageOrig         DOCUMENT ME!
     * @param   anfrageNew          DOCUMENT ME!
     * @param   citizenOrClerk      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private AenderungsanfrageJson processAnfrage(final Integer kassenzeichennumer,
            final AenderungsanfrageJson anfrageOrig,
            final AenderungsanfrageJson anfrageNew,
            final Boolean citizenOrClerk) throws Exception {
        return new AenderungsanfrageJson(
                kassenzeichennumer,
                processFlaechen(anfrageOrig.getFlaechen(), anfrageNew.getFlaechen()),
                processAnmerkungen(anfrageOrig.getGeometrien(), anfrageNew.getGeometrien()),
                processNachrichten(anfrageOrig.getNachrichten(), anfrageNew.getNachrichten(), citizenOrClerk));
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
