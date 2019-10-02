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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class NachrichtJson extends AbstractJson {

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

    private Boolean draft;
    private Typ typ;
    private Date timestamp;
    private String nachricht;
    private NachrichtParameterJson nachrichtenParameter;
    private String absender;
    private List<NachrichtAnhangJson> anhang;

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
            final Boolean draft = on.has("draft") ? on.get("draft").asBoolean() : null;
            final Typ typ = on.has("typ") ? Typ.valueOf(on.get("typ").textValue()) : null;
            final Date timestamp = on.has("timestamp") ? new Date(on.get("timestamp").longValue()) : null;
            final String absender = on.has("absender") ? on.get("absender").textValue() : null;
            final String nachricht = on.has("nachricht") ? on.get("nachricht").textValue() : null;
            final NachrichtParameterJson nachrichtenParameter = on.has("nachrichtenParameter")
                ? objectMapper.treeToValue(on.get("nachrichtenParameter"), NachrichtParameterJson.class) : null;
            final List<NachrichtAnhangJson> anhang = new ArrayList<>();
            if (on.has("anhang") && on.get("anhang").isArray()) {
                final Iterator<JsonNode> iterator = on.get("anhang").iterator();
                while (iterator.hasNext()) {
                    anhang.add(objectMapper.treeToValue(iterator.next(), NachrichtAnhangJson.class));
                }
            }
            if ((nachricht == null) && (timestamp == null)) {
                throw new RuntimeException(
                    "invalid NachrichtJson: neither nachricht nor timestamp is set");
            }
            return new NachrichtJson(draft, typ, timestamp, nachricht, nachrichtenParameter, absender, anhang);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class System extends NachrichtJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new System object.
         *
         * @param  timestamp             DOCUMENT ME!
         * @param  nachrichtenParameter  DOCUMENT ME!
         * @param  verursacher           DOCUMENT ME!
         */
        public System(final Date timestamp,
                final NachrichtParameterJson nachrichtenParameter,
                final String verursacher) {
            super(
                null,
                Typ.SYSTEM,
                timestamp,
                null,
                nachrichtenParameter,
                verursacher,
                new ArrayList<NachrichtAnhangJson>());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Buerger extends NachrichtJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Buerger object.
         *
         * @param  timestamp  DOCUMENT ME!
         * @param  nachricht  DOCUMENT ME!
         * @param  absender   DOCUMENT ME!
         */
        public Buerger(final Date timestamp, final String nachricht, final String absender) {
            super(null, Typ.CITIZEN, timestamp, nachricht, null, absender, new ArrayList<NachrichtAnhangJson>());
        }

        /**
         * Creates a new Buerger object.
         *
         * @param  timestamp  DOCUMENT ME!
         * @param  nachricht  DOCUMENT ME!
         * @param  absender   DOCUMENT ME!
         * @param  anhang     DOCUMENT ME!
         */
        public Buerger(final Date timestamp,
                final String nachricht,
                final String absender,
                final List<NachrichtAnhangJson> anhang) {
            super(null, Typ.CITIZEN, timestamp, nachricht, null, absender, anhang);
        }

        /**
         * Creates a new Buerger object.
         *
         * @param  timestamp  DOCUMENT ME!
         * @param  nachricht  DOCUMENT ME!
         * @param  absender   DOCUMENT ME!
         * @param  draft      DOCUMENT ME!
         */
        public Buerger(final Date timestamp, final String nachricht, final String absender, final boolean draft) {
            super(draft, Typ.CITIZEN, timestamp, nachricht, null, absender, new ArrayList<NachrichtAnhangJson>());
        }

        /**
         * Creates a new Buerger object.
         *
         * @param  timestamp  DOCUMENT ME!
         * @param  nachricht  DOCUMENT ME!
         * @param  absender   DOCUMENT ME!
         * @param  anhang     DOCUMENT ME!
         * @param  draft      DOCUMENT ME!
         */
        public Buerger(final Date timestamp,
                final String nachricht,
                final String absender,
                final List<NachrichtAnhangJson> anhang,
                final boolean draft) {
            super(draft, Typ.CITIZEN, timestamp, nachricht, null, absender, anhang);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Sachberarbeiter extends NachrichtJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Sachberarbeiter object.
         *
         * @param  timestamp  DOCUMENT ME!
         * @param  nachricht  DOCUMENT ME!
         * @param  absender   DOCUMENT ME!
         */
        public Sachberarbeiter(final Date timestamp, final String nachricht, final String absender) {
            super(null, Typ.CLERK, timestamp, nachricht, null, absender, new ArrayList<NachrichtAnhangJson>());
        }

        /**
         * Creates a new Sachberarbeiter object.
         *
         * @param  draft      DOCUMENT ME!
         * @param  timestamp  DOCUMENT ME!
         * @param  nachricht  DOCUMENT ME!
         * @param  absender   DOCUMENT ME!
         */
        public Sachberarbeiter(final boolean draft,
                final Date timestamp,
                final String nachricht,
                final String absender) {
            super(draft, Typ.CLERK, timestamp, nachricht, null, absender, new ArrayList<NachrichtAnhangJson>());
        }
    }
}
