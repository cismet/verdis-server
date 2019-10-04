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

import de.cismet.verdis.server.utils.AenderungsanfrageUtils;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class NachrichtAnhangJson extends AbstractJson {

    //~ Instance fields --------------------------------------------------------

    private String name;
    private String uuid;

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
    public static NachrichtAnhangJson readValue(final String json) throws Exception {
        return AenderungsanfrageUtils.getInstance().getMapper().readValue(json, NachrichtAnhangJson.class);
    }

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
            final String uiud = on.has("uuid") ? on.get("uuid").textValue() : null;
            if ((uiud == null) && (uiud == null)) {
                throw new RuntimeException(
                    "invalid NachrichtAnhangJson: name and uid can't be null");
            }
            return new NachrichtAnhangJson(name, uiud);
        }
    }
}
