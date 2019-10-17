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
package de.cismet.verdis.server.jsondeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import de.cismet.verdis.server.json.StacOptionsDurationJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class StacOptionsDurationDeserializer extends StdDeserializer<StacOptionsDurationJson> {

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper objectMapper;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StacOptionsDurationDeserializer object.
     *
     * @param  objectMapper  DOCUMENT ME!
     */
    public StacOptionsDurationDeserializer(final ObjectMapper objectMapper) {
        super(StacOptionsDurationJson.class);
        this.objectMapper = objectMapper;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public StacOptionsDurationJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
        JsonProcessingException {
        final ObjectNode on = jp.readValueAsTree();
        final Integer value = on.has("value") ? on.get("value").asInt() : null;
        final StacOptionsDurationJson.Unit unit = on.has("unit")
            ? StacOptionsDurationJson.Unit.valueOf(on.get("unit").asText()) : null;

        if (value == null) {
            throw new RuntimeException("invalid StacOptionsDurationJson: value is missing");
        }
        if (unit == null) {
            throw new RuntimeException("invalid StacOptionsDurationJson: unit is missing");
        }
        return new StacOptionsDurationJson(unit, value);
    }
}
