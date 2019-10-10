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
public class NachrichtParameterGroesseJson extends NachrichtParameterJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Groesse object.
     *
     * @param  type     DOCUMENT ME!
     * @param  flaeche  DOCUMENT ME!
     * @param  groesse  DOCUMENT ME!
     */
    public NachrichtParameterGroesseJson(final NachrichtParameterJson.Type type,
            final String flaeche,
            final Integer groesse) {
        super(type, flaeche, groesse, null, null);
    }
}
