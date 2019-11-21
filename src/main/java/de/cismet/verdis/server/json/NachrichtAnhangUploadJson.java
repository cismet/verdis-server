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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class NachrichtAnhangUploadJson extends NachrichtAnhangJson {

    //~ Instance fields --------------------------------------------------------

    private Integer status;
    private String message;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NachrichtAnhangUploadJson object.
     *
     * @param  status   DOCUMENT ME!
     * @param  message  DOCUMENT ME!
     */
    public NachrichtAnhangUploadJson(final Integer status, final String message) {
        super(null, null);
        this.status = status;
        this.message = message;
    }

    /**
     * Creates a new NachrichtAnhangUploadJson object.
     *
     * @param  status   DOCUMENT ME!
     * @param  message  DOCUMENT ME!
     * @param  name     DOCUMENT ME!
     * @param  uuid     DOCUMENT ME!
     */
    public NachrichtAnhangUploadJson(final Integer status, final String message, final String name, final String uuid) {
        super(name, uuid);
        this.status = status;
        this.message = message;
    }
}
