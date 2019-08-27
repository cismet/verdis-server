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
import java.io.IOException;
import lombok.AllArgsConstructor;
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public class FlaecheJson {

        //~ Instance fields ----------------------------------------------------

        private Double groesse;
        private String anschlussgrad;
        private String flaechenart;
        private BemerkungJson bemerkung;
        private String pruefungStatus;

        
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
            final BemerkungJson bemerkung = on.has("bemerkung")
                ? objectMapper.treeToValue(on.get("bemerkung"), BemerkungJson.class) : null;
            final String pruefungStatus = on.has("pruefungStatus") ? on.get("pruefungStatus").textValue() : null;
            if ((anschlussgrad == null) && (flaechenart == null) && (groesse == null)) {
                throw new RuntimeException(
                    "invalid BemerkungJson: neither anschlussgrad nor flaechenart nor groesse is set");
            }
            if ((groesse != null) && (groesse < 0)) {
                throw new RuntimeException("invalid BemerkungJson: groesse can't be negative");
            }
            // TODO: check for valid anschlussgrad
            // TODO: check for valid flaechenart
            return new FlaecheJson(groesse, anschlussgrad, flaechenart, bemerkung, pruefungStatus);
        }
    }
    }
