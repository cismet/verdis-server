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
package de.cismet.verdis.server.utils.aenderungsanfrage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FlaecheJson {

    //~ Instance fields --------------------------------------------------------

    private Double groesse;
    private String anschlussgrad;
    private String flaechenart;
    private PruefungJson pruefung;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  pruefung  DOCUMENT ME!
     */
    public void addPruefung(final PruefungJson pruefung) {
        final PruefungJson lastPruefung = getLastPruefung();
        if (lastPruefung == null) {
            setPruefung(pruefung);
        } else {
            lastPruefung.setNext(pruefung);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PruefungJson getLastPruefung() {
        PruefungJson lastPruefung = getPruefung();
        if (lastPruefung != null) {
            while (lastPruefung.getNext() != null) {
                lastPruefung = lastPruefung.getNext();
            }
        }
        return lastPruefung;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<FlaecheJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(FlaecheJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public FlaecheJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final String anschlussgrad = on.has("anschlussgrad") ? on.get("anschlussgrad").textValue() : null;
            final String flaechenart = on.has("flaechenart") ? on.get("flaechenart").textValue() : null;
            final Double groesse = on.has("groesse") ? on.get("groesse").doubleValue() : null;
            final PruefungJson pruefung = on.has("pruefung")
                ? objectMapper.treeToValue(on.get("pruefung"), PruefungJson.class) : null;
            if ((anschlussgrad == null) && (flaechenart == null) && (groesse == null)) {
                throw new RuntimeException(
                    "invalid BemerkungJson: neither anschlussgrad nor flaechenart nor groesse is set");
            }
            if ((groesse != null) && (groesse < 0)) {
                throw new RuntimeException("invalid BemerkungJson: groesse can't be negative");
            }
            // TODO: check for valid anschlussgrad
            // TODO: check for valid flaechenart
            return new FlaecheJson(groesse, anschlussgrad, flaechenart, pruefung);
        }
    }
}
