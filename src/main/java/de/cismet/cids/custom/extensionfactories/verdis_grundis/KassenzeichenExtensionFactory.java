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
package de.cismet.cids.custom.extensionfactories.verdis_grundis;

import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.MetaObjectNode;

import java.rmi.RemoteException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.objectextension.ObjectExtensionFactory;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.search.AenderungsanfrageSearchStatement;
import de.cismet.verdis.server.utils.StacUtils;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class KassenzeichenExtensionFactory extends ObjectExtensionFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            KassenzeichenExtensionFactory.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public void extend(final CidsBean kassenzeichenBean) {
        if (kassenzeichenBean == null) {
            return;
        }

        final Integer kassenzeichenNummer = (Integer)kassenzeichenBean.getProperty(
                VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER);
        if (kassenzeichenNummer != null) {
            CidsBean aenderungsanfrageBean = null;
            final AenderungsanfrageSearchStatement search = new AenderungsanfrageSearchStatement();
            search.setKassenzeichennummer(kassenzeichenNummer);
            final Map activeLocalServers = new HashMap<>();
            activeLocalServers.put(VerdisConstants.DOMAIN, getDomainServer());
            search.setUser(getUser());
            search.setActiveLocalServers(activeLocalServers);
            final Collection<MetaObjectNode> mons = search.performServerSearch();
            if (mons != null) {
                for (final MetaObjectNode mon : mons) {
                    try {
                        final MetaObject mo = getDomainServer().getMetaObject(
                                getUser(),
                                mon.getObjectId(),
                                mon.getClassId());
                        if (mo != null) {
                            aenderungsanfrageBean = mo.getBean();
                        }
                    } catch (RemoteException ex) {
                        LOG.error(ex, ex);
                    }
                }
            }
            try {
                kassenzeichenBean.setProperty(
                    VerdisConstants.PROP.KASSENZEICHEN.AENDERUNGSANFRAGE,
                    (aenderungsanfrageBean != null)
                        ? StacUtils.asMap(
                            (String)aenderungsanfrageBean.getProperty(
                                VerdisConstants.PROP.AENDERUNGSANFRAGE.CHANGES_JSON)) : null);
            } catch (Exception ex) {
                LOG.error("Error during extension", ex);
            }
        }
    }
}
