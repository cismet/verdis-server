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

import de.cismet.verdis.server.utils.AenderungsanfrageUtils;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
public class NachrichtParameterJson extends AbstractJson {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Type {

        //~ Enum constants -----------------------------------------------------

        CHANGED {

            @Override
            public String toString() {
                return "changed";
            }
        },
        REJECTED {

            @Override
            public String toString() {
                return "rejected";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    private Type type;
    private String flaeche;
    private Integer groesse;
    private FlaecheFlaechenartJson flaechenart;
    private FlaecheAnschlussgradJson anschlussgrad;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   json  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static NachrichtParameterJson readValue(final String json) throws Exception {
        return AenderungsanfrageUtils.getInstance().getMapper().readValue(json, NachrichtParameterJson.class);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<NachrichtParameterJson> {

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
        public NachrichtParameterJson deserialize(final JsonParser jp, final DeserializationContext dc)
                throws IOException, JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();

            final Type type = on.has("type") ? Type.valueOf(on.get("type").textValue()) : null;
            final String flaeche = on.has("flaeche") ? on.get("flaeche").asText() : null;
            final Integer groesse = on.has("groesse") ? on.get("groesse").asInt() : null;
            final FlaecheFlaechenartJson flaechenart = on.has("flaechenart")
                ? objectMapper.treeToValue(on.get("flaechenart"), FlaecheFlaechenartJson.class) : null;
            final FlaecheAnschlussgradJson anschlussgrad = on.has("anschlussgrad")
                ? objectMapper.treeToValue(on.get("anschlussgrad"), FlaecheAnschlussgradJson.class) : null;

            if ((groesse == null) && (flaechenart == null) && (anschlussgrad == null)) {
                throw new RuntimeException(
                    "invalid NachrichtSystemParametersJson: neither groesse nor flaechenart nor anschlussgrad is set");
            }
            return new NachrichtParameterJson(type, flaeche, groesse, flaechenart, anschlussgrad);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Groesse extends NachrichtParameterJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Groesse object.
         *
         * @param  type     DOCUMENT ME!
         * @param  flaeche  DOCUMENT ME!
         * @param  groesse  DOCUMENT ME!
         */
        public Groesse(final Type type, final String flaeche, final Integer groesse) {
            super(type, flaeche, groesse, null, null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Flaechenart extends NachrichtParameterJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Flaechenart object.
         *
         * @param  type         DOCUMENT ME!
         * @param  flaeche      DOCUMENT ME!
         * @param  flaechenart  DOCUMENT ME!
         */
        public Flaechenart(final Type type,
                final String flaeche,
                final FlaecheFlaechenartJson flaechenart) {
            super(type, flaeche, null, flaechenart, null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Anschlussgrad extends NachrichtParameterJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Anschlussgrad object.
         *
         * @param  type           DOCUMENT ME!
         * @param  flaeche        DOCUMENT ME!
         * @param  anschlussgrad  DOCUMENT ME!
         */
        public Anschlussgrad(final Type type,
                final String flaeche,
                final FlaecheAnschlussgradJson anschlussgrad) {
            super(type, flaeche, null, null, anschlussgrad);
        }
    }
}
