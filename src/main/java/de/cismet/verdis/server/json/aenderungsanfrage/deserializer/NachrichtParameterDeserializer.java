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

import de.cismet.verdis.server.json.aenderungsanfrage.FlaecheAnschlussgradJson;
import de.cismet.verdis.server.json.aenderungsanfrage.FlaecheFlaechenartJson;
import de.cismet.verdis.server.json.aenderungsanfrage.NachrichtJson;
import de.cismet.verdis.server.json.aenderungsanfrage.NachrichtParameterJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NachrichtParameterDeserializer extends StdDeserializer<NachrichtParameterJson> {

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper objectMapper;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Deserializer object.
     *
     * @param  objectMapper  DOCUMENT ME!
     */
    public NachrichtParameterDeserializer(final ObjectMapper objectMapper) {
        super(NachrichtJson.class);
        this.objectMapper = objectMapper;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public NachrichtParameterJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
        JsonProcessingException {
        final ObjectNode on = jp.readValueAsTree();

        final NachrichtParameterJson.Type type = on.has("type")
            ? NachrichtParameterJson.Type.valueOf(on.get("type").textValue()) : null;
        final String flaeche = on.has("flaeche") ? on.get("flaeche").asText() : null;
        final Integer groesse = on.has("groesse") ? on.get("groesse").asInt() : null;
        final FlaecheFlaechenartJson flaechenart = on.has("flaechenart")
            ? objectMapper.treeToValue(on.get("flaechenart"), FlaecheFlaechenartJson.class) : null;
        final FlaecheAnschlussgradJson anschlussgrad = on.has("anschlussgrad")
            ? objectMapper.treeToValue(on.get("anschlussgrad"), FlaecheAnschlussgradJson.class) : null;

        if ((groesse == null) && (flaechenart == null) && (anschlussgrad == null)) {
            throw new RuntimeException(
                "invalid NachrichtSystemParametersJson: neither groesse nor flaechenart nor anschlussgrad is set");
        }
        return new NachrichtParameterJson(type, flaeche, groesse, flaechenart, anschlussgrad);
    }
}
