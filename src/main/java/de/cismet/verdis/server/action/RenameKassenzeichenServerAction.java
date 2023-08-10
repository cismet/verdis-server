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
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.verdis.commons.constants.VerdisConstants;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class RenameKassenzeichenServerAction implements MetaServiceStore, UserAwareServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            RenameKassenzeichenServerAction.class);
    private static final String QUERY_SELECT_CSLOCKS = ""
                + "SELECT cs_locks.id "
                + "FROM cs_locks, {mcTableKassenzeichen} AS kassenzeichen "
                + "WHERE kassenzeichen.id = cs_locks.object_id "
                + "AND cs_locks.class_key = {mcKassenzeichenKey} "
                + "AND kassenzeichen.{fieldKassenzeichenKassenzeichennummer} = ?;";

    public static final String TASKNAME = "renameKassenzeichen";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum ParameterType {

        //~ Enum constants -----------------------------------------------------

        KASSENZEICHENNUMMER_OLD, KASSENZEICHENNUMMER_NEW
    }

    //~ Instance fields --------------------------------------------------------

    private Connection connection;
    private PreparedStatement selectKassenzeichenStatement;
    private PreparedStatement selectLocksStatement;

    private MetaService service;
    private User user;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RenameKassenzeichenServerAction object.
     */
    public RenameKassenzeichenServerAction() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Connection getConnection() throws Exception {
        if (connection == null) {
            connection = DomainServerImpl.getServerInstance().getConnectionPool().getConnection();
        }
        return connection;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private PreparedStatement getSelectLocksPreparedStatement() throws Exception {
        if (selectLocksStatement == null) {
            final MetaClass mcKassenzeichen = getMetaService().getClassByTableName(
                    getUser(),
                    VerdisConstants.MC.KASSENZEICHEN);
            final String query = QUERY_SELECT_CSLOCKS.replaceAll(
                        "\\{mcTableKassenzeichen\\}",
                        mcKassenzeichen.getTableName())
                        .replaceAll("\\{mcKassenzeichenKey\\}", mcKassenzeichen.getTableName())
                        .replaceAll(
                            "\\{fieldKassenzeichenKassenzeichennummer\\}",
                            VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER);

            selectLocksStatement = getConnection().prepareStatement(query);
        }
        return selectLocksStatement;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        Integer kassenzeichennummerOld = null;
        Integer kassenzeichennummerNew = null;
        for (final ServerActionParameter sap : params) {
            if (sap != null) {
                if (sap.getKey().equals(ParameterType.KASSENZEICHENNUMMER_OLD.toString())) {
                    kassenzeichennummerOld = (Integer)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.KASSENZEICHENNUMMER_NEW.toString())) {
                    kassenzeichennummerNew = (Integer)sap.getValue();
                }
            }
        }

        if (kassenzeichennummerOld == null) {
            return new Exception("Es fehlt die Nummer des Kassenzeichens, welches umbenannt werden soll.");
        }
        if (kassenzeichennummerNew == null) {
            return new Exception("Es fehlt die Nummer, welche das Kassenzeichen erhalten soll.");
        }

        final int numOfDigits = 8;
        if ((kassenzeichennummerNew < Math.pow(10, numOfDigits - 1))
                    || (kassenzeichennummerNew >= Math.pow(10, numOfDigits))) {
            return new Exception("Die neue Kassenzeichennummer muss " + numOfDigits + "-stellig sein.");
        }

        try {
            final CidsBean source = getKassenzeichen(kassenzeichennummerOld);
            final CidsBean target = getKassenzeichen(kassenzeichennummerNew);
            final boolean locked = checkLocksExists(kassenzeichennummerOld);

            if (source == null) {
                return new Exception("Das Kassenzeichen mit der Nummer " + kassenzeichennummerOld
                                + " wurde nicht gefunden.");
            }
            if (target != null) {
                return new Exception("Das Kassenzeichen mit der Nummer " + kassenzeichennummerNew
                                + " existiert bereits.");
            }
            if (locked) {
                return new Exception("Das Kassenzeichen mit der Nummer " + kassenzeichennummerOld
                                + " ist gesperrt.");
            }

            updateKassenzeichennummer(source, kassenzeichennummerNew);
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            return ex;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichennummer  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private boolean checkLocksExists(final int kassenzeichennummer) throws Exception {
        final PreparedStatement preparedSelectStatement = getSelectLocksPreparedStatement();
        preparedSelectStatement.setInt(1, kassenzeichennummer);

        final ResultSet rs = preparedSelectStatement.executeQuery();
        return rs.next();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichennummer  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private CidsBean getKassenzeichen(final int kassenzeichennummer) throws Exception {
        final User user = getUser();
        final MetaService metaService = getMetaService();
        final MetaClass mc = metaService.getClassByTableName(user, VerdisConstants.MC.KASSENZEICHEN);

        final String kassenzeichenQuery = "SELECT DISTINCT " + mc.getID() + ", "
                    + mc.getTableName() + "." + mc.getPrimaryKey() + " "
                    + "FROM " + mc.getTableName() + " "
                    + "WHERE " + mc.getTableName() + "."
                    + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " = " + kassenzeichennummer + " "
                    + "LIMIT 1;";

        final MetaObject[] mos = metaService.getMetaObject(user, kassenzeichenQuery);
        if (mos.length == 1) {
            return mos[0].getBean();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichen       DOCUMENT ME!
     * @param   kassenzeichennumer  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void updateKassenzeichennummer(final CidsBean kassenzeichen, final int kassenzeichennumer)
            throws Exception {
        if (kassenzeichen != null) {
            kassenzeichen.setProperty(VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER, kassenzeichennumer);
            kassenzeichen.setProperty(
                VerdisConstants.PROP.KASSENZEICHEN.LETZTE_AENDERUNG_TIMESTAMP,
                new Timestamp(new java.util.Date().getTime()));
            kassenzeichen.setProperty(
                VerdisConstants.PROP.KASSENZEICHEN.LETZTE_AENDERUNG_USER,
                String.format("%s@%s", getUser().getName(), getUser().getUserGroup().getName()));

            DomainServerImpl.getServerInstance().updateMetaObject(getUser(), kassenzeichen.getMetaObject());
        }
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
