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
public class PruefungGroesseJson extends PruefungJson<Integer> {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PruefungGroesseJson object.
     *
     * @param  value      DOCUMENT ME!
     * @param  von        DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     */
    public PruefungGroesseJson(final Integer value, final String von, final Date timestamp) {
        super(false, value, von, timestamp);
    }

    /**
     * Creates a new PruefungGroesseJson object.
     *
     * @param  pending    DOCUMENT ME!
     * @param  value      DOCUMENT ME!
     * @param  von        DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     */
    public PruefungGroesseJson(final boolean pending, final Integer value, final String von, final Date timestamp) {
        super(pending, value, von, timestamp);
    }
}
