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
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = false)
public class FlaechePruefungJson extends AbstractJson {

    //~ Instance fields --------------------------------------------------------

    private PruefungJson.Groesse groesse;
    private PruefungJson.Flaechenart flaechenart;
    private PruefungJson.Anschlussgrad anschlussgrad;

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Groesse extends FlaechePruefungJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaechePruefungGroesseJson object.
         *
         * @param  pruefung  DOCUMENT ME!
         */
        public Groesse(final PruefungJson.Groesse pruefung) {
            super(pruefung, null, null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Flaechenart extends FlaechePruefungJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaechePruefungFlaechenartJson object.
         *
         * @param  pruefung  DOCUMENT ME!
         */
        public Flaechenart(final PruefungJson.Flaechenart pruefung) {
            super(null, pruefung, null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Anschlussgrad extends FlaechePruefungJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaechePruefungAnschlussgradJson object.
         *
         * @param  pruefung  DOCUMENT ME!
         */
        public Anschlussgrad(final PruefungJson.Anschlussgrad pruefung) {
            super(null, null, pruefung);
        }
    }
    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<FlaechePruefungJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(FlaechePruefungJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public FlaechePruefungJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final PruefungJson.Groesse pruefungGroesse = on.has("groesse")
                ? objectMapper.treeToValue(on.get("groesse"), PruefungJson.Groesse.class) : null;
            final PruefungJson.Flaechenart pruefungFlaechenart = on.has("flaechenart")
                ? objectMapper.treeToValue(on.get("flaechenart"), PruefungJson.Flaechenart.class) : null;
            final PruefungJson.Anschlussgrad pruefungAnschlussgrad = on.has("anschlussgrad")
                ? objectMapper.treeToValue(on.get("anschlussgrad"), PruefungJson.Anschlussgrad.class) : null;
            if ((pruefungGroesse == null) && (pruefungFlaechenart == null) && (pruefungAnschlussgrad == null)) {
                throw new RuntimeException(
                    "invalid FlaechePruefungJson: neither anschlussgrad nor flaechenart nor groesse is set");
            }
            return new FlaechePruefungJson(pruefungGroesse, pruefungFlaechenart, pruefungAnschlussgrad);
        }
    }
}
