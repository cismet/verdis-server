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
public class FlaecheAenderungFlaechenartJson extends FlaecheAenderungJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaecheAenderungFlaechenartJson object.
     *
     * @param  flaechenartJson  DOCUMENT ME!
     */
    public FlaecheAenderungFlaechenartJson(final FlaecheFlaechenartJson flaechenartJson) {
        super(null, null, flaechenartJson, null, null);
    }

    /**
     * Creates a new FlaecheAenderungFlaechenartJson object.
     *
     * @param  flaechenartJson  DOCUMENT ME!
     * @param  pruefung         DOCUMENT ME!
     */
    public FlaecheAenderungFlaechenartJson(final FlaecheFlaechenartJson flaechenartJson,
            final FlaechePruefungFlaechenartJson pruefung) {
        super(null, null, flaechenartJson, null, pruefung);
    }

    /**
     * Creates a new FlaecheAenderungFlaechenartJson object.
     *
     * @param  draft            DOCUMENT ME!
     * @param  flaechenartJson  DOCUMENT ME!
     */
    public FlaecheAenderungFlaechenartJson(final Boolean draft, final FlaecheFlaechenartJson flaechenartJson) {
        super(draft, null, flaechenartJson, null, null);
    }

    /**
     * Creates a new FlaecheAenderungFlaechenartJson object.
     *
     * @param  draft            DOCUMENT ME!
     * @param  flaechenartJson  DOCUMENT ME!
     * @param  pruefung         DOCUMENT ME!
     */
    public FlaecheAenderungFlaechenartJson(final Boolean draft,
            final FlaecheFlaechenartJson flaechenartJson,
            final FlaechePruefungFlaechenartJson pruefung) {
        super(draft, null, flaechenartJson, null, pruefung);
    }
}
