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
     * @param  nachricht  DOCUMENT ME!
     * @param  absender   DOCUMENT ME!
     * @param  draft      timestamp DOCUMENT ME!
     */
    public NachrichtSachberarbeiterJson(final String nachricht, final String absender, final Boolean draft) {
        this(
            null,
            null,
            null,
            nachricht,
            absender,
            draft);
    }

    /**
     * Creates a new Sachberarbeiter object.
     *
     * @param  identifier  DOCUMENT ME!
     * @param  timestamp   DOCUMENT ME!
     * @param  order       DOCUMENT ME!
     * @param  nachricht   DOCUMENT ME!
     * @param  absender    DOCUMENT ME!
     * @param  draft       DOCUMENT ME!
     */
    public NachrichtSachberarbeiterJson(
            final String identifier,
            final Date timestamp,
            final Integer order,
            final String nachricht,
            final String absender,
            final Boolean draft) {
        super(
            identifier,
            draft,
            NachrichtJson.Typ.CLERK,
            timestamp,
            order,
            nachricht,
            null,
            absender,
            new ArrayList<NachrichtAnhangJson>());
    }
}
