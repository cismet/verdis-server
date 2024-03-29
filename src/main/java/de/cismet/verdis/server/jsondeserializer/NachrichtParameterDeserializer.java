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

import de.cismet.verdis.server.json.FlaecheAnschlussgradJson;
import de.cismet.verdis.server.json.FlaecheFlaechenartJson;
import de.cismet.verdis.server.json.NachrichtJson;
import de.cismet.verdis.server.json.NachrichtParameterJson;
import de.cismet.verdis.server.utils.AenderungsanfrageUtils;

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
        final AenderungsanfrageUtils.Status status = on.has("status")
            ? AenderungsanfrageUtils.Status.valueOf(on.get("status").textValue()) : null;
        final String flaeche = on.has("flaeche") ? on.get("flaeche").asText() : null;
        final Integer groesse = on.has("groesse") ? on.get("groesse").asInt() : null;
        final FlaecheFlaechenartJson flaechenart = on.has("flaechenart")
            ? objectMapper.treeToValue(on.get("flaechenart"), FlaecheFlaechenartJson.class) : null;
        final FlaecheAnschlussgradJson anschlussgrad = on.has("anschlussgrad")
            ? objectMapper.treeToValue(on.get("anschlussgrad"), FlaecheAnschlussgradJson.class) : null;
        final Boolean benachrichtigt = on.has("benachrichtigt") ? on.get("benachrichtigt").booleanValue() : null;
        final Boolean verlaengern = on.has("verlaengert") ? on.get("verlaengert").booleanValue() : null;

        if (type == null) {
            throw new RuntimeException("invalid NachrichtSystemParametersJson: type has to be is set");
        }
        switch (type) {
            case STATUS: {
                if (status == null) {
                    throw new RuntimeException("invalid NachrichtSystemParametersJson: status has to be is set");
                }
            }
            break;
            case CHANGED:
            case REJECTED: {
                if ((groesse == null) && (flaechenart == null) && (anschlussgrad == null)) {
                    throw new RuntimeException(
                        "invalid NachrichtSystemParametersJson: neither groesse nor flaechenart nor anschlussgrad is set");
                }
            }
            break;
            case NOTIFY: {
                if (benachrichtigt == null) {
                    throw new RuntimeException("invalid NachrichtSystemParametersJson: benachrichtigt has to be set");
                }
            }
            break;
            case PROLONG: {
                if (verlaengern == null) {
                    throw new RuntimeException("invalid NachrichtSystemParametersJson: verlaengern has to be set");
                }
            }
            break;
        }
        return new NachrichtParameterJson(
                type,
                status,
                flaeche,
                groesse,
                flaechenart,
                anschlussgrad,
                benachrichtigt,
                verlaengern);
    }
}
