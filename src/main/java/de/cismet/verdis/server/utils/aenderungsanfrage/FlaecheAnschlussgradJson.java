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
public class FlaecheAnschlussgradJson extends FlaecheJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaecheAnschlussgradJson object.
     *
     * @param  anschlussgrad  DOCUMENT ME!
     */
    public FlaecheAnschlussgradJson(final String anschlussgrad) {
        super(null, anschlussgrad, null, null);
    }

    /**
     * Creates a new FlaecheAnschlussgradJson object.
     *
     * @param  anschlussgrad  DOCUMENT ME!
     * @param  pruefung       bemerkung DOCUMENT ME!
     */
    public FlaecheAnschlussgradJson(final String anschlussgrad,
            final FlaechePruefungJson pruefung) {
        super(null, anschlussgrad, null, pruefung);
    }
}
