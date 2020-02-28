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

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
     * @param   kassenzeichennumer  DOCUMENT ME!
     * @param   anfrageOrig         DOCUMENT ME!
     * @param   anfrage             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageJson processAnfrage(final Integer kassenzeichennumer,
            final AenderungsanfrageJson anfrageOrig,
            final AenderungsanfrageJson anfrage) throws Exception {
        final AenderungsanfrageJson anfrageProcessed = new AenderungsanfrageJson(kassenzeichennumer);
        if (anfrageOrig == null) {
            // nicht bürger-nachrichten rausschmeissen
            // (ist neu, kann noch keine System- oder Sachbearbeiter-Nachricht enthalten)
            for (final NachrichtJson nachricht : anfrage.getNachrichten()) {
                if (NachrichtJson.Typ.CITIZEN.equals(nachricht.getTyp())) {
                    anfrageProcessed.getNachrichten().add(nachricht);
                }
            }

            // pruefung rausschmeissen
            // (ist neu, kann noch nicht geprueft worden sein)
            for (final String bezeichnung : anfrage.getFlaechen().keySet()) {
                final FlaecheAenderungJson flaeche = anfrage.getFlaechen().get(bezeichnung);
                flaeche.setPruefung(null);
                anfrageProcessed.getFlaechen().put(bezeichnung, flaeche);
            }
        } else {
            final AenderungsanfrageJson anfrageOrigCopy = createAenderungsanfrageJson(anfrageOrig.toJson());

            // erst alle original Nachrichten übernehmen
            long newestNachrichtTimestamp = 0;
            for (final NachrichtJson nachricht : anfrageOrigCopy.getNachrichten()) {
                anfrageProcessed.getNachrichten().add(nachricht);
                final long nachrichtTimestamp = nachricht.getTimestamp().getTime();
                if (nachrichtTimestamp > newestNachrichtTimestamp) {
                    newestNachrichtTimestamp = nachrichtTimestamp;
                }
            }
            // dann neue Bürger-Nachrichten übernehmen
            for (final NachrichtJson nachricht : anfrage.getNachrichten()) {
                final long nachrichtTimestamp = nachricht.getTimestamp().getTime();
                if (NachrichtJson.Typ.CITIZEN.equals(nachricht.getTyp())
                            && (nachrichtTimestamp > newestNachrichtTimestamp)) {
                    anfrageProcessed.getNachrichten().add(nachricht);
                }
            }

            // alle originalFlaechen übernehmen
            for (final String bezeichnung : anfrageOrigCopy.getFlaechen().keySet()) {
                final FlaecheAenderungJson flaeche = anfrageOrigCopy.getFlaechen().get(bezeichnung);
                anfrageProcessed.getFlaechen().put(bezeichnung, flaeche);
            }
            for (final String bezeichnung : anfrage.getFlaechen().keySet()) {
                if (!anfrageOrigCopy.getFlaechen().containsKey(bezeichnung)) {
                    // neue CR an Flächen übernehmen (aber ohne pruefung)
                    final FlaecheAenderungJson flaecheJsonChanged = anfrage.getFlaechen().get(bezeichnung);
                    flaecheJsonChanged.setPruefung(null);
                    anfrageProcessed.getFlaechen().put(bezeichnung, flaecheJsonChanged);
                } else {
                    // veränderte CR an Flächen übernehmen, und pruefung entfernen
                    final FlaecheAenderungJson flaecheChanged = anfrage.getFlaechen().get(bezeichnung);
                    final FlaecheAenderungJson flaecheOrig = anfrageOrigCopy.getFlaechen().get(bezeichnung);

                    anfrageProcessed.getFlaechen().put(bezeichnung, flaecheOrig);
                    if ((flaecheChanged.getGroesse() != null)
                                && !flaecheChanged.getGroesse().equals(flaecheOrig.getGroesse())) {
                        flaecheOrig.setGroesse(flaecheChanged.getGroesse());
                        if (flaecheOrig.getPruefung() != null) {
                            flaecheOrig.setGroesse(null);
                        }
                    }
                    if ((flaecheChanged.getFlaechenart() != null)
                                && !flaecheChanged.getFlaechenart().equals(flaecheOrig.getFlaechenart())) {
                        flaecheOrig.setFlaechenart(flaecheChanged.getFlaechenart());
                        if (flaecheOrig.getPruefung() != null) {
                            flaecheOrig.setFlaechenart(null);
                        }
                    }
                    if ((flaecheChanged.getAnschlussgrad() != null)
                                && !flaecheChanged.getAnschlussgrad().equals(flaecheOrig.getAnschlussgrad())) {
                        flaecheOrig.setAnschlussgrad(flaecheChanged.getAnschlussgrad());
                        if (flaecheOrig.getPruefung() != null) {
                            flaecheOrig.setAnschlussgrad(null);
                        }
                    }
                }
            }
        }

        return anfrageProcessed;
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
    public static CidsBean getAenderungsanfrageBean(final StacEntry stacEntry,
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
    public static AenderungsanfrageJson createAenderungsanfrageJson(final Map<String, Object> map) throws Exception {
        return createAenderungsanfrageJson(getInstance().getMapper().writeValueAsString(map));
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
    public static AenderungsanfrageJson createAenderungsanfrageJson(final String json) throws Exception {
        return getInstance().getMapper().readValue(json, AenderungsanfrageJson.class);
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
    public static NachrichtAnhangJson createNachrichtAnhangJson(final String json) throws Exception {
        return getInstance().getMapper().readValue(json, NachrichtAnhangJson.class);
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
    public static NachrichtParameterJson createNachrichtParameterJson(final String json) throws Exception {
        return getInstance().getMapper().readValue(json, NachrichtParameterJson.class);
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
