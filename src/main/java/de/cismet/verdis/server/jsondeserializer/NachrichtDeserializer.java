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

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import de.cismet.verdis.server.json.NachrichtAnhangJson;
import de.cismet.verdis.server.json.NachrichtJson;
import de.cismet.verdis.server.json.NachrichtParameterJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NachrichtDeserializer extends StdDeserializer<NachrichtJson> {

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper objectMapper;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Deserializer object.
     *
     * @param  objectMapper  DOCUMENT ME!
     */
    public NachrichtDeserializer(final ObjectMapper objectMapper) {
        super(NachrichtJson.class);
        this.objectMapper = objectMapper;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public NachrichtJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
        JsonProcessingException {
        final ObjectNode on = jp.readValueAsTree();
        final String identifier = on.has("identifier") ? on.get("identifier").textValue() : null;
        final Boolean draft = on.has("draft") ? on.get("draft").asBoolean() : null;
        final NachrichtJson.Typ typ = on.has("typ") ? NachrichtJson.Typ.valueOf(on.get("typ").textValue()) : null;
        final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
        final Integer order = on.has("order") ? on.get("order").asInt() : null;
        final String absender = on.has("absender") ? on.get("absender").textValue() : null;
        final String nachricht = on.has("nachricht") ? on.get("nachricht").textValue() : null;
        final NachrichtParameterJson nachrichtenParameter = on.has("nachrichtenParameter")
            ? objectMapper.treeToValue(on.get("nachrichtenParameter"), NachrichtParameterJson.class) : null;
        final List<NachrichtAnhangJson> anhang = new ArrayList<>();
        if (on.has("anhang") && on.get("anhang").isArray()) {
            final Iterator<JsonNode> iterator = on.get("anhang").iterator();
            while (iterator.hasNext()) {
                anhang.add(objectMapper.treeToValue(iterator.next(), NachrichtAnhangJson.class));
            }
        }
        if ((nachricht == null) && (timestamp == null)) {
            throw new RuntimeException(
                "invalid NachrichtJson: neither nachricht nor timestamp is set");
        }
        return new NachrichtJson(
                identifier,
                draft,
                typ,
                timestamp,
                order,
                nachricht,
                nachrichtenParameter,
                absender,
                anhang);
    }
}
