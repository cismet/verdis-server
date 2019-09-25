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
public class FlaecheFlaechenartJson {

    //~ Instance fields --------------------------------------------------------

    private String art;
    @JsonProperty("art_abkuerzung")
    private String artAbkuerzung;

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<FlaecheFlaechenartJson> {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(FlaecheFlaechenartJson.class);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public FlaecheFlaechenartJson deserialize(final JsonParser jp, final DeserializationContext dc)
                throws IOException, JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final String art = on.has("art") ? on.get("art").asText() : null;
            final String artAbkuerzung = on.has("art_abkuerzung") ? on.get("art_abkuerzung").asText() : null;
            if ((art == null) || (artAbkuerzung == null)) {
                throw new RuntimeException(
                    "invalid FlaecheFlaechenartJson: art or artAbkuerzung can't be null");
            }
            return new FlaecheFlaechenartJson(art, artAbkuerzung);
        }
    }
}
