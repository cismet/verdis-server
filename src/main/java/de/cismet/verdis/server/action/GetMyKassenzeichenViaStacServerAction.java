/** *************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 *************************************************** */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.verdis.server.action;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserServer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.connectioncontext.ConnectionContext;
import java.rmi.Naming;
import java.util.HashMap;

/**
 * DOCUMENT ME!
 *
 * @author thorsten
 * @version $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class GetMyKassenzeichenViaStacServerAction implements MetaServiceStore, ServerAction {

    //~ Static fields/initializers ---------------------------------------------
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            GetMyKassenzeichenViaStacServerAction.class);

    //~ Enums ------------------------------------------------------------------
    /**
     * DOCUMENT ME!
     *
     * @version $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------
        STAC // Short Time Authorization Code
    }

    //~ Instance fields --------------------------------------------------------
    final String stac_check
            = "select base_login_name, stac_options from cs_stac where md5(salt || ? || stac_options || base_login_name) = thehash and expiration > now();";

    private MetaService metaService;
    private Connection connection;

    //~ Methods ----------------------------------------------------------------
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private Connection getConnection() throws Exception {
        if (connection == null) {
            connection = DomainServerImpl.getServerInstance().getConnectionPool().getConnection(true);
        }
        return connection;
    }

    @Override
    public void setMetaService(final MetaService metaService) {
        this.metaService = metaService;
    }

    @Override
    public MetaService getMetaService() {
        return metaService;
    }

    @Override
    public Object execute(final Object o, final ServerActionParameter... saps) {
        String options = null;
        String stac = null;
        String baseUser = null;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("GetMyKassenzeichenViaStacServerAction");
            }
            if (saps != null) {
                for (final ServerActionParameter sap : saps) {
                    if (sap.getKey().equals(PARAMETER_TYPE.STAC.toString())) {
                        stac = (String) sap.getValue();
                        break;
                    }
                }
                if (LOG.isDebugEnabled()) {

                    LOG.debug("STAC=" + stac);
                }
                final PreparedStatement ps = getConnection().prepareStatement(stac_check);
                ps.setString(1, stac);
                final ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    baseUser = rs.getString("base_login_name");
                    LOG.debug("user: " + baseUser);
                    options = rs.getString("stac_options");
                    LOG.debug("options: " + options);

                    ObjectMapper objectMapper = new ObjectMapper();
                    HashMap optionsHM = objectMapper.readValue(options, HashMap.class);

                    final Object userServer = Naming.lookup("rmi://localhost/userServer");
                    User u = ((UserServer) userServer).getUser(
                            null,
                            null,
                            "VERDIS_GRUNDIS",
                            baseUser,
                            null);

                    MetaObject mo = metaService.getMetaObject(u, (Integer) (optionsHM.get("kassenzeichenid")), (Integer) (optionsHM.get("classId")), ConnectionContext.create(
                            ConnectionContext.Category.OTHER,
                            getClass().getSimpleName()));

                    String json = mo.getBean().toJSONString(false);

                    return json;
                }
            }

            return "{\"nothing\":\"at all\"}";
        } catch (Exception exception) {
            LOG.error("Error during STAC Check", exception);
            return "{\"nothing\":\"at all\"}";
        }
    }

    @Override
    public String getTaskName() {
        return "getMyKassenzeichen";
    }
}
