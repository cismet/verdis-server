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
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.MetaObjectNode;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserServer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

import java.rmi.Naming;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.search.AenderungsanfrageSearchStatement;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class StacUtils {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(StacUtils.class);

    private static final String PREPARED_STATEMENT__STAC_CHECK =
        "SELECT id, thehash, stac_options, base_login_name, expiration FROM cs_stac WHERE md5(salt || ? || stac_options || base_login_name) = thehash AND expiration > now();";
    private static final String PREPARED_STATEMENT__STAC_CREATE = "SELECT create_stac(?, ?, ?);";

    private static Connection CONNECTION = null;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum AenderungsanfrageStatus {

        //~ Enum constants -----------------------------------------------------

        PENDING, PROCESSING, PROCESSED, CLOSED
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static Connection getConnection() throws Exception {
        if (CONNECTION == null) {
            CONNECTION = DomainServerImpl.getServerInstance().getConnectionPool().getConnection(true);
        }
        return CONNECTION;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId          DOCUMENT ME!
     * @param   kassenzeichenId  DOCUMENT ME!
     * @param   baseLoginName    DOCUMENT ME!
     * @param   creatorUserName  DOCUMENT ME!
     * @param   expiration       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static String createStac(final Integer classId,
            final Integer kassenzeichenId,
            final String baseLoginName,
            final String creatorUserName,
            final Timestamp expiration) throws Exception {
        final HashMap optionsHM = new HashMap();
        optionsHM.put("kassenzeichenid", kassenzeichenId);
        optionsHM.put("classId", classId);
        optionsHM.put("creatorUserName", creatorUserName);

        final ObjectMapper objectMapper = new ObjectMapper();
        final String stacOptions = objectMapper.writeValueAsString(optionsHM);

        final PreparedStatement ps = getConnection().prepareStatement(PREPARED_STATEMENT__STAC_CREATE);
        ps.setString(1, baseLoginName);
        ps.setTimestamp(2, expiration);
        ps.setString(3, stacOptions);
        final ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            final String stac = rs.getString(1);
            if (LOG.isDebugEnabled()) {
                LOG.debug("stac: " + stac);
            }
            return stac;
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stac               DOCUMENT ME!
     * @param   metaService        DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static StacEntry getStacEntry(final String stac,
            final MetaService metaService,
            final ConnectionContext connectionContext) throws Exception {
        final PreparedStatement ps = getConnection().prepareStatement(PREPARED_STATEMENT__STAC_CHECK);
        ps.setString(1, stac);
        if (LOG.isDebugEnabled()) {
            LOG.debug(ps.toString());
        }
        final ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            final Integer id = rs.getInt("id");
            if (LOG.isDebugEnabled()) {
                LOG.debug("id: " + id);
            }
            final String hash = rs.getString("thehash");
            if (LOG.isDebugEnabled()) {
                LOG.debug("hash: " + hash);
            }
            final String optionsJson = rs.getString("stac_options");
            if (LOG.isDebugEnabled()) {
                LOG.debug("options: " + optionsJson);
            }
            final String baseUser = rs.getString("base_login_name");
            if (LOG.isDebugEnabled()) {
                LOG.debug("user: " + baseUser);
            }
            final Timestamp expiration = rs.getTimestamp("expiration");
            if (LOG.isDebugEnabled()) {
                LOG.debug("expiration: " + expiration);
            }
            return new StacEntry(id, hash, optionsJson, baseUser, expiration);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stacEntry          stac DOCUMENT ME!
     * @param   metaService        DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static CidsBean getKassenzeichenBean(final StacEntry stacEntry,
            final MetaService metaService,
            final ConnectionContext connectionContext) throws Exception {
        if (stacEntry != null) {
            final User user = getUser(stacEntry, metaService, connectionContext);

            final MetaObject mo = metaService.getMetaObject(
                    user,
                    (Integer)(stacEntry.getOptions().get("kassenzeichenid")),
                    (Integer)(stacEntry.getOptions().get("classId")),
                    connectionContext);
            return mo.getBean();
        } else {
            return null;
        }
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
    public static User getUser(final StacEntry stacEntry,
            final MetaService metaService,
            final ConnectionContext connectionContext) throws Exception {
        if (stacEntry != null) {
            final Object userServer = Naming.lookup("rmi://localhost/userServer");
            final User user = ((UserServer)userServer).getUser(
                    null,
                    null,
                    "VERDIS_GRUNDIS",
                    stacEntry.getLoginName(),
                    null);
            return user;
        } else {
            return null;
        }
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
    public static HashMap<String, Object> asMap(final String json) throws Exception {
        return OBJECT_MAPPER.readValue(json, HashMap.class);
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
            if (mons != null) {
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
        }
        return null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    public static class StacEntry {

        //~ Instance fields ----------------------------------------------------

        private final Integer id;
        private final String hash;
        private final String optionsJson;
        private final String loginName;
        private final Timestamp expiration;
        private final Map<String, Object> options;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new StacEntry object.
         *
         * @param   id           DOCUMENT ME!
         * @param   hash         DOCUMENT ME!
         * @param   optionsJson  DOCUMENT ME!
         * @param   loginName    DOCUMENT ME!
         * @param   expiration   DOCUMENT ME!
         *
         * @throws  Exception  DOCUMENT ME!
         */
        public StacEntry(final Integer id,
                final String hash,
                final String optionsJson,
                final String loginName,
                final Timestamp expiration) throws Exception {
            this.id = id;
            this.hash = hash;
            this.optionsJson = optionsJson;
            this.loginName = loginName;
            this.expiration = expiration;
            this.options = asMap(optionsJson);
        }
    }
}
