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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
public class NachrichtJson {

    //~ Instance fields --------------------------------------------------------

    private String nachricht;
    private Date timestamp;
    private String anhang;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NachrichtJson object.
     *
     * @param  nachricht  DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     */
    public NachrichtJson(final String nachricht, final Date timestamp) {
        this(nachricht, timestamp, null);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<NachrichtJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Deserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(NachrichtJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public NachrichtJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final String nachricht = on.has("nachricht") ? on.get("nachricht").textValue() : null;
            final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
            final String anhang = on.has("anhang") ? on.get("anhang").textValue() : null;
            if ((nachricht == null) && (timestamp == null)) {
                throw new RuntimeException(
                    "invalid NachrichtJson: neither nachricht nor timestamp is set");
            }
            return new NachrichtJson(nachricht, timestamp, anhang);
        }
    }
}
