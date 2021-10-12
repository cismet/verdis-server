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
public class NachrichtParameterAnschlussgradJson extends NachrichtParameterJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Anschlussgrad object.
     *
     * @param  type           DOCUMENT ME!
     * @param  flaeche        DOCUMENT ME!
     * @param  anschlussgrad  DOCUMENT ME!
     */
    public NachrichtParameterAnschlussgradJson(final NachrichtParameterJson.Type type,
            final String flaeche,
            final FlaecheAnschlussgradJson anschlussgrad) {
        super(type, null, flaeche, null, null, anschlussgrad, null);
    }
}
