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

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Typ {

        //~ Enum constants -----------------------------------------------------

        CLERK, CITIZEN, SYSTEM
    }

    //~ Instance fields --------------------------------------------------------

    private Typ typ;
    private Date timestamp;
    private String nachricht;
    private String absender;
    private String anhang;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NachrichtJson object.
     *
     * @param  timestamp  DOCUMENT ME!
     * @param  nachricht  DOCUMENT ME!
     */
    public NachrichtJson(final Date timestamp, final String nachricht) {
        this(Typ.SYSTEM, timestamp, nachricht, null, null);
    }

    /**
     * Creates a new NachrichtJson object.
     *
     * @param  typ        DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     * @param  nachricht  DOCUMENT ME!
     * @param  absender   DOCUMENT ME!
     */
    public NachrichtJson(final Typ typ, final Date timestamp, final String nachricht, final String absender) {
        this(typ, timestamp, nachricht, absender, null);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<NachrichtJson> {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Deserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(NachrichtJson.class);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public NachrichtJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final Typ typ = on.has("typ") ? Typ.valueOf(on.get("typ").textValue()) : null;
            final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
            final String absender = on.has("absender") ? on.get("absender").textValue() : null;
            final String nachricht = on.has("nachricht") ? on.get("nachricht").textValue() : null;
            final String anhang = on.has("anhang") ? on.get("anhang").textValue() : null;
            if ((nachricht == null) && (timestamp == null)) {
                throw new RuntimeException(
                    "invalid NachrichtJson: neither nachricht nor timestamp is set");
            }
            return new NachrichtJson(typ, timestamp, nachricht, absender, anhang);
        }
    }
}
