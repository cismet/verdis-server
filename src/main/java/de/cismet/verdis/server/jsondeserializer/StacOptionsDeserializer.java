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
import de.cismet.verdis.server.json.StacOptionsJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class StacOptionsDeserializer extends StdDeserializer<StacOptionsJson> {

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper objectMapper;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StacOptionsDeserializer object.
     *
     * @param  objectMapper  DOCUMENT ME!
     */
    public StacOptionsDeserializer(final ObjectMapper objectMapper) {
        super(StacOptionsJson.class);
        this.objectMapper = objectMapper;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public StacOptionsJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
        JsonProcessingException {
        final ObjectNode on = jp.readValueAsTree();
        final Integer classId = on.has("classId") ? on.get("classId").asInt() : null;
        final Integer kassenzeichenid = on.has("kassenzeichenid") ? on.get("kassenzeichenid").asInt() : null;
        final String creatorUserName = on.has("creatorUserName") ? on.get("creatorUserName").asText() : null;
        final StacOptionsDurationJson duration = on.has("duration")
            ? objectMapper.treeToValue(on.get("duration"), StacOptionsDurationJson.class) : null;

        if (classId == null) {
            throw new RuntimeException("invalid StacOptionsJson: classId is missing");
        }
        if (kassenzeichenid == null) {
            throw new RuntimeException("invalid StacOptionsJson: kassenzeichenid is missing");
        }
        return new StacOptionsJson(classId, kassenzeichenid, creatorUserName, duration);
    }
}
