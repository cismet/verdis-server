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
public class NachrichtSachberarbeiterJson extends NachrichtJson {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Sachberarbeiter object.
     *
     * @param  timestamp  DOCUMENT ME!
     * @param  nachricht  DOCUMENT ME!
     * @param  absender   DOCUMENT ME!
     */
    public NachrichtSachberarbeiterJson(final Date timestamp, final String nachricht, final String absender) {
        super(
            null,
            NachrichtJson.Typ.CLERK,
            timestamp,
            nachricht,
            null,
            absender,
            new ArrayList<NachrichtAnhangJson>());
    }

    /**
     * Creates a new Sachberarbeiter object.
     *
     * @param  draft      DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     * @param  nachricht  DOCUMENT ME!
     * @param  absender   DOCUMENT ME!
     */
    public NachrichtSachberarbeiterJson(final boolean draft,
            final Date timestamp,
            final String nachricht,
            final String absender) {
        super(
            draft,
            NachrichtJson.Typ.CLERK,
            timestamp,
            nachricht,
            null,
            absender,
            new ArrayList<NachrichtAnhangJson>());
    }
}
