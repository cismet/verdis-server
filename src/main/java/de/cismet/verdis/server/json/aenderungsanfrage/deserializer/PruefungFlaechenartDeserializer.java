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
package de.cismet.verdis.server.json.aenderungsanfrage.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import java.util.Date;

import de.cismet.verdis.server.json.aenderungsanfrage.FlaecheFlaechenartJson;
import de.cismet.verdis.server.json.aenderungsanfrage.PruefungFlaechenartJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class PruefungFlaechenartDeserializer extends StdDeserializer<PruefungFlaechenartJson> {

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper objectMapper;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Deserializer object.
     *
     * @param  objectMapper  DOCUMENT ME!
     */
    public PruefungFlaechenartDeserializer(final ObjectMapper objectMapper) {
        super(PruefungFlaechenartJson.class);

        this.objectMapper = objectMapper;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public PruefungFlaechenartJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
        JsonProcessingException {
        final ObjectNode on = jp.readValueAsTree();
        final FlaecheFlaechenartJson value = on.has("value")
            ? objectMapper.treeToValue(on.get("value"), FlaecheFlaechenartJson.class) : null;
        final String von = on.has("von") ? on.get("von").textValue() : null;
        final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
        if ((value == null)) {
            throw new RuntimeException("invalid StatusJson: value is not set");
        }
        return new PruefungFlaechenartJson(value, von, timestamp);
    }
}
