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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.rmi.Naming;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.json.StacOptionsDurationJson;
import de.cismet.verdis.server.json.StacOptionsJson;
import de.cismet.verdis.server.jsondeserializer.StacOptionsDeserializer;
import de.cismet.verdis.server.jsondeserializer.StacOptionsDurationDeserializer;
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

    private static final String PREPARED_STATEMENT__STAC_CHECK = ""
                + "SELECT id, thehash, stac_options, base_login_name, expiration "
                + "FROM cs_stac "
                + "WHERE md5(salt || ? || stac_options || base_login_name) = thehash AND expiration > now();";
    private static final String PREPARED_STATEMENT__STACID_CHECK = ""
                + "SELECT id, thehash, stac_options, base_login_name, expiration "
                + "FROM cs_stac "
                + "WHERE id = ?;";
    private static final String PREPARED_STATEMENT__STAC_CREATE = ""
                + "SELECT create_stac(?, ?, ?);";
    private static final String PREPARED_STATEMENT__STAC_SET_EXPIRATION = ""
                + "UPDATE cs_stac "
                + "SET expiration = ? "
                + "WHERE id = ?";

    private static Connection CONNECTION = null;

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper mapper = new ObjectMapper();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenChangeRequestServerAction object.
     */
    public StacUtils() {
        try {
            final SimpleModule module = new SimpleModule();
            module.addDeserializer(StacOptionsJson.class, new StacOptionsDeserializer(mapper));
            module.addDeserializer(StacOptionsDurationJson.class, new StacOptionsDurationDeserializer(mapper));
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
     * @param   duration         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static String createStac(final Integer classId,
            final Integer kassenzeichenId,
            final String baseLoginName,
            final String creatorUserName,
            final Timestamp expiration,
            final StacOptionsDurationJson duration) throws Exception {
        final StacOptionsJson stacOptions = new StacOptionsJson(classId, kassenzeichenId, creatorUserName, duration);

        final String stacOptionsJson = stacOptions.toJson();

        final PreparedStatement ps = getConnection().prepareStatement(PREPARED_STATEMENT__STAC_CREATE);
        ps.setString(1, baseLoginName);
        ps.setTimestamp(2, expiration);
        ps.setString(3, stacOptionsJson);
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
     * @param   stacId             DOCUMENT ME!
     * @param   timestamp          DOCUMENT ME!
     * @param   metaService        DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void updateStacExpiration(final int stacId,
            final Timestamp timestamp,
            final MetaService metaService,
            final ConnectionContext connectionContext) throws Exception {
        final PreparedStatement ps = getConnection().prepareStatement(PREPARED_STATEMENT__STAC_SET_EXPIRATION);
        ps.setTimestamp(1, timestamp);
        ps.setInt(2, stacId);
        if (LOG.isDebugEnabled()) {
            LOG.debug(ps.toString());
        }
        ps.executeUpdate();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   duration  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Timestamp createTimestampFrom(final StacOptionsDurationJson duration) {
        return createTimestampFrom(duration, new Date());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   duration  DOCUMENT ME!
     * @param   date      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Timestamp createTimestampFrom(final StacOptionsDurationJson duration, final Date date) {
        if (duration != null) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            final StacOptionsDurationJson.Unit unit = duration.getUnit();
            if (unit != null) {
                final Integer value = duration.getValue();
                switch (unit) {
                    case MINUTES: {
                        cal.add(Calendar.MINUTE, value);
                    }
                    break;
                    case HOURS: {
                        cal.add(Calendar.HOUR, value);
                    }
                    break;
                    case DAYS: {
                        cal.add(Calendar.DAY_OF_YEAR, value);
                    }
                    break;
                    case WEEKS: {
                        cal.add(Calendar.WEEK_OF_YEAR, value);
                    }
                    break;
                    case MONTHS: {
                        cal.add(Calendar.MONTH, value);
                    }
                    break;
                    case YEARS: {
                        cal.add(Calendar.YEAR, value);
                    }
                    break;
                }
            }
            return new Timestamp(cal.getTime().getTime());
        } else {
            return new Timestamp(date.getTime());
        }
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
     * @param   stacId             DOCUMENT ME!
     * @param   metaService        DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static StacEntry getStacEntry(final Integer stacId,
            final MetaService metaService,
            final ConnectionContext connectionContext) throws Exception {
        final PreparedStatement ps = getConnection().prepareStatement(PREPARED_STATEMENT__STACID_CHECK);
        ps.setInt(1, stacId);
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
                    stacEntry.getStacOptions().getKassenzeichenid(),
                    stacEntry.getStacOptions().getClassId(),
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
        return getInstance().mapper.readValue(json, HashMap.class);
    }

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
     * @param   json  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static StacOptionsJson createStacOptionsJson(final String json) throws Exception {
        return getInstance().getMapper().readValue(json, StacOptionsJson.class);
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
    public static StacOptionsJson createStacOptionsJson(final Map<String, Object> map) throws Exception {
        return createStacOptionsJson(getInstance().getMapper().writeValueAsString(map));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static StacUtils getInstance() {
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

        private static final StacUtils INSTANCE = new StacUtils();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
