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
package de.cismet.verdis.server.utils.aenderungsanfrage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AnfrageJson {

    //~ Instance fields --------------------------------------------------------

    private Integer kassenzeichen;
    private Map<String, FlaecheJson> flaechen;
    private List<NachrichtJson> nachrichten;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AnfrageJson object.
     *
     * @param  kassenzeichen  DOCUMENT ME!
     */
    public AnfrageJson(final Integer kassenzeichen) {
        this(kassenzeichen, null, new ArrayList<NachrichtJson>());
    }

    /**
     * Creates a new AnfrageJson object.
     *
     * @param  kassenzeichen  DOCUMENT ME!
     * @param  flaechen       DOCUMENT ME!
     */
    public AnfrageJson(final Integer kassenzeichen, final Map<String, FlaecheJson> flaechen) {
        this(kassenzeichen, new HashMap<String, FlaecheJson>(), new ArrayList<NachrichtJson>());
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
    public static AnfrageJson readValue(final String json) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(PruefungGroesseJson.class, new PruefungGroesseJson.Deserializer(mapper));
        module.addDeserializer(PruefungFlaechenartJson.class, new PruefungFlaechenartJson.Deserializer(mapper));
        module.addDeserializer(PruefungAnschlussgradJson.class, new PruefungAnschlussgradJson.Deserializer(mapper));
        module.addDeserializer(PruefungGroesseJson.class, new PruefungGroesseJson.Deserializer(mapper));
        module.addDeserializer(FlaechePruefungJson.class, new FlaechePruefungJson.Deserializer(mapper));
        module.addDeserializer(FlaecheJson.class, new FlaecheJson.Deserializer(mapper));
        module.addDeserializer(AnschlussgradJson.class, new AnschlussgradJson.Deserializer(mapper));
        module.addDeserializer(FlaechenartJson.class, new FlaechenartJson.Deserializer(mapper));
        module.addDeserializer(NachrichtAnhangJson.class, new NachrichtAnhangJson.Deserializer(mapper));
        module.addDeserializer(NachrichtJson.class, new NachrichtJson.Deserializer(mapper));
        module.addDeserializer(AnfrageJson.class, new AnfrageJson.Deserializer(mapper));
        mapper.registerModule(module);

        return mapper.readValue(json, AnfrageJson.class);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<AnfrageJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AnfrageJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(AnfrageJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public AnfrageJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
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
            final Map<String, FlaecheJson> flaechen;
            if (on.has("flaechen") && on.get("flaechen").isObject()) {
                flaechen = new HashMap<>();
                final Iterator<Map.Entry<String, JsonNode>> fieldIterator = on.get("flaechen").fields();
                while (fieldIterator.hasNext()) {
                    final Map.Entry<String, JsonNode> fieldEntry = fieldIterator.next();
                    final String bezeichnung = fieldEntry.getKey();
                    // TODO: check for valid bezeichnung.
                    flaechen.put(bezeichnung, objectMapper.treeToValue(fieldEntry.getValue(), FlaecheJson.class));
                }
            } else {
                flaechen = null;
            }

            if (kassenzeichen == null) {
                throw new RuntimeException("invalid AnfrageJson: kassenzeichen is missing");
            }
            return new AnfrageJson(kassenzeichen, flaechen, nachrichten);
        }
    }
}
