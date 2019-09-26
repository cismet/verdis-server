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
package de.cismet.verdis.server.json.aenderungsanfrage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;

import de.cismet.verdis.server.utils.AenderungsanfrageUtils;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
abstract class AbstractJson {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  JsonProcessingException  DOCUMENT ME!
     */
    public String toJson() throws JsonProcessingException {
        return toJson(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pretty  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  JsonProcessingException  DOCUMENT ME!
     */
    public String toJson(final boolean pretty) throws JsonProcessingException {
        if (pretty) {
            return AenderungsanfrageUtils.getInstance()
                        .getMapper()
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(this);
        } else {
            return AenderungsanfrageUtils.getInstance().getMapper().writeValueAsString(this);
        }
    }
}
