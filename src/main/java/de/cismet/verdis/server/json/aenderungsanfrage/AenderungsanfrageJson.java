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
package de.cismet.verdis.server.json.aenderungsanfrage;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.cismet.verdis.server.utils.AenderungsanfrageUtils;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AenderungsanfrageJson extends AbstractJson {

    //~ Instance fields --------------------------------------------------------

    private Integer kassenzeichen;
    private Map<String, FlaecheAenderungJson> flaechen;
    private List<NachrichtJson> nachrichten;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AnfrageJson object.
     *
     * @param  kassenzeichen  DOCUMENT ME!
     */
    public AenderungsanfrageJson(final Integer kassenzeichen) {
        this(kassenzeichen, new HashMap<String, FlaecheAenderungJson>(), new ArrayList<NachrichtJson>());
    }

    /**
     * Creates a new AnfrageJson object.
     *
     * @param  kassenzeichen  DOCUMENT ME!
     * @param  flaechen       DOCUMENT ME!
     */
    public AenderungsanfrageJson(final Integer kassenzeichen, final Map<String, FlaecheAenderungJson> flaechen) {
        this(kassenzeichen, new HashMap<String, FlaecheAenderungJson>(), new ArrayList<NachrichtJson>());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   json  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static AenderungsanfrageJson readValue(final String json) throws Exception {
        return AenderungsanfrageUtils.getInstance().getMapper().readValue(json, AenderungsanfrageJson.class);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<AenderungsanfrageJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AnfrageJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(AenderungsanfrageJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public AenderungsanfrageJson deserialize(final JsonParser jp, final DeserializationContext dc)
                throws IOException, JsonProcessingException {
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

            if (kassenzeichen == null) {
                throw new RuntimeException("invalid AnfrageJson: kassenzeichen is missing");
            }
            return new AenderungsanfrageJson(kassenzeichen, flaechen, nachrichten);
        }
    }
}
