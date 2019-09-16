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
@JsonIgnoreProperties({ "pending" })
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

    private transient boolean pending;
    private Typ typ;
    private Date timestamp;
    private String nachricht;
    private String absender;
    private NachrichtAnhangJson anhang;

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

    /**
     * Creates a new NachrichtJson object.
     *
     * @param  pending    DOCUMENT ME!
     * @param  typ        DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     * @param  nachricht  DOCUMENT ME!
     * @param  absender   DOCUMENT ME!
     */
    public NachrichtJson(final boolean pending,
            final Typ typ,
            final Date timestamp,
            final String nachricht,
            final String absender) {
        this(pending, typ, timestamp, nachricht, absender, null);
    }

    /**
     * Creates a new NachrichtJson object.
     *
     * @param  typ        DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     * @param  nachricht  DOCUMENT ME!
     * @param  absender   DOCUMENT ME!
     * @param  anhang     DOCUMENT ME!
     */
    public NachrichtJson(final Typ typ,
            final Date timestamp,
            final String nachricht,
            final String absender,
            final NachrichtAnhangJson anhang) {
        this(false, typ, timestamp, nachricht, absender, anhang);
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
            final Typ typ = on.has("typ") ? Typ.valueOf(on.get("typ").textValue()) : null;
            final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
            final String absender = on.has("absender") ? on.get("absender").textValue() : null;
            final String nachricht = on.has("nachricht") ? on.get("nachricht").textValue() : null;
            final NachrichtAnhangJson anhang = on.has("anhang")
                ? objectMapper.treeToValue(on.get("anhang"), NachrichtAnhangJson.class) : null;
            if ((nachricht == null) && (timestamp == null)) {
                throw new RuntimeException(
                    "invalid NachrichtJson: neither nachricht nor timestamp is set");
            }
            return new NachrichtJson(typ, timestamp, nachricht, absender, anhang);
        }
    }
}
