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
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.middleware.interfaces.domainserver.UserStore;
import Sirius.server.newuser.User;

import Sirius.util.collections.MultiMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import de.cismet.cids.server.actions.DefaultScheduledServerAction;
import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;

import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.commons.security.WebDavClient;

import de.cismet.netutil.Proxy;
import de.cismet.netutil.ProxyHandler;

import de.cismet.verdis.server.utils.VerdisServerResources;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class VeranlagungsdateiScheduledServerAction extends DefaultScheduledServerAction implements MetaServiceStore,
    UserStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            VeranlagungsdateiScheduledServerAction.class);

    public static final String TASKNAME = "veranlagungsdatei";

    private static final transient SimpleDateFormat SUFFIX_DATEFORMAT = new SimpleDateFormat("yyMMdd");
    private static final transient SimpleDateFormat DATUM_DATEFORMAT = new SimpleDateFormat("dd.MM.yyyy");

    //~ Instance fields --------------------------------------------------------

    private transient WebDavClient webdavClient;

    private MetaService service;
    private User user;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String createKey(final ServerActionParameter... params) {
        return getTaskName();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   inputArray  DOCUMENT ME!
     * @param   glueString  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String implodeArray(final String[] inputArray, final String glueString) {
        String output = "";
        if (inputArray.length > 0) {
            final StringBuilder sb = new StringBuilder();
            sb.append(inputArray[0]);
            for (int i = 1; i < inputArray.length; i++) {
                sb.append(glueString);
                sb.append(inputArray[i]);
            }
            output = sb.toString();
        }
        return output;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        Connection con = null;
        try {
            con = DomainServerImpl.getServerInstance().getConnectionPool().getConnection();

            final String selectQuery = "SELECT veranlagungseintrag.id AS id, "
                        + "    MIN(kassenzeichen.kassenzeichennummer8) AS kassenzeichennummer, "
                        + "    veranlagungsnummer.nummer AS nummer, "
                        + "    SUM(veranlagungsposten.wert) AS wert, "
                        + "    MIN(veranlagungseintrag.veranlagungsdatum), "
                        + "    EXTRACT( MONTH FROM MIN(veranlagungseintrag.veranlagungsdatum))::INTEGER AS veranlagungsmonat, "
                        + "    EXTRACT( YEAR FROM MIN(veranlagungseintrag.veranlagungsdatum))::INTEGER AS veranlagungsjahr, "
                        + "    MIN(veranlagungseintrag.datum) AS datum "
                        + "FROM veranlagungseintrag, "
                        + "    veranlagungsposten, "
                        + "    veranlagungsnummer, "
                        + "    kassenzeichen "
                        + "WHERE "
                        + "    veranlagungseintrag.kassenzeichen = kassenzeichen.id AND "
                        + "    veranlagungseintrag.id = veranlagungsposten.veranlagungseintrag AND "
                        + "    veranlagungsposten.veranlagungsnummer = veranlagungsnummer.id AND "
                        + "    (veranlagungseintrag.ist_veranlagt IS NULL OR veranlagungseintrag.ist_veranlagt IS FALSE) AND "
                        + "    veranlagungsnummer.nummer BETWEEN '700' AND '800' "
                        + "GROUP BY veranlagungseintrag.id, veranlagungsnummer.nummer "
                        + "ORDER BY MIN(veranlagungseintrag.datum) ASC";

            final ResultSet set = con.createStatement().executeQuery(selectQuery);

            final Map<String, String[]> postenToEintragMap = new MultiMap();
            final StringBuffer csvBuffer = new StringBuffer();

            while (set.next()) {
                final int id = set.getInt("id");
                final int kassenzeichennummer = set.getInt("kassenzeichennummer");
                final int nummer = set.getInt("nummer");
                final int wert = set.getInt("wert");
                final int veranlagungsmonat = set.getInt("veranlagungsmonat");
                final int veranlagungsjahr = set.getInt("veranlagungsjahr");
                final Timestamp datum = set.getTimestamp("datum");

                final String[] row = new String[] {
                        Integer.toString(kassenzeichennummer),
                        Integer.toString(nummer),
                        Integer.toString(wert),
                        Integer.toString(veranlagungsmonat),
                        Integer.toString(veranlagungsjahr),
                        DATUM_DATEFORMAT.format(datum)
                    };
                postenToEintragMap.put(Integer.toString(id), row);
                csvBuffer.append(implodeArray(row, ";")).append("\n");
            }

            if (!postenToEintragMap.keySet().isEmpty()) {
                final String filename = "Veranlagung_VERDIS_fuer_GESKA_" + SUFFIX_DATEFORMAT.format(new Date())
                            + ".csv";

                final InputStream data = new ByteArrayInputStream(csvBuffer.toString().getBytes("UTF-8"));

                final Properties webdavProperties = ServerResourcesLoader.getInstance()
                            .loadProperties(VerdisServerResources.WEBDAV.getValue());
                final String webdavPath = webdavProperties.getProperty("url_veranlagung");
                if (webdavClient == null) {
                    webdavClient = new WebDavClient(ProxyHandler.getInstance().getProxy(),
                            webdavProperties.getProperty("user"),
                            webdavProperties.getProperty("password"));
                }
                webdavClient.put(webdavPath + "/" + filename, data);

                final String updateQuery = "UPDATE veranlagungseintrag "
                            + "SET ist_veranlagt = TRUE "
                            + "WHERE id IN (" + implodeArray(postenToEintragMap.keySet().toArray(new String[0]), ", ")
                            + ")";
                con.createStatement().executeUpdate(updateQuery);
                return filename;
            }
        } catch (final Exception ex) {
            LOG.fatal("error while creating veranlagungsdatei", ex);
        }

        return null;
    }

    @Override
    public String getTaskName() {
        return TASKNAME;
    }

    @Override
    public void setMetaService(final MetaService service) {
        this.service = service;
    }

    @Override
    public MetaService getMetaService() {
        return service;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }
}
