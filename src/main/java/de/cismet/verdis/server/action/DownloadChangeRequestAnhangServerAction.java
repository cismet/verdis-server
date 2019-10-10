/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.action;

import Sirius.server.newuser.User;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.net.URLEncoder;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.commons.security.WebDavClient;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.netutil.Proxy;

import de.cismet.verdis.server.json.NachrichtAnhangJson;
import de.cismet.verdis.server.utils.AenderungsanfrageConf;
import de.cismet.verdis.server.utils.AenderungsanfrageUtils;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class DownloadChangeRequestAnhangServerAction implements ServerAction,
    UserAwareServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DownloadChangeRequestAnhangServerAction.class);
    public static final String TASK_NAME = "downloadChangeRequestAnhang";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        UUID {

            @Override
            public String toString() {
                return "uuid";
            }
        },
        FILE_NAME {

            @Override
            public String toString() {
                return "fileName";
            }
        },
        ANHANG_JSON {

            @Override
            public String toString() {
                return "nachrichtAnhangJson";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    private User user;

    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        String nachrichtAnhangJson = (String)body;
        String uuid = null;
        String fileName = null;
        try {
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    if (sap.getKey().equals(Parameter.ANHANG_JSON.toString())) {
                        nachrichtAnhangJson = (String)sap.getValue();
                    } else if (sap.getKey().equals(Parameter.UUID.toString())) {
                        uuid = (String)sap.getValue();
                    } else if (sap.getKey().equals(Parameter.FILE_NAME.toString())) {
                        fileName = (String)sap.getValue();
                    }
                }
            }
            if (nachrichtAnhangJson != null) {
                final NachrichtAnhangJson nachrichtAnhang = AenderungsanfrageUtils.createNachrichtAnhangJson(
                        nachrichtAnhangJson);
                uuid = nachrichtAnhang.getUuid();
                fileName = nachrichtAnhang.getName();
            }

            if (fileName == null) {
                return new Exception("fileName can't be null");
            }
            if (uuid == null) {
                return new Exception("uuid can't be null");
            }

            final AenderungsanfrageConf conf = AenderungsanfrageUtils.getConfFromServerResource();
            final String webdavUrl = conf.getWebdavUrl();
            final String downloadDirPath = webdavUrl.endsWith("/") ? webdavUrl : (webdavUrl + "/");
            final String downloadFilePath = String.format(
                    "%s%s_%s",
                    downloadDirPath,
                    uuid,
                    URLEncoder.encode(fileName, "utf-8").replaceAll("\\+", "%20"));

            final WebDavClient webdavClient = new WebDavClient(
                    Proxy.fromPreferences(),
                    conf.getWebdavUser(),
                    conf.getWebdavPassword());

            return IOUtils.toByteArray(webdavClient.getInputStream(downloadFilePath));
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
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
