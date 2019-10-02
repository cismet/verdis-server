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
    private O anfrage;
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
         * @param  anfrage    DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Groesse(final Integer anfrage, final String von, final Date timestamp) {
            super(false, anfrage, von, timestamp);
        }

        /**
         * Creates a new PruefungGroesseJson object.
         *
         * @param  pending    DOCUMENT ME!
         * @param  anfrage    DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Groesse(final boolean pending, final Integer anfrage, final String von, final Date timestamp) {
            super(pending, anfrage, von, timestamp);
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
                final Integer anfrage = on.has("anfrage") ? on.get("anfrage").intValue() : null;
                final String von = on.has("von") ? on.get("von").textValue() : null;
                final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
                if ((anfrage == null)) {
                    throw new RuntimeException("invalid StatusJson: status is not set");
                }
                return new Groesse(anfrage, von, timestamp);
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
         * @param  anfrage    DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Flaechenart(final FlaecheFlaechenartJson anfrage, final String von, final Date timestamp) {
            this(false, anfrage, von, timestamp);
        }

        /**
         * Creates a new PruefungFlaechenartJson object.
         *
         * @param  pending    DOCUMENT ME!
         * @param  anfrage    DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Flaechenart(final boolean pending,
                final FlaecheFlaechenartJson anfrage,
                final String von,
                final Date timestamp) {
            super(pending, anfrage, von, timestamp);
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
                final FlaecheFlaechenartJson anfrage = on.has("anfrage")
                    ? objectMapper.treeToValue(on.get("anfrage"), FlaecheFlaechenartJson.class) : null;
                final String von = on.has("von") ? on.get("von").textValue() : null;
                final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
                if ((anfrage == null)) {
                    throw new RuntimeException("invalid StatusJson: status is not set");
                }
                return new Flaechenart(anfrage, von, timestamp);
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
         * @param  anfrage    DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Anschlussgrad(final FlaecheAnschlussgradJson anfrage, final String von, final Date timestamp) {
            this(false, anfrage, von, timestamp);
        }

        /**
         * Creates a new PruefungAnschlussgradJson object.
         *
         * @param  pending    DOCUMENT ME!
         * @param  anfrage    DOCUMENT ME!
         * @param  von        DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         */
        public Anschlussgrad(final boolean pending,
                final FlaecheAnschlussgradJson anfrage,
                final String von,
                final Date timestamp) {
            super(pending, anfrage, von, timestamp);
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
                final FlaecheAnschlussgradJson anfrage = on.has("anfrage")
                    ? objectMapper.treeToValue(on.get("anfrage"), FlaecheAnschlussgradJson.class) : null;
                final String von = on.has("von") ? on.get("von").textValue() : null;
                final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
                if ((anfrage == null)) {
                    throw new RuntimeException("invalid StatusJson: status is not set");
                }
                return new Anschlussgrad(anfrage, von, timestamp);
            }
        }
    }
}
