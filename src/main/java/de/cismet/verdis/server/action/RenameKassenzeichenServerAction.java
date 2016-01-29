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
import Sirius.server.newuser.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;

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
    private static final String QUERY_SELECT_KASSENZEICHEN = ""
                + "SELECT * "
                + "FROM {mcTableKassenzeichen} "
                + "WHERE {fieldKassenzeichenKassenzeichennummer} = ?;";
    private static final String QUERY_SELECT_CSLOCKS = ""
                + "SELECT cs_locks.id "
                + "FROM cs_locks, {mcTableKassenzeichen} AS kassenzeichen "
                + "WHERE kassenzeichen.id = cs_locks.object_id "
                + "AND cs_locks.class_id = {mcIdKassenzeichen} "
                + "AND kassenzeichen.{fieldKassenzeichenKassenzeichennummer} = ?;";
    private static final String QUERY_UPDATE_KASSENZEICHEN = ""
                + "UPDATE {mcTableKassenzeichen} "
                + "SET {fieldKassenzeichenKassenzeichennummer} = ?"
                + "WHERE {fieldKassenzeichenKassenzeichennummer} = ?;";

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
    private PreparedStatement updateKassenzeichenStatement;

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
            connection = DomainServerImpl.getServerInstance().getConnectionPool().getConnection(true);
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
    private PreparedStatement getSelectKassenzeichenPreparedStatement() throws Exception {
        if (selectKassenzeichenStatement == null) {
            final MetaClass mcKassenzeichen = getMetaService().getClassByTableName(
                    getUser(),
                    VerdisMetaClassConstants.MC_KASSENZEICHEN);
            final String query = QUERY_SELECT_KASSENZEICHEN.replaceAll(
                        "\\{mcTableKassenzeichen\\}",
                        mcKassenzeichen.getTableName())
                        .replaceAll(
                            "\\{fieldKassenzeichenKassenzeichennummer\\}",
                            KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER);

            selectKassenzeichenStatement = getConnection().prepareStatement(query);
        }
        return selectKassenzeichenStatement;
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
                    VerdisMetaClassConstants.MC_KASSENZEICHEN);
            final String query = QUERY_SELECT_CSLOCKS.replaceAll(
                        "\\{mcTableKassenzeichen\\}",
                        mcKassenzeichen.getTableName())
                        .replaceAll("\\{mcIdKassenzeichen\\}", Integer.toString(mcKassenzeichen.getID()))
                        .replaceAll(
                            "\\{fieldKassenzeichenKassenzeichennummer\\}",
                            KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER);

            selectLocksStatement = getConnection().prepareStatement(query);
        }
        return selectLocksStatement;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private PreparedStatement getUpdateKassenzeichenPreparedStatement() throws Exception {
        if (updateKassenzeichenStatement == null) {
            final MetaClass mcKassenzeichen = getMetaService().getClassByTableName(
                    getUser(),
                    VerdisMetaClassConstants.MC_KASSENZEICHEN);
            final String query = QUERY_UPDATE_KASSENZEICHEN.replaceAll(
                        "\\{mcTableKassenzeichen\\}",
                        mcKassenzeichen.getTableName())
                        .replaceAll(
                            "\\{fieldKassenzeichenKassenzeichennummer\\}",
                            KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER);
            updateKassenzeichenStatement = getConnection().prepareStatement(query);
        }
        return updateKassenzeichenStatement;
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
            final boolean sourceExists = checkKassenzeichenExists(kassenzeichennummerOld);
            final boolean targetExists = checkKassenzeichenExists(kassenzeichennummerNew);
            final boolean locked = checkLocksExists(kassenzeichennummerOld);

            if (!sourceExists) {
                return new Exception("Das Kassenzeichen mit der Nummer " + kassenzeichennummerOld
                                + " wurde nicht gefunden.");
            }
            if (targetExists) {
                return new Exception("Das Kassenzeichen mit der Nummer " + kassenzeichennummerNew
                                + " existiert bereits.");
            }
            if (locked) {
                return new Exception("Das Kassenzeichen mit der Nummer " + kassenzeichennummerOld
                                + " ist gesperrt.");
            }

            updateKassenzeichennummer(kassenzeichennummerOld, kassenzeichennummerNew);
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
    private boolean checkKassenzeichenExists(final int kassenzeichennummer) throws Exception {
        final PreparedStatement preparedSelectStatement = getSelectKassenzeichenPreparedStatement();
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
    private boolean checkLocksExists(final int kassenzeichennummer) throws Exception {
        final PreparedStatement preparedSelectStatement = getSelectLocksPreparedStatement();
        preparedSelectStatement.setInt(1, kassenzeichennummer);

        final ResultSet rs = preparedSelectStatement.executeQuery();
        return rs.next();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   kassenzeichennumerOld  DOCUMENT ME!
     * @param   kassenzeichennumerNew  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void updateKassenzeichennummer(final int kassenzeichennumerOld, final int kassenzeichennumerNew)
            throws Exception {
        final PreparedStatement preparedUpdateStatement = getUpdateKassenzeichenPreparedStatement();
        preparedUpdateStatement.setInt(1, kassenzeichennumerNew);
        preparedUpdateStatement.setInt(2, kassenzeichennumerOld);
        preparedUpdateStatement.executeUpdate();
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
