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

import java.util.ArrayList;
import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NachrichtSystemJson extends NachrichtJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new System object.
     *
     * @param  timestamp             DOCUMENT ME!
     * @param  nachrichtenParameter  DOCUMENT ME!
     * @param  verursacher           DOCUMENT ME!
     */
    public NachrichtSystemJson(final Date timestamp,
            final NachrichtParameterJson nachrichtenParameter,
            final String verursacher) {
        super(
            null,
            null,
            NachrichtJson.Typ.SYSTEM,
            timestamp,
            null,
            null,
            nachrichtenParameter,
            verursacher,
            new ArrayList<NachrichtAnhangJson>());
    }
}
