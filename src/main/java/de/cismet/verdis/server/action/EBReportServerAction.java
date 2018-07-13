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

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.newuser.User;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import java.util.Properties;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.verdis.server.utils.VerdisServerResources;


/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */

//ByteArrayActionDownload
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class EBReportServerAction implements UserAwareServerAction, MetaServiceStore, ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(EBReportServerAction.class);
    public static final String TASK_NAME = "EBReport";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum Type {

        //~ Enum constants -----------------------------------------------------

        FRONTEN, FLAECHEN
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum MapFormat {

        //~ Enum constants -----------------------------------------------------

        A4LS, A3LS, A4P, A3P
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        TYPE, MAP_FORMAT, MAP_SCALE, ABLUSSWIRKSAMKEIT, HINTS
    }

    //~ Instance fields --------------------------------------------------------

    private User user;

    private MetaService metaService;

    private VerdisServerResources reportResource = null;

    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
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
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        try {
            System.out.println(new String(executeCmd("/usr/bin/java -version")));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cmd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static byte[] executeCmd(final String cmd) throws Exception {
        final Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        return IOUtils.toByteArray(p.getInputStream());
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        final Integer kassenzeichenId = (Integer)body;
        Type type = null;
        MapFormat mapFormat = null;
        String hints = null;
        Double scaleDenominator = null;
        Boolean abflusswirksamkeit = null;

        try {
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    if (sap.getKey().equals(Parameter.TYPE.toString())) {
                        type = Type.valueOf((String)sap.getValue());
                    } else if (sap.getKey().equals(Parameter.MAP_FORMAT.toString())) {
                        mapFormat = MapFormat.valueOf((String)sap.getValue());
                    } else if (sap.getKey().equals(Parameter.HINTS.toString())) {
                        hints = (String)sap.getValue();
                    } else if (sap.getKey().equals(Parameter.MAP_SCALE.toString())) {
                        scaleDenominator = (Double)sap.getValue();
                    } else if (sap.getKey().equals(Parameter.ABLUSSWIRKSAMKEIT.toString())) {
                        abflusswirksamkeit = (Boolean)sap.getValue();
                    }
                }
            }

            try {
                final Properties properties = ServerResourcesLoader.getInstance()
                            .loadProperties(VerdisServerResources.EB_REPORT_PROPERTIES.getValue());
                final String ebGeneratorCmd = properties.getProperty("ebGeneratorCmd")
                            .replaceAll(
                                "<kassenzeichenId>",
                                String.valueOf(kassenzeichenId).replaceAll("<type>", type.name()).replaceAll(
                                    "<mapFormat>",
                                    mapFormat.name()).replaceAll("<hints>", hints).replaceAll(
                                    "<scaleDenominator>",
                                    String.valueOf(scaleDenominator)).replaceAll(
                                    "<abflusswirksamkeit>",
                                    String.valueOf(abflusswirksamkeit)));
                return executeCmd(ebGeneratorCmd);
            } finally {
            }
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            return ex;
        }
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
