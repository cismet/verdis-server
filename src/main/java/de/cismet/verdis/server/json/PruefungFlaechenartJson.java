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

import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class PruefungFlaechenartJson extends PruefungJson<FlaecheFlaechenartJson> {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PruefungFlaechenartJson object.
     */
    public PruefungFlaechenartJson() {
        this(true, null, null, null);
    }

    /**
     * Creates a new PruefungFlaechenartJson object.
     *
     * @param  value  DOCUMENT ME!
     */
    public PruefungFlaechenartJson(final FlaecheFlaechenartJson value) {
        this(true, value, null, null);
    }

    /**
     * Creates a new PruefungFlaechenartJson object.
     *
     * @param  pending    DOCUMENT ME!
     * @param  value      DOCUMENT ME!
     * @param  von        DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     */
    public PruefungFlaechenartJson(final Boolean pending,
            final FlaecheFlaechenartJson value,
            final String von,
            final Date timestamp) {
        super(pending, value, von, timestamp);
    }
}
