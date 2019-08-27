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
public class FlaecheGroesseJson extends FlaecheJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaecheGroesseJson object.
     *
     * @param  groesse  DOCUMENT ME!
     */
    public FlaecheGroesseJson(final Double groesse) {
        super(groesse, null, null, null);
    }

    /**
     * Creates a new FlaecheGroesseJson object.
     *
     * @param  groesse   DOCUMENT ME!
     * @param  pruefung  DOCUMENT ME!
     */
    public FlaecheGroesseJson(final Double groesse, final PruefungJson pruefung) {
        super(groesse, null, null, pruefung);
    }
}
