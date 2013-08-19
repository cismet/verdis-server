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

import de.cismet.verdis.commons.constants.FortfuehrungAnlassPropertyConstants;
import de.cismet.verdis.commons.constants.FortfuehrungPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;
import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;

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

        final String query = "SELECT "
                    + "   fortfuehrung." + FortfuehrungPropertyConstants.PROP__ID + ", "
                    + "   fortfuehrung_anlass." + FortfuehrungAnlassPropertyConstants.PROP__NAME + " AS anlass_name, "
                    + "   fortfuehrung." + FortfuehrungPropertyConstants.PROP__BEGINN + ", "
                    + "   fortfuehrung." + FortfuehrungPropertyConstants.PROP__FLURSTUECK_ALT + ", "
                    + "   fortfuehrung." + FortfuehrungPropertyConstants.PROP__FLURSTUECK_NEU + ", "
                    + "   fortfuehrung." + FortfuehrungPropertyConstants.PROP__IST_ABGEARBEITET + " "
                    + "FROM " + VerdisMetaClassConstants.MC_FORTFUEHRUNG + " AS fortfuehrung "
                    + "LEFT JOIN " + VerdisMetaClassConstants.MC_FORTFUEHRUNG_ANLASS + " AS fortfuehrung_anlass "
                    + "ON fortfuehrung." + FortfuehrungPropertyConstants.PROP__ANLASS + " = fortfuehrung_anlass.id "
                    + "WHERE "
                    + "   fortfuehrung." + FortfuehrungPropertyConstants.PROP__BEGINN + " BETWEEN '" + fromDate
                    + "' AND '" + toDate + "';";

        final MetaService metaService = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);

        try {
            for (final ArrayList fields : metaService.performCustomSearch(query)) {
                items.add(
                    new Object[] {
                        (Integer)fields.get(0),
                        (String)fields.get(1),
                        (Date)fields.get(2),
                        (String)fields.get(3),
                        (String)fields.get(4),
                        (Boolean)fields.get(5)
                    });
            }
        } catch (Exception ex) {
            LOG.error("problem fortfuehrung item search", ex);
        }

        return items;
    }
}
