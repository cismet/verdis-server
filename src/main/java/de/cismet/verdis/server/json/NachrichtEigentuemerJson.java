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
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NachrichtEigentuemerJson extends NachrichtJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Buerger object.
     *
     * @param  identifier  DOCUMENT ME!
     * @param  timestamp   DOCUMENT ME!
     * @param  order       DOCUMENT ME!
     * @param  nachricht   DOCUMENT ME!
     * @param  absender    DOCUMENT ME!
     * @param  anhang      DOCUMENT ME!
     * @param  draft       DOCUMENT ME!
     */
    public NachrichtEigentuemerJson(
            final String identifier,
            final Date timestamp,
            final Integer order,
            final String nachricht,
            final String absender,
            final List<NachrichtAnhangJson> anhang,
            final Boolean draft) {
        super(identifier, draft, NachrichtJson.Typ.CITIZEN, timestamp, order, nachricht, null, absender, anhang);
    }
}
