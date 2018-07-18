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

    private static final String STAC_CHECK =
        "select base_login_name, stac_options from cs_stac where md5(salt || ? || stac_options || base_login_name) = thehash and expiration > now();";
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            StacUtils.class);
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
        final PreparedStatement ps = getConnection().prepareStatement(STAC_CHECK);
        ps.setString(1, stac);
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
            return mo.getBean();
        } else {
            return null;
        }
    }
}
