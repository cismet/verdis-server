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

    public FlaecheAenderungFlaechenartJson(final FlaecheFlaechenartJson flaechenartJson) {
        super(null, null, flaechenartJson, null, null);
    }

    public FlaecheAenderungFlaechenartJson(final FlaecheFlaechenartJson flaechenartJson,
            final FlaechePruefungFlaechenartJson pruefung) {
        super(null, null, flaechenartJson, null, pruefung);
    }
    
    public FlaecheAenderungFlaechenartJson(final Boolean draft, final FlaecheFlaechenartJson flaechenartJson) {
        super(draft, null, flaechenartJson, null, null);
    }

    public FlaecheAenderungFlaechenartJson(final Boolean draft, final FlaecheFlaechenartJson flaechenartJson,
            final FlaechePruefungFlaechenartJson pruefung) {
        super(draft, null, flaechenartJson, null, pruefung);
    }    
}
