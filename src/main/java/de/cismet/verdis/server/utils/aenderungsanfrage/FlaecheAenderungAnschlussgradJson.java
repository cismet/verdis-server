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

    public FlaecheAenderungAnschlussgradJson(final FlaecheAnschlussgradJson anschlussgradJson) {
        super(null, null, null, anschlussgradJson, null);
    }

    public FlaecheAenderungAnschlussgradJson(final FlaecheAnschlussgradJson anschlussgradJson,
            final FlaechePruefungAnschlussgradJson pruefung) {
        super(null, null, null, anschlussgradJson, pruefung);
    }
    
    public FlaecheAenderungAnschlussgradJson(final Boolean draft, final FlaecheAnschlussgradJson anschlussgradJson) {
        super(draft, null, null, anschlussgradJson, null);
    }

    public FlaecheAenderungAnschlussgradJson(final Boolean draft,final FlaecheAnschlussgradJson anschlussgradJson,
            final FlaechePruefungAnschlussgradJson pruefung) {
        super(draft, null, null, anschlussgradJson, pruefung);
    }
}
