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
public class NachrichtParameterFlaechenartJson extends NachrichtParameterJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Flaechenart object.
     *
     * @param  type         DOCUMENT ME!
     * @param  flaeche      DOCUMENT ME!
     * @param  flaechenart  DOCUMENT ME!
     */
    public NachrichtParameterFlaechenartJson(final NachrichtParameterJson.Type type,
            final String flaeche,
            final FlaecheFlaechenartJson flaechenart) {
        super(type, null, flaeche, null, flaechenart, null, null, null);
    }
}
