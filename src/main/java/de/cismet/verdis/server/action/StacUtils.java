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
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserServer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.rmi.Naming;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.util.Date;
import java.util.HashMap;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.connectioncontext.ConnectionContext;

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
        "SELECT base_login_name, stac_options, expiration FROM cs_stac WHERE md5(salt || ? || stac_options || base_login_name) = thehash AND expiration > now();";
    private static final String PREPARED_STATEMENT__STAC_CREATE = "SELECT create_stac(?, ?, ?);";

    private static Connection CONNECTION = null;

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
    public static CidsBean getKassenzeichenBean(final String stac,
            final MetaService metaService,
            final ConnectionContext connectionContext) throws Exception {
        final PreparedStatement ps = getConnection().prepareStatement(PREPARED_STATEMENT__STAC_CHECK);
        ps.setString(1, stac);
        if (LOG.isDebugEnabled()) {
            LOG.debug(ps.toString());
        }
        final ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            final String baseUser = rs.getString("base_login_name");
            if (LOG.isDebugEnabled()) {
                LOG.debug("user: " + baseUser);
            }
            final String options = rs.getString("stac_options");
            if (LOG.isDebugEnabled()) {
                LOG.debug("options: " + options);
            }
            final Timestamp expiration = rs.getTimestamp("expiration");
            if (LOG.isDebugEnabled()) {
                LOG.debug("expiration: " + expiration);
            }

            final ObjectMapper objectMapper = new ObjectMapper();
            final HashMap optionsHM = objectMapper.readValue(options, HashMap.class);
            final Object userServer = Naming.lookup("rmi://localhost/userServer");
            final User user = ((UserServer)userServer).getUser(
                    null,
                    null,
                    "VERDIS_GRUNDIS",
                    baseUser,
                    null);

            final MetaObject mo = metaService.getMetaObject(
                    user,
                    (Integer)(optionsHM.get("kassenzeichenid")),
                    (Integer)(optionsHM.get("classId")),
                    connectionContext);
            final CidsBean kassenzeichenBean = mo.getBean();
            kassenzeichenBean.setProperty("stac_options", optionsHM);
            kassenzeichenBean.setProperty("stac_expiration", expiration);
            return kassenzeichenBean;
        } else {
            return null;
        }
    }
}
