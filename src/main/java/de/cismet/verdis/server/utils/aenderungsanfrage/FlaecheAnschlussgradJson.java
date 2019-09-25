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
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class FlaecheAnschlussgradJson {

    //~ Instance fields --------------------------------------------------------

    private String grad;
    @JsonProperty("grad_abkuerzung")
    private String gradAbkuerzung;

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<FlaecheAnschlussgradJson> {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(FlaecheAnschlussgradJson.class);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public FlaecheAnschlussgradJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final String grad = on.has("grad") ? on.get("grad").asText() : null;
            final String gradAbkuerzung = on.has("grad_abkuerzung") ? on.get("grad_abkuerzung").asText() : null;
            if ((grad == null) || (gradAbkuerzung == null)) {
                throw new RuntimeException(
                    "invalid FlaecheAnschlussgradJson: grad or gradAbkuerzung can't be null");
            }
            return new FlaecheAnschlussgradJson(grad, gradAbkuerzung);
        }
    }
}
