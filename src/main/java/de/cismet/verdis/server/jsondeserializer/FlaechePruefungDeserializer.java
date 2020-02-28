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

import de.cismet.verdis.server.json.FlaechePruefungJson;
import de.cismet.verdis.server.json.PruefungAnschlussgradJson;
import de.cismet.verdis.server.json.PruefungFlaechenartJson;
import de.cismet.verdis.server.json.PruefungGroesseJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class FlaechePruefungDeserializer extends StdDeserializer<FlaechePruefungJson> {

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper objectMapper;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaecheJsonDeserializer object.
     *
     * @param  objectMapper  DOCUMENT ME!
     */
    public FlaechePruefungDeserializer(final ObjectMapper objectMapper) {
        super(FlaechePruefungJson.class);
        this.objectMapper = objectMapper;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public FlaechePruefungJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
        JsonProcessingException {
        final ObjectNode on = jp.readValueAsTree();
        final PruefungGroesseJson pruefungGroesse = on.has("groesse")
            ? objectMapper.treeToValue(on.get("groesse"), PruefungGroesseJson.class) : null;
        final PruefungFlaechenartJson pruefungFlaechenart = on.has("flaechenart")
            ? objectMapper.treeToValue(on.get("flaechenart"), PruefungFlaechenartJson.class) : null;
        final PruefungAnschlussgradJson pruefungAnschlussgrad = on.has("anschlussgrad")
            ? objectMapper.treeToValue(on.get("anschlussgrad"), PruefungAnschlussgradJson.class) : null;
        if ((pruefungGroesse == null) && (pruefungFlaechenart == null) && (pruefungAnschlussgrad == null)) {
            throw new RuntimeException(
                "invalid FlaechePruefungJson: neither anschlussgrad nor flaechenart nor groesse is set");
        }
        return new FlaechePruefungJson(pruefungGroesse, pruefungFlaechenart, pruefungAnschlussgrad);
    }
}
