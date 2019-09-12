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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class PruefungGroesseJson extends PruefungJson {

    //~ Constructors -----------------------------------------------------------

    public PruefungGroesseJson(final Integer anfrage, final String von, final Date timestamp) {
        this(false, anfrage, von, timestamp);
    }

    /**
     * Creates a new PruefungGroesseJson object.
     *
     * @param  anfrage    DOCUMENT ME!
     * @param  von        DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     */
    public PruefungGroesseJson(final boolean pending, final Integer anfrage, final String von, final Date timestamp) {
        super(pending, anfrage, von, timestamp);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<PruefungGroesseJson> {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Deserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(PruefungGroesseJson.class);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public PruefungGroesseJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final Integer anfrage = on.has("anfrage") ? on.get("anfrage").intValue() : null;
            final String von = on.has("von") ? on.get("von").textValue() : null;
            final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
            if ((anfrage == null)) {
                throw new RuntimeException("invalid StatusJson: status is not set");
            }
            return new PruefungGroesseJson(anfrage, von, timestamp);
        }
    }
}
