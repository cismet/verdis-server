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
import de.cismet.verdis.server.json.AenderungsanfrageResultJson;
import de.cismet.verdis.server.json.FlaecheAenderungJson;
import de.cismet.verdis.server.json.NachrichtJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class AenderungsanfrageResultDeserializer extends StdDeserializer<AenderungsanfrageResultJson> {

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper objectMapper;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AnfrageJsonDeserializer object.
     *
     * @param  objectMapper  DOCUMENT ME!
     */
    public AenderungsanfrageResultDeserializer(final ObjectMapper objectMapper) {
        super(AenderungsanfrageJson.class);
        this.objectMapper = objectMapper;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public AenderungsanfrageResultJson deserialize(final JsonParser jp, final DeserializationContext dc)
            throws IOException, JsonProcessingException {
        final ObjectNode on = jp.readValueAsTree();
        final AenderungsanfrageJson aenderungsanfrage = on.has("aenderungsanfrage")
            ? objectMapper.treeToValue(on.get("aenderungsanfrage"), AenderungsanfrageJson.class) : null;
        final AenderungsanfrageResultJson.ResultStatus resultStatus = on.has("resultStatus")
            ? AenderungsanfrageResultJson.ResultStatus.valueOf(on.get("resultStatus").textValue()) : null;
        final String errorMessage = on.has("errorMessage") ? on.get("errorMessage").asText() : null;

        if (resultStatus == null) {
            throw new RuntimeException("invalid AenderungsanfrageResultJson: resultStatus is missing");
        }
        return new AenderungsanfrageResultJson(resultStatus, aenderungsanfrage, errorMessage);
    }
}
