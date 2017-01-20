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
package de.cismet.verdis.server.startup;

import Sirius.server.middleware.interfaces.domainserver.DomainServerStartupHook;
import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import org.apache.log4j.Logger;

import de.cismet.verdis.server.utils.VerdisServerResources;

/**
 * DOCUMENT ME!
 *
 * @author   daniel
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = DomainServerStartupHook.class)
public class VerdisServerStartupHook implements DomainServerStartupHook {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(VerdisServerStartupHook.class.getName());

    //~ Methods ----------------------------------------------------------------

    @Override
    public void domainServerStarted() {
        loadAllServerResources();
    }

    @Override
    public String getDomain() {
        return "VERDIS_GRUNDIS";
    }

    /**
     * DOCUMENT ME!
     */
    public void loadAllServerResources() {
        boolean error = false;
        for (final VerdisServerResources verdisServerResource : VerdisServerResources.values()) {
            try {
                ServerResourcesLoader.getInstance().load(verdisServerResource.getValue());
            } catch (final Exception ex) {
                LOG.warn("Exception while loading resource from the resources base path.", ex);
                error = true;
            }
        }

        if (error) {
            LOG.error("!!! CAUTION !!! Not all server resources could be loaded !");
        }
    }
}
