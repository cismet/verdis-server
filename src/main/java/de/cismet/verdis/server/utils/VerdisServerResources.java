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
package de.cismet.verdis.server.utils;

import de.cismet.cids.utils.serverresources.ServerResource;
import de.cismet.cids.utils.serverresources.TextServerResource;
import lombok.Getter;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public enum VerdisServerResources {

    //~ Enum constants ---------------------------------------------------------
//~ Enum constants ---------------------------------------------------------

    MOTD_VERDIS_GRUNDIS_PROPERTIES(new TextServerResource("/motd/verdis_grundis.properties")),

    WEBDAV(new TextServerResource("/webdav/WebDav.properties"));
    
    //~ Instance fields --------------------------------------------------------

    @Getter
    private final ServerResource value;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Props object.
     *
     * @param  value  DOCUMENT ME!
     * @param  type   DOCUMENT ME!
     */
    private VerdisServerResources(final ServerResource value) {
        this.value = value;
    }

}
