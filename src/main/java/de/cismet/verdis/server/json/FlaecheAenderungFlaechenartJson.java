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
public class FlaecheAenderungFlaechenartJson extends FlaecheAenderungJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaecheAenderungFlaechenartJson object.
     *
     * @param  flaechenart  DOCUMENT ME!
     */
    public FlaecheAenderungFlaechenartJson(final FlaecheFlaechenartJson flaechenart) {
        super(null, null, flaechenart, null, null);
    }

    /**
     * Creates a new FlaecheAenderungFlaechenartJson object.
     *
     * @param  flaechenart  DOCUMENT ME!
     * @param  pruefung     DOCUMENT ME!
     */
    public FlaecheAenderungFlaechenartJson(final FlaecheFlaechenartJson flaechenart,
            final FlaechePruefungFlaechenartJson pruefung) {
        super(null, null, flaechenart, null, pruefung);
    }

    /**
     * Creates a new FlaecheAenderungFlaechenartJson object.
     *
     * @param  draft        DOCUMENT ME!
     * @param  flaechenart  DOCUMENT ME!
     */
    public FlaecheAenderungFlaechenartJson(final Boolean draft, final FlaecheFlaechenartJson flaechenart) {
        super(draft, null, flaechenart, null, null);
    }

    /**
     * Creates a new FlaecheAenderungFlaechenartJson object.
     *
     * @param  draft        DOCUMENT ME!
     * @param  flaechenart  DOCUMENT ME!
     * @param  pruefung     DOCUMENT ME!
     */
    public FlaecheAenderungFlaechenartJson(final Boolean draft,
            final FlaecheFlaechenartJson flaechenart,
            final FlaechePruefungFlaechenartJson pruefung) {
        super(draft, null, flaechenart, null, pruefung);
    }
}
