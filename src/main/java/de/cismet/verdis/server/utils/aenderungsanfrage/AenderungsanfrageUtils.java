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
package de.cismet.verdis.server.utils.aenderungsanfrage;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.newuser.User;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.search.AenderungsanfrageSearchStatement;
import de.cismet.verdis.server.utils.StacUtils;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenChangeRequestServerAction object.
     */
    public AenderungsanfrageUtils() {
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        } catch (final Throwable t) {
            LOG.fatal("this should never happen", t);
        }
    }

    //~ Methods ----------------------------------------------------------------

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
    public AnfrageJson processAnfrage(final Integer kassenzeichennumer,
            final AnfrageJson anfrageOrig,
            final AnfrageJson anfrageChanged) throws Exception {
        final AnfrageJson anfrageProcessed = new AnfrageJson(kassenzeichennumer);
        if (anfrageOrig == null) {
            // nicht bürger-nachrichten rausschmeissen
            // (ist neu, kann noch keine System- oder Sachbearbeiter-Nachricht enthalten)
            for (final NachrichtJson nachrichtJson : anfrageChanged.getNachrichten()) {
                if (NachrichtJson.Typ.CITIZEN.equals(nachrichtJson.getTyp())) {
                    anfrageProcessed.getNachrichten().add(nachrichtJson);
                }
            }

            // pruefung rausschmeissen
            // (ist neu, kann noch nicht geprueft worden sein)
            for (final String bezeichnung : anfrageChanged.getFlaechen().keySet()) {
                final FlaecheJson flaecheJson = anfrageChanged.getFlaechen().get(bezeichnung);
                flaecheJson.setPruefung(null);
                anfrageProcessed.getFlaechen().put(bezeichnung, flaecheJson);
            }
        } else {
            final AnfrageJson anfrageOrigCopy = AnfrageJson.readValue(objectMapper.writeValueAsString(anfrageOrig));

            // erst alle original Nachrichten übernehmen
            long newestNachrichtTimestamp = 0;
            for (final NachrichtJson nachrichtJson : anfrageOrigCopy.getNachrichten()) {
                anfrageProcessed.getNachrichten().add(nachrichtJson);
                final long nachrichtTimestamp = nachrichtJson.getTimestamp().getTime();
                if (nachrichtTimestamp > newestNachrichtTimestamp) {
                    newestNachrichtTimestamp = nachrichtTimestamp;
                }
            }
            // dann neue Bürger-Nachrichten übernehmen
            for (final NachrichtJson nachrichtJson : anfrageChanged.getNachrichten()) {
                final long nachrichtTimestamp = nachrichtJson.getTimestamp().getTime();
                if (NachrichtJson.Typ.CITIZEN.equals(nachrichtJson.getTyp())
                            && (nachrichtTimestamp > newestNachrichtTimestamp)) {
                    anfrageProcessed.getNachrichten().add(nachrichtJson);
                }
            }

            // alle originalFlaechen übernehmen
            for (final String bezeichnung : anfrageOrigCopy.getFlaechen().keySet()) {
                final FlaecheJson flaecheJson = anfrageOrigCopy.getFlaechen().get(bezeichnung);
                anfrageProcessed.getFlaechen().put(bezeichnung, flaecheJson);
            }
            for (final String bezeichnung : anfrageChanged.getFlaechen().keySet()) {
                if (!anfrageOrigCopy.getFlaechen().containsKey(bezeichnung)) {
                    // neue CR an Flächen übernehmen (aber ohne pruefung)
                    final FlaecheJson flaecheJsonChanged = anfrageChanged.getFlaechen().get(bezeichnung);
                    flaecheJsonChanged.setPruefung(null);
                    anfrageProcessed.getFlaechen().put(bezeichnung, flaecheJsonChanged);
                } else {
                    // veränderte CR an Flächen übernehmen, und pruefung entfernen
                    final FlaecheJson flaecheJsonChanged = anfrageChanged.getFlaechen().get(bezeichnung);
                    final FlaecheJson flaecheJsonOrig = anfrageOrigCopy.getFlaechen().get(bezeichnung);

                    anfrageProcessed.getFlaechen().put(bezeichnung, flaecheJsonOrig);
                    if ((flaecheJsonChanged.getGroesse() != null)
                                && !flaecheJsonChanged.getGroesse().equals(flaecheJsonOrig.getGroesse())) {
                        flaecheJsonOrig.setGroesse(flaecheJsonChanged.getGroesse());
                        if (flaecheJsonOrig.getPruefung() != null) {
                            flaecheJsonOrig.setGroesse(null);
                        }
                    }
                    if ((flaecheJsonChanged.getFlaechenart() != null)
                                && !flaecheJsonChanged.getFlaechenart().equals(flaecheJsonOrig.getFlaechenart())) {
                        flaecheJsonOrig.setFlaechenart(flaecheJsonChanged.getFlaechenart());
                        if (flaecheJsonOrig.getPruefung() != null) {
                            flaecheJsonOrig.setFlaechenart(null);
                        }
                    }
                    if ((flaecheJsonChanged.getAnschlussgrad() != null)
                                && !flaecheJsonChanged.getAnschlussgrad().equals(flaecheJsonOrig.getAnschlussgrad())) {
                        flaecheJsonOrig.setAnschlussgrad(flaecheJsonChanged.getAnschlussgrad());
                        if (flaecheJsonOrig.getPruefung() != null) {
                            flaecheJsonOrig.setAnschlussgrad(null);
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
