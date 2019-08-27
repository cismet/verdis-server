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
public class PruefungJson {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Status {

        //~ Enum constants -----------------------------------------------------

        NONE, ACCEPTED, REJECTED
    }

    //~ Instance fields --------------------------------------------------------

    private Status status;
    private String von;
    private Date timestamp;
    private PruefungJson next;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StatusJson object.
     *
     * @param  status     DOCUMENT ME!
     * @param  von        DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     */
    public PruefungJson(final Status status, final String von, final Date timestamp) {
        this(status, von, timestamp, null);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<PruefungJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Deserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(PruefungJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public PruefungJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final Status status = on.has("status") ? Status.valueOf(on.get("status").textValue()) : null;
            final String von = on.has("von") ? on.get("von").textValue() : null;
            final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
            final PruefungJson next = on.has("next") ? objectMapper.treeToValue(on.get("next"), PruefungJson.class)
                                                     : null;
            if ((status == null)) {
                throw new RuntimeException("invalid StatusJson: status is not set");
            }
            return new PruefungJson(status, von, timestamp, next);
        }
    }
}
