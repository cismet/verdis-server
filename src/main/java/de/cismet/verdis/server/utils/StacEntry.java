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
package de.cismet.verdis.server.utils;

import lombok.Getter;

import java.sql.Timestamp;

import de.cismet.verdis.server.json.StacOptionsJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
public class StacEntry {

    //~ Instance fields --------------------------------------------------------

    private final Integer id;
    private final String hash;
    private final String stacOptionsJson;
    private final String loginName;
    private final Timestamp expiration;
    private final StacOptionsJson stacOptions;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StacEntry object.
     *
     * @param   id               DOCUMENT ME!
     * @param   hash             DOCUMENT ME!
     * @param   stacOptionsJson  DOCUMENT ME!
     * @param   loginName        DOCUMENT ME!
     * @param   expiration       DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public StacEntry(final Integer id,
            final String hash,
            final String stacOptionsJson,
            final String loginName,
            final Timestamp expiration) throws Exception {
        this.id = id;
        this.hash = hash;
        this.stacOptionsJson = stacOptionsJson;
        this.loginName = loginName;
        this.expiration = expiration;
        this.stacOptions = StacUtils.createStacOptionsJson(stacOptionsJson);
    }
}
