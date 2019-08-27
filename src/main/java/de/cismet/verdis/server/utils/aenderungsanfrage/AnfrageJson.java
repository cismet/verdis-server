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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AnfrageJson {

    //~ Instance fields --------------------------------------------------------

    private Integer kassenzeichen;
    private Map<String, FlaecheJson> flaechen;
    private DialogJson nachrichten;
    private PruefungJson pruefung;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AnfrageJson object.
     *
     * @param  kassenzeichen  DOCUMENT ME!
     */
    public AnfrageJson(final Integer kassenzeichen) {
        this(kassenzeichen, null, null, null);
    }

    /**
     * Creates a new AnfrageJson object.
     *
     * @param  kassenzeichen  DOCUMENT ME!
     * @param  flaechen       DOCUMENT ME!
     */
    public AnfrageJson(final Integer kassenzeichen, final Map<String, FlaecheJson> flaechen) {
        this(kassenzeichen, flaechen, null, null);
    }

    /**
     * Creates a new AnfrageJson object.
     *
     * @param  kassenzeichen  DOCUMENT ME!
     * @param  flaechen       DOCUMENT ME!
     * @param  bemerkung      DOCUMENT ME!
     */
    public AnfrageJson(final Integer kassenzeichen,
            final Map<String, FlaecheJson> flaechen,
            final DialogJson bemerkung) {
        this(kassenzeichen, flaechen, bemerkung, null);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  pruefung  DOCUMENT ME!
     */
    public void addPruefung(final PruefungJson pruefung) {
        if (pruefung == null) {
            setPruefung(pruefung);
        } else {
            PruefungJson lastPruefungJson = getPruefung();
            while (lastPruefungJson.getNext() != null) {
                lastPruefungJson = lastPruefungJson.getNext();
            }
            lastPruefungJson.setNext(pruefung);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @JsonIgnore
    public PruefungJson getLastPruefung() {
        PruefungJson lastPruefung = getPruefung();
        if (lastPruefung != null) {
            while (lastPruefung.getNext() != null) {
                lastPruefung = lastPruefung.getNext();
            }
        }
        return lastPruefung;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dialog  DOCUMENT ME!
     */
    public void addDialog(final DialogJson dialog) {
        final DialogJson lastDialog = getLastDialog();
        if (lastDialog == null) {
            setNachrichten(dialog);
        } else {
            lastDialog.setNext(dialog);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  buerger         DOCUMENT ME!
     * @param  sachbearbeiter  DOCUMENT ME!
     */
    public void addDialog(final NachrichtJson buerger, final NachrichtJson sachbearbeiter) {
        addDialog(new DialogJson(buerger, sachbearbeiter));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @JsonIgnore
    public DialogJson getLastDialog() {
        DialogJson lastDialog = getNachrichten();
        if (lastDialog != null) {
            while (lastDialog.getNext() != null) {
                lastDialog = lastDialog.getNext();
            }
        }
        return lastDialog;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Deserializer extends StdDeserializer<AnfrageJson> {

        //~ Instance fields ----------------------------------------------------

        private final ObjectMapper objectMapper;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AnfrageJsonDeserializer object.
         *
         * @param  objectMapper  DOCUMENT ME!
         */
        public Deserializer(final ObjectMapper objectMapper) {
            super(AnfrageJson.class);
            this.objectMapper = objectMapper;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public AnfrageJson deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
            JsonProcessingException {
            final ObjectNode on = jp.readValueAsTree();
            final Integer kassenzeichen = on.has("kassenzeichen") ? on.get("kassenzeichen").asInt() : null;
            final DialogJson bemerkung = on.has("nachrichten")
                ? objectMapper.treeToValue(on.get("nachrichten"), DialogJson.class) : null;
            final PruefungJson pruefung = on.has("pruefung")
                ? objectMapper.treeToValue(on.get("pruefung"), PruefungJson.class) : null;
            final Map<String, FlaecheJson> flaechen;
            if (on.has("flaechen") && on.get("flaechen").isObject()) {
                flaechen = new HashMap<>();
                final Iterator<Map.Entry<String, JsonNode>> fieldIterator = on.get("flaechen").fields();
                while (fieldIterator.hasNext()) {
                    final Map.Entry<String, JsonNode> fieldEntry = fieldIterator.next();
                    final String bezeichnung = fieldEntry.getKey();
                    // TODO: check for valid bezeichnung.
                    flaechen.put(bezeichnung, objectMapper.treeToValue(fieldEntry.getValue(), FlaecheJson.class));
                }
            } else {
                flaechen = null;
            }

            if (kassenzeichen == null) {
                throw new RuntimeException("invalid AnfrageJson: kassenzeichen is missing");
            }
            return new AnfrageJson(kassenzeichen, flaechen, bemerkung, pruefung);
        }
    }
}
