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
package de.cismet.verdis.server.json;

import de.cismet.verdis.server.utils.AenderungsanfrageUtils;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NachrichtParameterNotifyJson extends NachrichtParameterJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NachrichtParameterNotifyJson object.
     *
     * @param  benachrichtigt  DOCUMENT ME!
     */
    public NachrichtParameterNotifyJson(final Boolean benachrichtigt) {
        super(Type.NOTIFY, null, null, null, null, null, benachrichtigt);
    }
}
