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
public class NachrichtBuergerJson extends NachrichtJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Buerger object.
     *
     * @param  timestamp  DOCUMENT ME!
     * @param  nachricht  DOCUMENT ME!
     * @param  absender   DOCUMENT ME!
     */
    public NachrichtBuergerJson(final Date timestamp, final String nachricht, final String absender) {
        super(
            null,
            NachrichtJson.Typ.CITIZEN,
            timestamp,
            nachricht,
            null,
            absender,
            new ArrayList<NachrichtAnhangJson>());
    }

    /**
     * Creates a new Buerger object.
     *
     * @param  timestamp  DOCUMENT ME!
     * @param  nachricht  DOCUMENT ME!
     * @param  absender   DOCUMENT ME!
     * @param  anhang     DOCUMENT ME!
     */
    public NachrichtBuergerJson(final Date timestamp,
            final String nachricht,
            final String absender,
            final List<NachrichtAnhangJson> anhang) {
        super(null, NachrichtJson.Typ.CITIZEN, timestamp, nachricht, null, absender, anhang);
    }

    /**
     * Creates a new Buerger object.
     *
     * @param  timestamp  DOCUMENT ME!
     * @param  nachricht  DOCUMENT ME!
     * @param  absender   DOCUMENT ME!
     * @param  draft      DOCUMENT ME!
     */
    public NachrichtBuergerJson(final Date timestamp,
            final String nachricht,
            final String absender,
            final boolean draft) {
        super(
            draft,
            NachrichtJson.Typ.CITIZEN,
            timestamp,
            nachricht,
            null,
            absender,
            new ArrayList<NachrichtAnhangJson>());
    }

    /**
     * Creates a new Buerger object.
     *
     * @param  timestamp  DOCUMENT ME!
     * @param  nachricht  DOCUMENT ME!
     * @param  absender   DOCUMENT ME!
     * @param  anhang     DOCUMENT ME!
     * @param  draft      DOCUMENT ME!
     */
    public NachrichtBuergerJson(final Date timestamp,
            final String nachricht,
            final String absender,
            final List<NachrichtAnhangJson> anhang,
            final boolean draft) {
        super(draft, NachrichtJson.Typ.CITIZEN, timestamp, nachricht, null, absender, anhang);
    }
}
