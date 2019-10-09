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

import de.cismet.verdis.server.json.aenderungsanfrage.FlaecheFlaechenartJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class FlaecheFlaechenartDeserializer extends StdDeserializer<FlaecheFlaechenartJson> {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaecheJsonDeserializer object.
     *
     * @param  objectMapper  DOCUMENT ME!
     */
    public FlaecheFlaechenartDeserializer(final ObjectMapper objectMapper) {
        super(FlaecheFlaechenartJson.class);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public FlaecheFlaechenartJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
        JsonProcessingException {
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
