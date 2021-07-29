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

import org.geojson.GeoJsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AenderungsanfrageJson extends AbstractJson {

    //~ Instance fields --------------------------------------------------------

    private Integer kassenzeichen;
    private String emailAdresse;
    private String emailVerifikation;
    private Boolean emailVerifiziert;
    private Map<String, FlaecheAenderungJson> flaechen;
    private Map<String, GeoJsonObject> geometrien;
    private List<NachrichtJson> nachrichten;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AnfrageJson object.
     *
     * @param  kassenzeichen  DOCUMENT ME!
     */
    public AenderungsanfrageJson(final Integer kassenzeichen) {
        this(
            kassenzeichen,
            null,
            null,
            null,
            new HashMap<String, FlaecheAenderungJson>(),
            new HashMap<String, GeoJsonObject>(),
            new ArrayList<NachrichtJson>());
    }

    /**
     * Creates a new AenderungsanfrageJson object.
     *
     * @param  kassenzeichen     DOCUMENT ME!
     * @param  emailAdresse      DOCUMENT ME!
     * @param  emailVerifiziert  DOCUMENT ME!
     */
    public AenderungsanfrageJson(final Integer kassenzeichen,
            final String emailAdresse,
            final Boolean emailVerifiziert) {
        this(
            kassenzeichen,
            emailAdresse,
            null,
            emailVerifiziert,
            new HashMap<String, FlaecheAenderungJson>(),
            new HashMap<String, GeoJsonObject>(),
            new ArrayList<NachrichtJson>());
    }

    /**
     * Creates a new AenderungsanfrageJson object.
     *
     * @param  kassenzeichen  DOCUMENT ME!
     * @param  flaechen       DOCUMENT ME!
     * @param  geometrien     DOCUMENT ME!
     * @param  nachrichten    DOCUMENT ME!
     */
    public AenderungsanfrageJson(final Integer kassenzeichen,
            final Map<String, FlaecheAenderungJson> flaechen,
            final Map<String, GeoJsonObject> geometrien,
            final List<NachrichtJson> nachrichten) {
        this(kassenzeichen, null, null, null, flaechen, geometrien, nachrichten);
    }
}
