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

import de.cismet.verdis.server.json.aenderungsanfrage.NachrichtAnhangJson;
import de.cismet.verdis.server.utils.AenderungsanfrageConf;
import de.cismet.verdis.server.utils.AenderungsanfrageUtils;

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

        FILENAME {

            @Override
            public String toString() {
                return "fileName";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    private User user;
    private MetaService metaService;
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        final byte[] bytes;
        String fileName = null;

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
                    if (Parameter.FILENAME.toString().equals(key)) {
                        fileName = (String)value;
                    }
                }
            }
            if (fileName == null) {
                throw new Exception(Parameter.FILENAME.toString() + " parameter is missing");
            }

            final String uuid = UUID.randomUUID().toString();
            final AenderungsanfrageConf conf = AenderungsanfrageUtils.getConfFromServerResource();
            final String webdavUrl = conf.getWebdavUrl();
            final String uploadDirPath = webdavUrl.endsWith("/") ? webdavUrl : (webdavUrl + "/");
            final String uploadFilePath = String.format(
                    "%s%s_%s",
                    uploadDirPath,
                    uuid,
                    URLEncoder.encode(fileName, "utf-8").replaceAll("\\+", "%20"));
            new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            final InputStream data = new ByteArrayInputStream(bytes);

                            final WebDavClient webdavClient = new WebDavClient(
                                    Proxy.fromPreferences(),
                                    conf.getWebdavUser(),
                                    conf.getWebdavPassword());
                            webdavClient.put(uploadFilePath, data);
                        } catch (final Exception ex) {
                            LOG.error(ex, ex);
                        }
                        return null;
                    }
                }.execute();

            return new NachrichtAnhangJson(fileName, uuid).toJson();
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            return ex;
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
}
