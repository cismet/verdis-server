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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.math.BigInteger;

import java.util.Base64;
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
    public static enum Body {

        //~ Enum constants -----------------------------------------------------

        BYTE_ARRAY, STRING, INTEGER
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        TYPE, MAP_FORMAT, MAP_SCALE, ABLUSSWIRKSAMKEIT, HINTS, BODY
    }

    //~ Instance fields --------------------------------------------------------

    private User user;

    private MetaService metaService;

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
     * @param   cmd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static String executeCmd(final String cmd) throws Exception {
        final ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", cmd);
        final Process process = builder.start();
        final InputStream is = process.getInputStream();
        return IOUtils.toString(new InputStreamReader(is));
    }

    @Override
    public Object execute(final Object object, final ServerActionParameter... params) {
        Body body = Body.INTEGER;
        Type type = Type.FLAECHEN;
        MapFormat mapFormat = MapFormat.A4LS;
        String hints = "";
        Boolean abflusswirksamkeit = false;
        Double scaleDenominator = null;

        try {
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    final Object value = sap.getValue();
                    if (sap.getKey().equals(Parameter.BODY.toString())) {
                        body = (value instanceof Body) ? (Body)value : Body.valueOf((String)value);
                    } else if (sap.getKey().equals(Parameter.TYPE.toString())) {
                        type = (value instanceof Type) ? (Type)value : Type.valueOf((String)value);
                    } else if (sap.getKey().equals(Parameter.MAP_FORMAT.toString())) {
                        mapFormat = (value instanceof MapFormat) ? (MapFormat)value : MapFormat.valueOf((String)value);
                    } else if (sap.getKey().equals(Parameter.HINTS.toString())) {
                        hints = (String)sap.getValue();
                    } else if (sap.getKey().equals(Parameter.MAP_SCALE.toString())) {
                        scaleDenominator = (value instanceof Double) ? (Double)value : Double.valueOf((String)value);
                    } else if (sap.getKey().equals(Parameter.ABLUSSWIRKSAMKEIT.toString())) {
                        abflusswirksamkeit = (value instanceof Boolean) ? (Boolean)sap.getValue()
                                                                        : Boolean.valueOf((String)value);
                    }
                }
            }

            final Integer kassenzeichen;
            if (object == null) {
                throw new Exception("body is null");
            } else if (body == null) {
                throw new Exception("body-type parameter is null");
            } else {
                switch (body) {
                    case BYTE_ARRAY: {
                        kassenzeichen = new BigInteger((byte[])object).intValue();
                    }
                    break;
                    case INTEGER: {
                        kassenzeichen = (Integer)object;
                    }
                    break;
                    case STRING: {
                        kassenzeichen = Integer.parseInt((String)object);
                    }
                    break;
                    default: {
                        kassenzeichen = null;
                    }
                }
            }
            return createReport(kassenzeichen, type, mapFormat, hints, scaleDenominator, abflusswirksamkeit);
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            return ex;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichen       DOCUMENT ME!
     * @param   type                DOCUMENT ME!
     * @param   mapFormat           DOCUMENT ME!
     * @param   hints               DOCUMENT ME!
     * @param   scaleDenominator    DOCUMENT ME!
     * @param   abflusswirksamkeit  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static byte[] createReport(final Integer kassenzeichen,
            final Type type,
            final MapFormat mapFormat,
            final String hints,
            final Double scaleDenominator,
            final Boolean abflusswirksamkeit) throws Exception {
        final Properties properties = ServerResourcesLoader.getInstance()
                    .loadProperties(VerdisServerResources.EB_REPORT_ACTION_PROPERTIES.getValue());

        final Properties cmdProperties = new Properties();
        final InputStream inputStream = new FileInputStream((String)properties.get("cmdProperties"));
        cmdProperties.load(inputStream);

        final String abflusswirksamkeitFlag;
        if (Boolean.TRUE.equals(abflusswirksamkeit)) {
            abflusswirksamkeitFlag = (String)cmdProperties.getProperty("abflusswirksamkeitFlag");
        } else {
            abflusswirksamkeitFlag = "";
        }

        final String ebGeneratorCmd = cmdProperties.getProperty("cmd")
                    .replaceAll("<callserverUrl>", (String)cmdProperties.get("callserverUrl"))
                    .replaceAll("<compressionFlag>", (String)cmdProperties.get("compressionFlag"))
                    .replaceAll("<user>", (String)cmdProperties.get("user"))
                    .replaceAll("<group>", (String)cmdProperties.get("group"))
                    .replaceAll("<domain>", (String)cmdProperties.get("domain"))
                    .replaceAll("<password>", (String)cmdProperties.get("password"))
                    .replaceAll("<kassenzeichen>", String.valueOf(kassenzeichen))
                    .replaceAll("<type>", (type != null) ? type.name() : "_null_")
                    .replaceAll("<mapFormat>", (mapFormat != null) ? mapFormat.name() : "_null_")
                    .replaceAll("<hints>", (hints != null) ? hints : "")
                    .replaceAll("<scaleDenominator>", String.valueOf(scaleDenominator))
                    .replaceAll("<abflusswirksamkeitFlag>", String.valueOf(abflusswirksamkeitFlag));
        if (LOG.isDebugEnabled()) {
            LOG.debug(ebGeneratorCmd);
        }
        final String response = executeCmd(ebGeneratorCmd);
        return Base64.getMimeDecoder().decode(response);
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
