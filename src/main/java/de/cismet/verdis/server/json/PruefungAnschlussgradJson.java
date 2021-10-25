/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.json;

import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class PruefungAnschlussgradJson extends PruefungJson<FlaecheAnschlussgradJson> {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PruefungAnschlussgradJson object.
     */
    public PruefungAnschlussgradJson() {
        this(true, null, null, null);
    }

    /**
     * Creates a new PruefungAnschlussgradJson object.
     *
     * @param  value  DOCUMENT ME!
     */
    public PruefungAnschlussgradJson(final FlaecheAnschlussgradJson value) {
        this(true, value, null, null);
    }

    /**
     * Creates a new PruefungAnschlussgradJson object.
     *
     * @param  pending    DOCUMENT ME!
     * @param  value      DOCUMENT ME!
     * @param  von        DOCUMENT ME!
     * @param  timestamp  DOCUMENT ME!
     */
    public PruefungAnschlussgradJson(final Boolean pending,
            final FlaecheAnschlussgradJson value,
            final String von,
            final Date timestamp) {
        super(pending, value, von, timestamp);
    }
}
