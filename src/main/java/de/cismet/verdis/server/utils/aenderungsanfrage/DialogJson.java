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

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
public class DialogJson {

    //~ Instance fields --------------------------------------------------------

    private NachrichtJson buerger;
    private NachrichtJson sachbearbeiter;
    private DialogJson next;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DialogJson object.
     *
     * @param  buerger  DOCUMENT ME!
     */
    public DialogJson(final NachrichtJson buerger) {
        this(buerger, null, null);
    }

    /**
     * Creates a new DialogJson object.
     *
     * @param  buerger         DOCUMENT ME!
     * @param  sachbearbeiter  DOCUMENT ME!
     */
    public DialogJson(final NachrichtJson buerger, final NachrichtJson sachbearbeiter) {
        this(buerger, sachbearbeiter, null);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<DialogJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Deserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(DialogJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public DialogJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final NachrichtJson buerger = on.has("buerger")
                ? objectMapper.treeToValue(on.get("buerger"), NachrichtJson.class) : null;
            final NachrichtJson sachbearbeiter = on.has("sachbearbeiter")
                ? objectMapper.treeToValue(on.get("sachbearbeiter"), NachrichtJson.class) : null;
            final DialogJson next = on.has("next") ? objectMapper.treeToValue(on.get("next"), DialogJson.class) : null;
            if ((buerger == null) && (sachbearbeiter == null)) {
                throw new RuntimeException(
                    "invalid BemerkungJson: neither bemerkungBuerger nor bemerkungSachbearbeiter is set");
            }
            return new DialogJson(buerger, sachbearbeiter, next);
        }
    }
}
