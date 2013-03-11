/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.verdis.server.trigger;

import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import org.openide.util.lookup.ServiceProvider;

import java.sql.Statement;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.trigger.AbstractDBAwareCidsTrigger;
import de.cismet.cids.trigger.CidsTrigger;
import de.cismet.cids.trigger.CidsTriggerKey;

import de.cismet.verdis.commons.constants.FlaechePropertyConstants;
import de.cismet.verdis.commons.constants.FlaecheninfoPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;
import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CidsTrigger.class)
public class FlaecheDeleteTrigger extends AbstractDBAwareCidsTrigger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(FlaecheDeleteTrigger.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public void beforeInsert(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void afterInsert(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void beforeUpdate(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void afterUpdate(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void beforeDelete(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void afterDelete(final CidsBean cidsBean, final User user) {
        try {
            final Object flaecheninfoId = cidsBean.getProperty(FlaechePropertyConstants.PROP__FLAECHENINFO + "."
                            + FlaecheninfoPropertyConstants.PROP__ID);
            final Statement s = getDbServer().getActiveDBConnection().getConnection().createStatement();
            final String sql = ""
                        + "DELETE FROM geom WHERE id IN ("
                        + "   SELECT flaecheninfo.geometrie FROM "
                        + "      flaecheninfo "
                        + "      LEFT JOIN flaeche "
                        + "         ON flaeche.flaecheninfo = flaecheninfo.id "
                        + "   WHERE "
                        + "      flaeche.id IS NULL "
                        + "      AND flaecheninfo.id = " + flaecheninfoId.toString() + ""
                        + "   )"
                        + ";"
                        + "DELETE FROM flaecheninfo WHERE id IN ("
                        + "   SELECT flaecheninfo.id FROM "
                        + "      flaecheninfo "
                        + "      LEFT JOIN flaeche "
                        + "         ON flaeche.flaecheninfo = flaecheninfo.id "
                        + "   WHERE "
                        + "      flaeche.id IS NULL "
                        + "      AND flaecheninfo.id = " + flaecheninfoId.toString() + ""
                        + "   )"
                        + ";";
            s.execute(sql);
        } catch (final Exception e) {
            LOG.error("Error while executing FlaecheDelete trigger.", e);
        }
    }

    @Override
    public void afterCommittedInsert(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void afterCommittedUpdate(final CidsBean cidsBean, final User user) {
    }

    @Override
    public void afterCommittedDelete(final CidsBean cidsBean, final User user) {
    }

    @Override
    public CidsTriggerKey getTriggerKey() {
        return new CidsTriggerKey(VerdisConstants.DOMAIN, VerdisMetaClassConstants.MC_FLAECHE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int compareTo(final CidsTrigger o) {
        return 0;
    }
}
