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
public class FlaecheAenderungGroesseJson extends FlaecheAenderungJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaecheAenderungGroesseJson object.
     *
     * @param  groesse  DOCUMENT ME!
     */
    public FlaecheAenderungGroesseJson(final Integer groesse) {
        super(null, groesse, null, null, null);
    }

    /**
     * Creates a new FlaecheAenderungGroesseJson object.
     *
     * @param  groesse   DOCUMENT ME!
     * @param  pruefung  DOCUMENT ME!
     */
    public FlaecheAenderungGroesseJson(final Integer groesse, final FlaechePruefungGroesseJson pruefung) {
        super(null, groesse, null, null, pruefung);
    }

    /**
     * Creates a new FlaecheAenderungGroesseJson object.
     *
     * @param  draft    DOCUMENT ME!
     * @param  groesse  DOCUMENT ME!
     */
    public FlaecheAenderungGroesseJson(final Boolean draft, final Integer groesse) {
        super(draft, groesse, null, null, null);
    }

    /**
     * Creates a new FlaecheAenderungGroesseJson object.
     *
     * @param  draft     DOCUMENT ME!
     * @param  groesse   DOCUMENT ME!
     * @param  pruefung  DOCUMENT ME!
     */
    public FlaecheAenderungGroesseJson(final Boolean draft,
            final Integer groesse,
            final FlaechePruefungGroesseJson pruefung) {
        super(draft, groesse, null, null, pruefung);
    }
}
