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

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.net.URLEncoder;

import java.util.UUID;

import javax.swing.SwingWorker;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.commons.security.WebDavClient;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.netutil.Proxy;

import de.cismet.verdis.server.json.NachrichtAnhangUploadJson;
import de.cismet.verdis.server.utils.AenderungsanfrageConf;
import de.cismet.verdis.server.utils.AenderungsanfrageUtils;
import de.cismet.verdis.server.utils.StacEntry;
import de.cismet.verdis.server.utils.StacUtils;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class UploadChangeRequestAnhangServerAction implements MetaServiceStore,
    UserAwareServerAction,
    ServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(UploadChangeRequestAnhangServerAction.class);
    public static final String TASKNAME = "uploadChangeRequestAnhang";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        STAC, FILENAME, WAIT_FOR_SUCCESS
    }

    //~ Instance fields --------------------------------------------------------

    private User user;
    private MetaService metaService;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   uploadFilePath  DOCUMENT ME!
     * @param   bytes           DOCUMENT ME!
     * @param   conf            DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static int upload(final String uploadFilePath, final byte[] bytes, final AenderungsanfrageConf conf)
            throws Exception {
        final InputStream data = new ByteArrayInputStream(bytes);

        final WebDavClient webdavClient = new WebDavClient(
                Proxy.fromPreferences(),
                conf.getWebdavUser(),
                conf.getWebdavPassword());
        return webdavClient.put(uploadFilePath, data);
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        NachrichtAnhangUploadJson returnJson;
        final byte[] bytes;
        String fileName = null;
        String stac = null;
        boolean waitForSuccess = true;

        try {
            if (body == null) {
                throw new Exception("body can't be null");
            } else {
                bytes = (byte[])body;
            }

            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    final String key = sap.getKey();
                    final Object value = sap.getValue();
                    if (Parameter.STAC.toString().equalsIgnoreCase(key)) {
                        stac = (String)value;
                    } else if (Parameter.FILENAME.toString().equalsIgnoreCase(key)) {
                        fileName = (String)value;
                    } else if (Parameter.WAIT_FOR_SUCCESS.toString().equalsIgnoreCase(key)) {
                        if (value instanceof String) {
                            waitForSuccess = Boolean.parseBoolean((String)value);
                        } else if (value instanceof Boolean) {
                            waitForSuccess = (Boolean)value;
                        }
                    }
                }
            }
            if (stac == null) {
                throw new StatusException(
                    412,
                    String.format("Parameter '%s' nicht gesetzt", Parameter.STAC.toString()));
            } else if (fileName == null) {
                throw new StatusException(
                    412,
                    String.format("Parameter '%s' nicht gesetzt", Parameter.FILENAME.toString()));
            }

            final StacEntry stacEntry = StacUtils.getStacEntry(
                    stac,
                    getMetaService(),
                    getConnectionContext());
            if (stacEntry == null) {
                throw new StatusException(412, "STAC ist ungültig");
            } else {
                final String uuid = UUID.randomUUID().toString();
                final AenderungsanfrageConf conf = AenderungsanfrageUtils.getConfFromServerResource();
                final String webdavUrl = conf.getWebdavUrl();
                final String uploadDirPath = webdavUrl.endsWith("/") ? webdavUrl : (webdavUrl + "/");
                final String uploadFilePath = String.format(
                        "%s%s_%s",
                        uploadDirPath,
                        uuid,
                        URLEncoder.encode(fileName, "utf-8").replaceAll("\\+", "%20"));
                final int status;
                if (waitForSuccess) {
                    status = upload(uploadFilePath, bytes, conf);
                    if (status != 201) {
                        LOG.error(String.format("upload to %s failed with status code %d", uploadFilePath, status));
                        throw new StatusException(status, "Upload fehlgeschlagen");
                    }
                } else {
                    status = 202;
                    new SwingWorker<Void, Void>() {

                            @Override
                            protected Void doInBackground() throws Exception {
                                upload(uploadFilePath, bytes, conf);
                                return null;
                            }

                            @Override
                            protected void done() {
                                try {
                                    get();
                                } catch (final Exception ex) {
                                    LOG.error(ex, ex);
                                }
                            }
                        }.execute();
                }
                returnJson = new NachrichtAnhangUploadJson(status, null, fileName, uuid);
            }
        } catch (final StatusException ex) {
            returnJson = new NachrichtAnhangUploadJson(ex.getStatus(), ex.getMessage());
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            returnJson = new NachrichtAnhangUploadJson(500, ex.getMessage());
        }

        try {
            return returnJson.toJson();
        } catch (final JsonProcessingException ex) {
            LOG.error(ex, ex);
            return null;
        }
    }

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

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    @Override
    public String getTaskName() {
        return TASKNAME;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class StatusException extends Exception {

        //~ Instance fields ----------------------------------------------------

        private final int status;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new StatusException object.
         *
         * @param  status   DOCUMENT ME!
         * @param  message  DOCUMENT ME!
         */
        public StatusException(final int status, final String message) {
            super(message);
            this.status = status;
        }

        /**
         * Creates a new StatusException object.
         *
         * @param  status   DOCUMENT ME!
         * @param  message  DOCUMENT ME!
         * @param  cause    DOCUMENT ME!
         */
        public StatusException(final int status, final String message, final Throwable cause) {
            super(message, cause);
            this.status = status;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getStatus() {
            return status;
        }
    }
}
