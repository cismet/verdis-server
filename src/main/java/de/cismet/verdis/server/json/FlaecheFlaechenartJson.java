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

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FlaecheFlaechenartJson extends AbstractJson {

    //~ Instance fields --------------------------------------------------------

    private String art;
    @JsonProperty("art_abkuerzung")
    private String artAbkuerzung;
}
