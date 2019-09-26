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

import com.fasterxml.jackson.annotation.JsonInclude;
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

import de.cismet.verdis.server.json.aenderungsanfrage.AenderungsanfrageJson;
import de.cismet.verdis.server.json.aenderungsanfrage.FlaecheAenderungJson;
import de.cismet.verdis.server.json.aenderungsanfrage.FlaecheAnschlussgradJson;
import de.cismet.verdis.server.json.aenderungsanfrage.FlaecheFlaechenartJson;
import de.cismet.verdis.server.json.aenderungsanfrage.FlaechePruefungJson;
import de.cismet.verdis.server.json.aenderungsanfrage.NachrichtAnhangJson;
import de.cismet.verdis.server.json.aenderungsanfrage.NachrichtJson;
import de.cismet.verdis.server.json.aenderungsanfrage.PruefungJson;
import de.cismet.verdis.server.search.AenderungsanfrageSearchStatement;

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

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper mapper = new ObjectMapper();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenChangeRequestServerAction object.
     */
    public AenderungsanfrageUtils() {
        try {
            final SimpleModule module = new SimpleModule();
            module.addDeserializer(PruefungJson.Groesse.class, new PruefungJson.Groesse.Deserializer(mapper));
            module.addDeserializer(PruefungJson.Flaechenart.class, new PruefungJson.Flaechenart.Deserializer(mapper));
            module.addDeserializer(
                PruefungJson.Anschlussgrad.class,
                new PruefungJson.Anschlussgrad.Deserializer(mapper));
            module.addDeserializer(PruefungJson.Groesse.class, new PruefungJson.Groesse.Deserializer(mapper));
            module.addDeserializer(FlaechePruefungJson.class, new FlaechePruefungJson.Deserializer(mapper));
            module.addDeserializer(FlaecheAenderungJson.class, new FlaecheAenderungJson.Deserializer(mapper));
            module.addDeserializer(FlaecheAnschlussgradJson.class, new FlaecheAnschlussgradJson.Deserializer(mapper));
            module.addDeserializer(FlaecheFlaechenartJson.class, new FlaecheFlaechenartJson.Deserializer(mapper));
            module.addDeserializer(NachrichtAnhangJson.class, new NachrichtAnhangJson.Deserializer(mapper));
            module.addDeserializer(NachrichtJson.class, new NachrichtJson.Deserializer(mapper));
            module.addDeserializer(AenderungsanfrageJson.class, new AenderungsanfrageJson.Deserializer(mapper));
            mapper.registerModule(module);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
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
    public ObjectMapper getMapper() {
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
     * @param   anfrageChanged      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageJson processAnfrage(final Integer kassenzeichennumer,
            final AenderungsanfrageJson anfrageOrig,
            final AenderungsanfrageJson anfrageChanged) throws Exception {
        final AenderungsanfrageJson anfrageProcessed = new AenderungsanfrageJson(kassenzeichennumer);
        if (anfrageOrig == null) {
            // nicht bürger-nachrichten rausschmeissen
            // (ist neu, kann noch keine System- oder Sachbearbeiter-Nachricht enthalten)
            for (final NachrichtJson nachricht : anfrageChanged.getNachrichten()) {
                if (NachrichtJson.Typ.CITIZEN.equals(nachricht.getTyp())) {
                    anfrageProcessed.getNachrichten().add(nachricht);
                }
            }

            // pruefung rausschmeissen
            // (ist neu, kann noch nicht geprueft worden sein)
            for (final String bezeichnung : anfrageChanged.getFlaechen().keySet()) {
                final FlaecheAenderungJson flaeche = anfrageChanged.getFlaechen().get(bezeichnung);
                flaeche.setPruefung(null);
                anfrageProcessed.getFlaechen().put(bezeichnung, flaeche);
            }
        } else {
            final AenderungsanfrageJson anfrageOrigCopy = AenderungsanfrageJson.readValue(anfrageOrig.toJson());

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
            for (final NachrichtJson nachricht : anfrageChanged.getNachrichten()) {
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
            for (final String bezeichnung : anfrageChanged.getFlaechen().keySet()) {
                if (!anfrageOrigCopy.getFlaechen().containsKey(bezeichnung)) {
                    // neue CR an Flächen übernehmen (aber ohne pruefung)
                    final FlaecheAenderungJson flaecheJsonChanged = anfrageChanged.getFlaechen().get(bezeichnung);
                    flaecheJsonChanged.setPruefung(null);
                    anfrageProcessed.getFlaechen().put(bezeichnung, flaecheJsonChanged);
                } else {
                    // veränderte CR an Flächen übernehmen, und pruefung entfernen
                    final FlaecheAenderungJson flaecheChanged = anfrageChanged.getFlaechen().get(bezeichnung);
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
    public static CidsBean getAenderungsanfrageBean(final StacUtils.StacEntry stacEntry,
            final MetaService metaService,
            final ConnectionContext connectionContext) throws Exception {
        if (stacEntry != null) {
            final User user = getUser(stacEntry, metaService, connectionContext);
            final Map localServers = new HashMap<>();
            localServers.put(VerdisConstants.DOMAIN, metaService);
            final AenderungsanfrageSearchStatement search = new AenderungsanfrageSearchStatement();
            search.setActiveLocalServers(localServers);
            search.setUser(user);
            search.setStacHash(stacEntry.getHash());
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
