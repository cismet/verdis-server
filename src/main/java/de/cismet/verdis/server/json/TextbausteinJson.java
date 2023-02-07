/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Getter
@Setter
public class TextbausteinJson extends AbstractJson {

    //~ Instance fields --------------------------------------------------------

    @JsonProperty private String titel;
    @JsonProperty private String text;
    @JsonProperty private String kategorie;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TextBausteinJson object.
     */
    public TextbausteinJson() {
        this(null, null, null);
    }

    /**
     * Creates a new TextBausteinJson object.
     *
     * @param  titel      DOCUMENT ME!
     * @param  text       DOCUMENT ME!
     * @param  kategorie  DOCUMENT ME!
     */
    public TextbausteinJson(@JsonProperty("titel") final String titel,
            @JsonProperty("text") final String text,
            @JsonProperty("kategorie") final String kategorie) {
        this.titel = titel;
        this.text = text;
        this.kategorie = kategorie;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return getTitel();
    }
}
