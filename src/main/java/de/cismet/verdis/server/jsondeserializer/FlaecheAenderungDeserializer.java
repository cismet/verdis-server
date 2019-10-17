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

import de.cismet.verdis.server.json.FlaecheAenderungJson;
import de.cismet.verdis.server.json.FlaecheAnschlussgradJson;
import de.cismet.verdis.server.json.FlaecheFlaechenartJson;
import de.cismet.verdis.server.json.FlaechePruefungJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class FlaecheAenderungDeserializer extends StdDeserializer<FlaecheAenderungJson> {

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper objectMapper;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaecheJsonDeserializer object.
     *
     * @param  objectMapper  DOCUMENT ME!
     */
    public FlaecheAenderungDeserializer(final ObjectMapper objectMapper) {
        super(FlaecheAenderungJson.class);
        this.objectMapper = objectMapper;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public FlaecheAenderungJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
        JsonProcessingException {
        final ObjectNode on = jp.readValueAsTree();
        final Boolean draft = on.has("draft") ? on.get("draft").asBoolean() : null;
        final Integer groesse = on.has("groesse") ? on.get("groesse").intValue() : null;
        final FlaecheFlaechenartJson flaechenart = on.has("flaechenart")
            ? objectMapper.treeToValue(on.get("flaechenart"), FlaecheFlaechenartJson.class) : null;
        final FlaecheAnschlussgradJson anschlussgrad = on.has("anschlussgrad")
            ? objectMapper.treeToValue(on.get("anschlussgrad"), FlaecheAnschlussgradJson.class) : null;
        final FlaechePruefungJson pruefung = on.has("pruefung")
            ? objectMapper.treeToValue(on.get("pruefung"), FlaechePruefungJson.class) : null;
        if ((anschlussgrad == null) && (flaechenart == null) && (groesse == null)) {
            throw new RuntimeException(
                "invalid FlaecheJson: neither anschlussgrad nor flaechenart nor groesse is set");
        }
        if ((groesse != null) && (groesse < 0)) {
            throw new RuntimeException("invalid FlaecheJson: groesse can't be negative");
        }
        // TODO: check for valid anschlussgrad
        // TODO: check for valid flaechenart
        return new FlaecheAenderungJson(draft, groesse, flaechenart, anschlussgrad, pruefung);
    }
}
