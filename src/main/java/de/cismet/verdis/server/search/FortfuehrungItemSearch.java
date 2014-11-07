/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import de.cismet.cids.server.search.AbstractCidsServerSearch;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class FortfuehrungItemSearch extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(FortfuehrungItemSearch.class);

    //~ Instance fields --------------------------------------------------------

    private Date fromDate = null;
    private Date toDate = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FortfuehrungItemSearch object.
     *
     * @param  fromDate  DOCUMENT ME!
     * @param  toDate    DOCUMENT ME!
     */
    public FortfuehrungItemSearch(final Date fromDate, final Date toDate) {
        setFromDate(fromDate);
        setToDate(toDate);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fromDate  DOCUMENT ME!
     */
    private void setFromDate(final Date fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  toDate  DOCUMENT ME!
     */
    private void setToDate(final Date toDate) {
        this.toDate = toDate;
    }

    @Override
    public Collection performServerSearch() {
        final List<Object[]> items = new ArrayList<Object[]>();
        final String query = "SELECT lookup_alkis_ffn.id AS id "
                    + ", lookup_alkis_ffn.ffn AS ffn "
                    + ", lookup_ffn_anlassarten.anl_bezeichnung AS anlass_name "
                    + ", to_date(lookup_alkis_ffn.beginn, 'DD-Mon-YY') AS beginn_date "
                    + ", flurstueckskennzeichen_alt AS fs_alt "
                    + ", flurstueckskennzeichen_neu AS fs_neu "
                    + ", asText(geom.geo_field) AS geo_field "
                    + "FROM lookup_alkis_ffn "
                    + "LEFT JOIN lookup_ffn_anlassarten ON '\\\"' || lookup_ffn_anlassarten.anl_ffn || '\\\"' = lookup_alkis_ffn.anl_ffn "
                    + "LEFT JOIN flurstueck ON lookup_alkis_ffn.ffn = flurstueck.fortfuehrungsnummer "
                    + "LEFT JOIN geom ON geom.id = flurstueck.umschreibendes_rechteck "
                    + "WHERE geom.id IS NOT NULL AND "
                    + "to_date(lookup_alkis_ffn.beginn, 'DD-Mon-YY') BETWEEN '" + fromDate + "' AND '" + toDate + "';";

        final MetaService metaService = (MetaService)getActiveLocalServers().get("WUNDA_BLAU");

        try {
            for (final ArrayList fields : metaService.performCustomSearch(query)) {
                items.add(
                    new Object[] {
                        (Integer)fields.get(0),
                        (String)fields.get(1),
                        (String)fields.get(2),
                        (Date)fields.get(3),
                        (String)fields.get(4),
                        (String)fields.get(5),
                        (String)fields.get(6)
                    });
            }
        } catch (final Exception ex) {
            LOG.error("problem fortfuehrung item search", ex);
        }

        return items;
    }
}
