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
package de.cismet.verdis.server.utils.aenderungsanfrage;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class FlaecheArtJson extends FlaecheJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaecheArtJson object.
     *
     * @param  art  DOCUMENT ME!
     */
    public FlaecheArtJson(final String art) {
        super(null, null, art, null);
    }

    /**
     * Creates a new FlaecheArtJson object.
     *
     * @param  art       DOCUMENT ME!
     * @param  pruefung  DOCUMENT ME!
     */
    public FlaecheArtJson(final String art, final PruefungJson pruefung) {
        super(null, null, art, pruefung);
    }
}
