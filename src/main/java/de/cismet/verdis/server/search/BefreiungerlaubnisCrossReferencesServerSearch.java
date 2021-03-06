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

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cids.server.search.AbstractCidsServerSearch;

import de.cismet.verdis.commons.constants.VerdisConstants;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */

public class BefreiungerlaubnisCrossReferencesServerSearch extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(BefreiungerlaubnisCrossReferencesServerSearch.class);

    //~ Instance fields --------------------------------------------------------

    String searchQuery = "";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaechenCrossReferencesServerSearch object.
     *
     * @param  kzNummer  DOCUMENT ME!
     */
    public BefreiungerlaubnisCrossReferencesServerSearch(final int kzNummer) {
        searchQuery = "SELECT  "
                    + "   kassenzeichen1." + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " AS kz1, "
                    + "   befreiungerlaubnis." + VerdisConstants.PROP.BEFREIUNGERLAUBNIS.ID + " AS bid, "
                    + "   befreiungerlaubnis." + VerdisConstants.PROP.BEFREIUNGERLAUBNIS.AKTENZEICHEN
                    + " AS aktenzeichen1, "
                    + "   kassenzeichen2." + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " AS kz2 "
                    + "FROM "
                    + "   " + VerdisConstants.MC.KASSENZEICHEN + " AS kassenzeichen1, "
                    + "   " + VerdisConstants.MC.KASSENZEICHEN + " AS kassenzeichen2, "
                    + "   " + VerdisConstants.MC.KANALANSCHLUSS + " AS kanalanschluss1, "
                    + "   " + VerdisConstants.MC.KANALANSCHLUSS + " AS kanalanschluss2, "
                    + "   " + VerdisConstants.MC.BEFREIUNGERLAUBNISARRAY + " AS befreiungerlaubnisarray1, "
                    + "   " + VerdisConstants.MC.BEFREIUNGERLAUBNISARRAY + " AS befreiungerlaubnisarray2, "
                    + "   " + VerdisConstants.MC.BEFREIUNGERLAUBNIS + " AS befreiungerlaubnis "
                    + "WHERE  "
                    + "       kassenzeichen1." + VerdisConstants.PROP.KASSENZEICHEN.KANALANSCHLUSS
                    + " = kanalanschluss1.id "
                    + "   AND kassenzeichen2." + VerdisConstants.PROP.KASSENZEICHEN.KANALANSCHLUSS
                    + " = kanalanschluss2.id "
                    + "   AND befreiungerlaubnisarray1.kanalanschluss_reference = kanalanschluss1.id "
                    + "   AND befreiungerlaubnisarray2.kanalanschluss_reference = kanalanschluss2.id "
                    + "   AND befreiungerlaubnisarray2.befreiungerlaubnis = befreiungerlaubnisarray1.befreiungerlaubnis "
                    + "   AND befreiungerlaubnis." + VerdisConstants.PROP.BEFREIUNGERLAUBNIS.ID
                    + " = befreiungerlaubnisarray1.befreiungerlaubnis "
                    + "   AND NOT befreiungerlaubnisarray2.kanalanschluss_reference = befreiungerlaubnisarray1.kanalanschluss_reference "
                    + "   AND kassenzeichen1." + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER + " ="
                    + kzNummer;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);
        if (ms != null) {
            try {
                final ArrayList<ArrayList> lists = ms.performCustomSearch(searchQuery);
                return lists;
            } catch (RemoteException ex) {
                LOG.fatal("error while performing custom server search", ex);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return searchQuery;
    }
}
