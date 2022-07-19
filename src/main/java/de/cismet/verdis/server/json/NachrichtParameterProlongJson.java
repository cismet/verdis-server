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

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NachrichtParameterProlongJson extends NachrichtParameterJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NachrichtParameterNotifyJson object.
     *
     * @param  verlaengert  DOCUMENT ME!
     */
    public NachrichtParameterProlongJson(final Boolean verlaengert) {
        super(Type.PROLONG, null, null, null, null, null, null, verlaengert);
    }
}
