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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @param    <O>
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties({ "pending" })
@EqualsAndHashCode(callSuper = false)
public abstract class PruefungJson<O> extends AbstractJson {

    //~ Instance fields --------------------------------------------------------

    @JsonIgnore private transient Boolean pending;
    private O value;
    private String von;
    private Date timestamp;

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Groesse extends PruefungJson<Integer> {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PruefungGroesseJson object.
         *
         * @param  value      DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Groesse(final Integer value, final String von, final Date timestamp) {
            super(false, value, von, timestamp);
        }

        /**
         * Creates a new PruefungGroesseJson object.
         *
         * @param  pending    DOCUMENT ME!
         * @param  value      DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Groesse(final boolean pending, final Integer value, final String von, final Date timestamp) {
            super(pending, value, von, timestamp);
        }

        //~ Inner Classes ------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @version  $Revision$, $Date$
         */
        public static class Deserializer extends StdDeserializer<Groesse> {

            //~ Constructors ---------------------------------------------------

            /**
             * Creates a new Deserializer object.
             *
             * @param  objectMapper  DOCUMENT ME!
             */
            public Deserializer(final ObjectMapper objectMapper) {
                super(Groesse.class);
            }

            //~ Methods --------------------------------------------------------

            @Override
            public Groesse deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
                JsonProcessingException {
                final ObjectNode on = jp.readValueAsTree();
                final Integer value = on.has("value") ? on.get("value").intValue() : null;
                final String von = on.has("von") ? on.get("von").textValue() : null;
                final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
                if ((value == null)) {
                    throw new RuntimeException("invalid StatusJson: status is not set");
                }
                return new Groesse(value, von, timestamp);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Flaechenart extends PruefungJson<FlaecheFlaechenartJson> {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PruefungFlaechenartJson object.
         *
         * @param  value      DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Flaechenart(final FlaecheFlaechenartJson value, final String von, final Date timestamp) {
            this(false, value, von, timestamp);
        }

        /**
         * Creates a new PruefungFlaechenartJson object.
         *
         * @param  pending    DOCUMENT ME!
         * @param  value      DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Flaechenart(final boolean pending,
                final FlaecheFlaechenartJson value,
                final String von,
                final Date timestamp) {
            super(pending, value, von, timestamp);
        }

        //~ Inner Classes ------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @version  $Revision$, $Date$
         */
        public static class Deserializer extends StdDeserializer<Flaechenart> {

            //~ Instance fields ------------------------------------------------

            private final ObjectMapper objectMapper;

            //~ Constructors ---------------------------------------------------

            /**
             * Creates a new Deserializer object.
             *
             * @param  objectMapper  DOCUMENT ME!
             */
            public Deserializer(final ObjectMapper objectMapper) {
                super(Flaechenart.class);

                this.objectMapper = objectMapper;
            }

            //~ Methods --------------------------------------------------------

            @Override
            public Flaechenart deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
                JsonProcessingException {
                final ObjectNode on = jp.readValueAsTree();
                final FlaecheFlaechenartJson value = on.has("value")
                    ? objectMapper.treeToValue(on.get("value"), FlaecheFlaechenartJson.class) : null;
                final String von = on.has("von") ? on.get("von").textValue() : null;
                final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
                if ((value == null)) {
                    throw new RuntimeException("invalid StatusJson: status is not set");
                }
                return new Flaechenart(value, von, timestamp);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Anschlussgrad extends PruefungJson<FlaecheAnschlussgradJson> {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PruefungAnschlussgradJson object.
         *
         * @param  value      DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Anschlussgrad(final FlaecheAnschlussgradJson value, final String von, final Date timestamp) {
            this(false, value, von, timestamp);
        }

        /**
         * Creates a new PruefungAnschlussgradJson object.
         *
         * @param  pending    DOCUMENT ME!
         * @param  value      DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Anschlussgrad(final boolean pending,
                final FlaecheAnschlussgradJson value,
                final String von,
                final Date timestamp) {
            super(pending, value, von, timestamp);
        }

        //~ Inner Classes ------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @version  $Revision$, $Date$
         */
        public static class Deserializer extends StdDeserializer<Anschlussgrad> {

            //~ Instance fields ------------------------------------------------

            private final ObjectMapper objectMapper;

            //~ Constructors ---------------------------------------------------

            /**
             * Creates a new Deserializer object.
             *
             * @param  objectMapper  DOCUMENT ME!
             */
            public Deserializer(final ObjectMapper objectMapper) {
                super(Anschlussgrad.class);

                this.objectMapper = objectMapper;
            }

            //~ Methods --------------------------------------------------------

            @Override
            public Anschlussgrad deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
                JsonProcessingException {
                final ObjectNode on = jp.readValueAsTree();
                final FlaecheAnschlussgradJson value = on.has("value")
                    ? objectMapper.treeToValue(on.get("value"), FlaecheAnschlussgradJson.class) : null;
                final String von = on.has("von") ? on.get("von").textValue() : null;
                final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
                if ((value == null)) {
                    throw new RuntimeException("invalid StatusJson: status is not set");
                }
                return new Anschlussgrad(value, von, timestamp);
            }
        }
    }
}
