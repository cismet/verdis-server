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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.geojson.GeoJsonObject;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.cismet.verdis.server.json.AenderungsanfrageJson;
import de.cismet.verdis.server.json.FlaecheAenderungJson;
import de.cismet.verdis.server.json.NachrichtJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class AenderungsanfrageDeserializer extends StdDeserializer<AenderungsanfrageJson> {

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper objectMapper;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AnfrageJsonDeserializer object.
     *
     * @param  objectMapper  DOCUMENT ME!
     */
    public AenderungsanfrageDeserializer(final ObjectMapper objectMapper) {
        super(AenderungsanfrageJson.class);
        this.objectMapper = objectMapper;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public AenderungsanfrageJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
        JsonProcessingException {
        final ObjectNode on = jp.readValueAsTree();
        final Integer kassenzeichen = on.has("kassenzeichen") ? on.get("kassenzeichen").asInt() : null;
        final List<NachrichtJson> nachrichten = new ArrayList<>();
        if (on.has("nachrichten") && on.get("nachrichten").isArray()) {
            final Iterator<JsonNode> iterator = on.get("nachrichten").iterator();
            while (iterator.hasNext()) {
                nachrichten.add(objectMapper.treeToValue(iterator.next(), NachrichtJson.class));
            }
        }
        final Map<String, FlaecheAenderungJson> flaechen = new HashMap<>();
        if (on.has("flaechen") && on.get("flaechen").isObject()) {
            final Iterator<Map.Entry<String, JsonNode>> fieldIterator = on.get("flaechen").fields();
            while (fieldIterator.hasNext()) {
                final Map.Entry<String, JsonNode> fieldEntry = fieldIterator.next();
                final String bezeichnung = fieldEntry.getKey();
                // TODO: check for valid bezeichnung.
                flaechen.put(
                    bezeichnung,
                    objectMapper.treeToValue(fieldEntry.getValue(), FlaecheAenderungJson.class));
            }
        }
        final Map<String, GeoJsonObject> geometrien = new HashMap<>();
        if (on.has("geometrien") && on.get("geometrien").isObject()) {
            final Iterator<Map.Entry<String, JsonNode>> fieldIterator = on.get("geometrien").fields();
            while (fieldIterator.hasNext()) {
                final Map.Entry<String, JsonNode> fieldEntry = fieldIterator.next();
                final String bezeichnung = fieldEntry.getKey();
                geometrien.put(
                    bezeichnung,
                    objectMapper.treeToValue(fieldEntry.getValue(), GeoJsonObject.class));
            }
        }

        if (kassenzeichen == null) {
            throw new RuntimeException("invalid AnfrageJson: kassenzeichen is missing");
        }
        return new AenderungsanfrageJson(kassenzeichen, flaechen, geometrien, nachrichten);
    }
}
