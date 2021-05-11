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
public class AenderungsanfrageResultJson extends AbstractJson {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum ResultStatus {

        //~ Enum constants -----------------------------------------------------

        SUCCESS {

            @Override
            public String toString() {
                return "success";
            }
        },
        ERROR {

            @Override
            public String toString() {
                return "error";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    private final ResultStatus resultStatus;
    private final AenderungsanfrageJson aenderungsanfrage;
    private final String errorMessage;
}
