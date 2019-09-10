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
public class FlaechePruefungJson {

    //~ Instance fields --------------------------------------------------------

    private PruefungJson groesse;
    private PruefungJson flaechenart;
    private PruefungJson anschlussgrad;

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<FlaechePruefungJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(FlaechePruefungJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public FlaechePruefungJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final PruefungJson pruefungGroesse = on.has("groesse")
                ? objectMapper.treeToValue(on.get("groesse"), PruefungJson.class) : null;
            final PruefungJson pruefungFlaechenart = on.has("flaechenart")
                ? objectMapper.treeToValue(on.get("flaechenart"), PruefungJson.class) : null;
            final PruefungJson pruefungAnschlussgrad = on.has("anschlussgrad")
                ? objectMapper.treeToValue(on.get("anschlussgrad"), PruefungJson.class) : null;
            if ((pruefungGroesse == null) && (pruefungFlaechenart == null) && (pruefungAnschlussgrad == null)) {
                throw new RuntimeException(
                    "invalid FlaechePruefungJson: neither anschlussgrad nor flaechenart nor groesse is set");
            }
            return new FlaechePruefungJson(pruefungGroesse, pruefungFlaechenart, pruefungAnschlussgrad);
        }
    }
}
