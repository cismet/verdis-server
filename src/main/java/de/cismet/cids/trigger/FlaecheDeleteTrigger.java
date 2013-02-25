/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.trigger;

import Sirius.server.newuser.User;
import de.cismet.cids.dynamics.CidsBean;
import de.cismet.verdis.commons.constants.FlaechePropertyConstants;
import de.cismet.verdis.commons.constants.FlaecheninfoPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;
import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jruiz
 */
@ServiceProvider(service = CidsTrigger.class)
public class FlaecheDeleteTrigger extends AbstractDBAwareCidsTrigger {
    
    private static final transient Logger LOG = Logger.getLogger(FlaecheDeleteTrigger.class);
    

    @Override
    public void beforeInsert(CidsBean cidsBean, User user) {
    }

    @Override
    public void afterInsert(CidsBean cidsBean, User user) {
    }

    @Override
    public void beforeUpdate(CidsBean cidsBean, User user) {
    }

    @Override
    public void afterUpdate(CidsBean cidsBean, User user) {
    }

    @Override
    public void beforeDelete(CidsBean cidsBean, User user) {
    }

    @Override
    public void afterDelete(CidsBean cidsBean, User user) {
        try {
            final Object flaecheninfoId = cidsBean.getProperty(FlaechePropertyConstants.PROP__FLAECHENINFO + "." + FlaecheninfoPropertyConstants.PROP__ID);
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
    public void afterCommittedInsert(CidsBean cidsBean, User user) {
    }

    @Override
    public void afterCommittedUpdate(CidsBean cidsBean, User user) {
    }

    @Override
    public void afterCommittedDelete(CidsBean cidsBean, User user) {
   }

    @Override
    public CidsTriggerKey getTriggerKey() {
        return new CidsTriggerKey(VerdisConstants.DOMAIN, VerdisMetaClassConstants.MC_FLAECHE);
    }   

    @Override
    public int compareTo(CidsTrigger o) {
        return 0;
    }

}
