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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
public class FlaecheAenderungJson extends AbstractJson {

    //~ Instance fields --------------------------------------------------------

    private Boolean draft;
    private Integer groesse;
    private FlaecheFlaechenartJson flaechenart;
    private FlaecheAnschlussgradJson anschlussgrad;
    private FlaechePruefungJson pruefung;

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Groesse extends FlaecheAenderungJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheAenderungGroesseJson object.
         *
         * @param  groesse  DOCUMENT ME!
         */
        public Groesse(final Integer groesse) {
            super(null, groesse, null, null, null);
        }

        /**
         * Creates a new FlaecheAenderungGroesseJson object.
         *
         * @param  groesse   DOCUMENT ME!
         * @param  pruefung  DOCUMENT ME!
         */
        public Groesse(final Integer groesse, final FlaechePruefungJson.Groesse pruefung) {
            super(null, groesse, null, null, pruefung);
        }

        /**
         * Creates a new FlaecheAenderungGroesseJson object.
         *
         * @param  draft    DOCUMENT ME!
         * @param  groesse  DOCUMENT ME!
         */
        public Groesse(final Boolean draft, final Integer groesse) {
            super(draft, groesse, null, null, null);
        }

        /**
         * Creates a new FlaecheAenderungGroesseJson object.
         *
         * @param  draft     DOCUMENT ME!
         * @param  groesse   DOCUMENT ME!
         * @param  pruefung  DOCUMENT ME!
         */
        public Groesse(final Boolean draft,
                final Integer groesse,
                final FlaechePruefungJson.Groesse pruefung) {
            super(draft, groesse, null, null, pruefung);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Flaechenart extends FlaecheAenderungJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheAenderungFlaechenartJson object.
         *
         * @param  flaechenart  DOCUMENT ME!
         */
        public Flaechenart(final FlaecheFlaechenartJson flaechenart) {
            super(null, null, flaechenart, null, null);
        }

        /**
         * Creates a new FlaecheAenderungFlaechenartJson object.
         *
         * @param  flaechenart  DOCUMENT ME!
         * @param  pruefung     DOCUMENT ME!
         */
        public Flaechenart(final FlaecheFlaechenartJson flaechenart,
                final FlaechePruefungJson.Flaechenart pruefung) {
            super(null, null, flaechenart, null, pruefung);
        }

        /**
         * Creates a new FlaecheAenderungFlaechenartJson object.
         *
         * @param  draft        DOCUMENT ME!
         * @param  flaechenart  DOCUMENT ME!
         */
        public Flaechenart(final Boolean draft, final FlaecheFlaechenartJson flaechenart) {
            super(draft, null, flaechenart, null, null);
        }

        /**
         * Creates a new FlaecheAenderungFlaechenartJson object.
         *
         * @param  draft        DOCUMENT ME!
         * @param  flaechenart  DOCUMENT ME!
         * @param  pruefung     DOCUMENT ME!
         */
        public Flaechenart(final Boolean draft,
                final FlaecheFlaechenartJson flaechenart,
                final FlaechePruefungJson.Flaechenart pruefung) {
            super(draft, null, flaechenart, null, pruefung);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Anschlussgrad extends FlaecheAenderungJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheAenderungAnschlussgradJson object.
         *
         * @param  anschlussgrad  DOCUMENT ME!
         */
        public Anschlussgrad(final FlaecheAnschlussgradJson anschlussgrad) {
            super(null, null, null, anschlussgrad, null);
        }

        /**
         * Creates a new FlaecheAenderungAnschlussgradJson object.
         *
         * @param  anschlussgrad  DOCUMENT ME!
         * @param  pruefung       DOCUMENT ME!
         */
        public Anschlussgrad(final FlaecheAnschlussgradJson anschlussgrad,
                final FlaechePruefungJson.Anschlussgrad pruefung) {
            super(null, null, null, anschlussgrad, pruefung);
        }

        /**
         * Creates a new FlaecheAenderungAnschlussgradJson object.
         *
         * @param  draft          DOCUMENT ME!
         * @param  anschlussgrad  DOCUMENT ME!
         */
        public Anschlussgrad(final Boolean draft, final FlaecheAnschlussgradJson anschlussgrad) {
            super(draft, null, null, anschlussgrad, null);
        }

        /**
         * Creates a new FlaecheAenderungAnschlussgradJson object.
         *
         * @param  draft          DOCUMENT ME!
         * @param  anschlussgrad  DOCUMENT ME!
         * @param  pruefung       DOCUMENT ME!
         */
        public Anschlussgrad(final Boolean draft,
                final FlaecheAnschlussgradJson anschlussgrad,
                final FlaechePruefungJson.Anschlussgrad pruefung) {
            super(draft, null, null, anschlussgrad, pruefung);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<FlaecheAenderungJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(FlaecheAenderungJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public FlaecheAenderungJson deserialize(final JsonParser jp, final DeserializationContext dc)
                throws IOException, JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final Boolean draft = on.has("draft") ? on.get("draft").asBoolean() : null;
            final Integer groesse = on.has("groesse") ? on.get("groesse").intValue() : null;
            final FlaecheFlaechenartJson flaechenart = on.has("flaechenart")
                ? objectMapper.treeToValue(on.get("flaechenart"), FlaecheFlaechenartJson.class) : null;
            final FlaecheAnschlussgradJson anschlussgrad = on.has("anschlussgrad")
                ? objectMapper.treeToValue(on.get("anschlussgrad"), FlaecheAnschlussgradJson.class) : null;
            final FlaechePruefungJson pruefung = on.has("pruefung")
                ? objectMapper.treeToValue(on.get("pruefung"), FlaechePruefungJson.class) : null;
            if ((anschlussgrad == null) && (flaechenart == null) && (groesse == null)) {
                throw new RuntimeException(
                    "invalid FlaecheJson: neither anschlussgrad nor flaechenart nor groesse is set");
            }
            if ((groesse != null) && (groesse < 0)) {
                throw new RuntimeException("invalid FlaecheJson: groesse can't be negative");
            }
            // TODO: check for valid anschlussgrad
            // TODO: check for valid flaechenart
            return new FlaecheAenderungJson(draft, groesse, flaechenart, anschlussgrad, pruefung);
        }
    }
}
