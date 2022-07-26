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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
public class StacOptionsJson extends AbstractJson {

    //~ Instance fields --------------------------------------------------------

    private Integer kassenzeichenid;
    private String creatorUserName;
    private StacOptionsDurationJson duration;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StacOptionsJson object.
     *
     * @param  kassenzeichenid  DOCUMENT ME!
     * @param  creatorUserName  DOCUMENT ME!
     */
    public StacOptionsJson(final Integer kassenzeichenid, final String creatorUserName) {
        this(kassenzeichenid, creatorUserName, null);
    }
}
