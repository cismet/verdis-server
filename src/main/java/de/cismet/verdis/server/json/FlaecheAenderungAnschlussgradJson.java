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
public class FlaecheAenderungAnschlussgradJson extends FlaecheAenderungJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaecheAenderungAnschlussgradJson object.
     *
     * @param  anschlussgrad  DOCUMENT ME!
     */
    public FlaecheAenderungAnschlussgradJson(final FlaecheAnschlussgradJson anschlussgrad) {
        super(null, null, null, anschlussgrad, null);
    }

    /**
     * Creates a new FlaecheAenderungAnschlussgradJson object.
     *
     * @param  anschlussgrad  DOCUMENT ME!
     * @param  pruefung       DOCUMENT ME!
     */
    public FlaecheAenderungAnschlussgradJson(final FlaecheAnschlussgradJson anschlussgrad,
            final FlaechePruefungFlaechenartJson pruefung) {
        super(null, null, null, anschlussgrad, pruefung);
    }

    /**
     * Creates a new FlaecheAenderungAnschlussgradJson object.
     *
     * @param  draft          DOCUMENT ME!
     * @param  anschlussgrad  DOCUMENT ME!
     */
    public FlaecheAenderungAnschlussgradJson(final Boolean draft, final FlaecheAnschlussgradJson anschlussgrad) {
        super(draft, null, null, anschlussgrad, null);
    }

    /**
     * Creates a new FlaecheAenderungAnschlussgradJson object.
     *
     * @param  draft          DOCUMENT ME!
     * @param  anschlussgrad  DOCUMENT ME!
     * @param  pruefung       DOCUMENT ME!
     */
    public FlaecheAenderungAnschlussgradJson(final Boolean draft,
            final FlaecheAnschlussgradJson anschlussgrad,
            final FlaechePruefungFlaechenartJson pruefung) {
        super(draft, null, null, anschlussgrad, pruefung);
    }
}
