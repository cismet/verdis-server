/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.verdis.server.utils.aenderungsanfrage;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public class BemerkungJson {

        //~ Instance fields ----------------------------------------------------

        private String buerger;
        private String anhang;
        private String sachbearbeiter;
        private BemerkungJson bemerkung;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new BemerkungJson object.
         *
         * @param  buerger  DOCUMENT ME!
         */
        public BemerkungJson(final String buerger) {
            this(buerger, null, null, null);
        }

        /**
         * Creates a new BemerkungJson object.
         *
         * @param  buerger  DOCUMENT ME!
         * @param  anhang   DOCUMENT ME!
         */
        public BemerkungJson(final String buerger, final String anhang) {
            this(buerger, anhang, null, null);
        }

        /**
         * Creates a new BemerkungJson object.
         *
         * @param  buerger         DOCUMENT ME!
         * @param  anhang          DOCUMENT ME!
         * @param  sachbearbeiter  DOCUMENT ME!
         */
        public BemerkungJson(final String buerger, final String anhang, final String sachbearbeiter) {
            this(buerger, anhang, sachbearbeiter, null);
        }
        
        public static class Deserializer extends StdDeserializer<BemerkungJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new BemerkungJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(BemerkungJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public BemerkungJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final String buerger = on.has("buerger") ? on.get("buerger").textValue() : null;
            final String sachbearbeiter = on.has("sachbearbeiter") ? on.get("sachbearbeiter").textValue() : null;
            final String anhang = on.has("anhang") ? on.get("anhang").textValue() : null;
            final BemerkungJson bemerkung = on.has("bemerkung")
                ? objectMapper.treeToValue(on.get("bemerkung"), BemerkungJson.class) : null;
            if ((buerger == null) && (sachbearbeiter == null)) {
                throw new RuntimeException("invalid BemerkungJson: neither buerger nor sachbearbeiter is set");
            }
            return new BemerkungJson(buerger, anhang, sachbearbeiter, bemerkung);
        }
    }
    }
