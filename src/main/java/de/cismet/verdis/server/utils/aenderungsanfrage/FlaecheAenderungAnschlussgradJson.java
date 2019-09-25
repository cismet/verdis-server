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
public class FlaecheAenderungAnschlussgradJson extends FlaecheAenderungJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaecheAenderungAnschlussgradJson object.
     *
     * @param  anschlussgradJson  DOCUMENT ME!
     */
    public FlaecheAenderungAnschlussgradJson(final FlaecheAnschlussgradJson anschlussgradJson) {
        super(null, null, null, anschlussgradJson, null);
    }

    /**
     * Creates a new FlaecheAenderungAnschlussgradJson object.
     *
     * @param  anschlussgradJson  DOCUMENT ME!
     * @param  pruefung           DOCUMENT ME!
     */
    public FlaecheAenderungAnschlussgradJson(final FlaecheAnschlussgradJson anschlussgradJson,
            final FlaechePruefungAnschlussgradJson pruefung) {
        super(null, null, null, anschlussgradJson, pruefung);
    }

    /**
     * Creates a new FlaecheAenderungAnschlussgradJson object.
     *
     * @param  draft              DOCUMENT ME!
     * @param  anschlussgradJson  DOCUMENT ME!
     */
    public FlaecheAenderungAnschlussgradJson(final Boolean draft, final FlaecheAnschlussgradJson anschlussgradJson) {
        super(draft, null, null, anschlussgradJson, null);
    }

    /**
     * Creates a new FlaecheAenderungAnschlussgradJson object.
     *
     * @param  draft              DOCUMENT ME!
     * @param  anschlussgradJson  DOCUMENT ME!
     * @param  pruefung           DOCUMENT ME!
     */
    public FlaecheAenderungAnschlussgradJson(final Boolean draft,
            final FlaecheAnschlussgradJson anschlussgradJson,
            final FlaechePruefungAnschlussgradJson pruefung) {
        super(draft, null, null, anschlussgradJson, pruefung);
    }
}
