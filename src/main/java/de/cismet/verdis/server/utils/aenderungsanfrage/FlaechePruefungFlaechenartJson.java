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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FlaechePruefungFlaechenartJson extends FlaechePruefungJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaechePruefungFlaechenartJson object.
     *
     * @param  pruefung  DOCUMENT ME!
     */
    public FlaechePruefungFlaechenartJson(final PruefungFlaechenartJson pruefung) {
        super(null, pruefung, null);
    }
}