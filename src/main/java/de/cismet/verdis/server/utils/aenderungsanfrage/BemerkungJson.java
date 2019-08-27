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
public class BemerkungJson {

    //~ Instance fields --------------------------------------------------------

    private String bemerkungBuerger;
    private String anhangBuerger;
    private String bemerkungSachbearbeiter;
    private BemerkungJson next;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BemerkungJson object.
     *
     * @param  buerger  DOCUMENT ME!
     */
    public BemerkungJson(final String buerger) {
        this(buerger, null, null, null);
    }

    /**
     * Creates a new BemerkungJson object.
     *
     * @param  buerger  DOCUMENT ME!
     * @param  anhang   DOCUMENT ME!
     */
    public BemerkungJson(final String buerger, final String anhang) {
        this(buerger, anhang, null, null);
    }

    /**
     * Creates a new BemerkungJson object.
     *
     * @param  buerger         DOCUMENT ME!
     * @param  anhang          DOCUMENT ME!
     * @param  sachbearbeiter  DOCUMENT ME!
     */
    public BemerkungJson(final String buerger, final String anhang, final String sachbearbeiter) {
        this(buerger, anhang, sachbearbeiter, null);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<BemerkungJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new BemerkungJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(BemerkungJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public BemerkungJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final String bemerkungBuerger = on.has("bemerkungBuerger") ? on.get("bemerkungBuerger").textValue() : null;
            final String bemerkungSachbearbeiter = on.has("bemerkungSachbearbeiter")
                ? on.get("bemerkungSachbearbeiter").textValue() : null;
            final String anhangBuerger = on.has("anhangBuerger") ? on.get("anhangBuerger").textValue() : null;
            final BemerkungJson next = on.has("next") ? objectMapper.treeToValue(on.get("next"), BemerkungJson.class)
                                                      : null;
            if ((bemerkungBuerger == null) && (bemerkungSachbearbeiter == null)) {
                throw new RuntimeException(
                    "invalid BemerkungJson: neither bemerkungBuerger nor bemerkungSachbearbeiter is set");
            }
            return new BemerkungJson(bemerkungBuerger, anhangBuerger, bemerkungSachbearbeiter, next);
        }
    }
}
