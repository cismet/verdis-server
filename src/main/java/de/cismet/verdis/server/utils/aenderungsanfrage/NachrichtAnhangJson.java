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
public class NachrichtAnhangJson {

    //~ Instance fields --------------------------------------------------------

    private String name;
    private String uid;

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<NachrichtAnhangJson> {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Deserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(NachrichtAnhangJson.class);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public NachrichtAnhangJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final String name = on.has("name") ? on.get("name").textValue() : null;
            final String uid = on.has("uid") ? on.get("uid").textValue() : null;
            if ((uid == null) && (uid == null)) {
                throw new RuntimeException(
                    "invalid NachrichtAnhangJson: name and uid can't be null");
            }
            return new NachrichtAnhangJson(name, uid);
        }
    }
}
