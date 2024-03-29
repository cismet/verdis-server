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
package de.cismet.verdis.server.startup;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.interfaces.domainserver.DomainServerStartupHook;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Arrays;

import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.utils.AenderungsanfrageUtils;
import de.cismet.verdis.server.utils.VerdisServerResources;

/**
 * DOCUMENT ME!
 *
 * @author   daniel
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = DomainServerStartupHook.class)
public class VerdisServerStartupHook implements DomainServerStartupHook {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(VerdisServerStartupHook.class.getName());
    private static final String PREPARED_STATEMENT__DELETE_OLD_STACS = "DELETE FROM cs_stac WHERE expiration < now()";
    private static final String PREPARED_STATEMENT__DELETE_OLD_HISTORY =
        "DELETE FROM cs_history WHERE valid_from < (SELECT now() - interval '3 years')";

    //~ Methods ----------------------------------------------------------------

    @Override
    public void domainServerStarted() {
        loadAllServerResources();

        new Thread(new Runnable() {

                @Override
                public void run() {
                    DomainServerImpl domainServer = null;
                    while (domainServer == null) {
                        domainServer = DomainServerImpl.getServerInstance();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                        }
                    }
                    archiveOldAenderungsanfragen(domainServer);
                    // deleteOldStacs(domainServer);
                    deleteOldHistory(domainServer);
                }
            }) {
            }.start();
    }

    @Override
    public String getDomain() {
        return "VERDIS_GRUNDIS";
    }

    /**
     * DOCUMENT ME!
     */
    public void loadAllServerResources() {
        boolean error = false;
        for (final VerdisServerResources verdisServerResource : VerdisServerResources.values()) {
            try {
                ServerResourcesLoader.getInstance().load(verdisServerResource.getValue());
            } catch (final Exception ex) {
                LOG.warn("Exception while loading resource from the resources base path.", ex);
                error = true;
            }
        }

        if (error) {
            LOG.error("!!! CAUTION !!! Not all server resources could be loaded !");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  domainServer  DOCUMENT ME!
     */
    private void deleteOldStacs(final DomainServerImpl domainServer) {
        Connection connection = null;
        try {
            connection = domainServer.getConnectionPool().getConnection();
            final PreparedStatement ps = connection.prepareStatement(PREPARED_STATEMENT__DELETE_OLD_STACS);
            ps.executeUpdate();
        } catch (final SQLException ex) {
            LOG.error(ex, ex);
        } finally {
            if (connection != null) {
                domainServer.getConnectionPool().releaseDbConnection(connection);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  domainServer  DOCUMENT ME!
     */
    private void deleteOldHistory(final DomainServerImpl domainServer) {
        Connection connection = null;
        try {
            connection = domainServer.getConnectionPool().getConnection();
            final PreparedStatement ps = connection.prepareStatement(PREPARED_STATEMENT__DELETE_OLD_HISTORY);
            ps.executeUpdate();
        } catch (final SQLException ex) {
            LOG.error(ex, ex);
        } finally {
            if (connection != null) {
                domainServer.getConnectionPool().releaseDbConnection(connection);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  domainServer  DOCUMENT ME!
     */
    private void archiveOldAenderungsanfragen(final DomainServerImpl domainServer) {
        final ConnectionContext connectionContext = ConnectionContext.createDeprecated();
        final User user = new User(-1, "StartupHook", VerdisConstants.DOMAIN);
        user.setPotentialUserGroups(Arrays.asList(new UserGroup(-1, "VORN_Schreiben_KA", VerdisConstants.DOMAIN)));
        AenderungsanfrageUtils.getInstance().archiveOldAenderungsanfragen(user, domainServer, connectionContext);
    }
}
