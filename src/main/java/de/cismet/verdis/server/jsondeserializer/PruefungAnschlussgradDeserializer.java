/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.jsondeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import java.util.Date;

import de.cismet.verdis.server.json.FlaecheAnschlussgradJson;
import de.cismet.verdis.server.json.PruefungAnschlussgradJson;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class PruefungAnschlussgradDeserializer extends StdDeserializer<PruefungAnschlussgradJson> {

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper objectMapper;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Deserializer object.
     *
     * @param  objectMapper  DOCUMENT ME!
     */
    public PruefungAnschlussgradDeserializer(final ObjectMapper objectMapper) {
        super(PruefungAnschlussgradJson.class);

        this.objectMapper = objectMapper;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public PruefungAnschlussgradJson deserialize(final JsonParser jp, final DeserializationContext dc)
            throws IOException, JsonProcessingException {
        final ObjectNode on = jp.readValueAsTree();
        final FlaecheAnschlussgradJson value = on.has("value")
            ? objectMapper.treeToValue(on.get("value"), FlaecheAnschlussgradJson.class) : null;
        final String von = on.has("von") ? on.get("von").textValue() : null;
        final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
        if ((value == null)) {
            throw new RuntimeException("invalid StatusJson: status is not set");
        }
        return new PruefungAnschlussgradJson(value, von, timestamp);
    }
}
