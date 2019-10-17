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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class NachrichtJson extends AbstractJson {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Typ {

        //~ Enum constants -----------------------------------------------------

        CLERK, CITIZEN, SYSTEM
    }

    //~ Instance fields --------------------------------------------------------

    private Boolean draft;
    private Typ typ;
    private Date timestamp;
    private String nachricht;
    private NachrichtParameterJson nachrichtenParameter;
    private String absender;
    private List<NachrichtAnhangJson> anhang;
}
