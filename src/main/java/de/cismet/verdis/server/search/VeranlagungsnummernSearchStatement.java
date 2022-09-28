/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaObjectNode;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.cismet.cids.server.search.AbstractCidsServerSearch;
import de.cismet.cids.server.search.MetaObjectNodeServerSearch;

import de.cismet.cidsx.base.types.Type;

import de.cismet.cidsx.server.api.types.SearchInfo;
import de.cismet.cidsx.server.api.types.SearchParameterInfo;
import de.cismet.cidsx.server.search.RestApiCidsServerSearch;

import de.cismet.verdis.commons.constants.VerdisConstants;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = RestApiCidsServerSearch.class)
public class VeranlagungsnummernSearchStatement extends AbstractCidsServerSearch implements MetaObjectNodeServerSearch,
    RestApiCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(VeranlagungsnummernSearchStatement.class);

    //~ Instance fields --------------------------------------------------------

    private final SearchInfo searchInfo;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new VeranlagungsnummernSearchStatement object.
     */
    public VeranlagungsnummernSearchStatement() {
        searchInfo = new SearchInfo();
        searchInfo.setKey(this.getClass().getName());
        searchInfo.setName(this.getClass().getSimpleName());
        searchInfo.setDescription("Search for Veranlagungsnummer-Objects");

        final SearchParameterInfo resultParameterInfo = new SearchParameterInfo();
        resultParameterInfo.setKey("return");
        resultParameterInfo.setArray(true);
        resultParameterInfo.setType(Type.NODE);
        searchInfo.setResultDescription(resultParameterInfo);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<MetaObjectNode> performServerSearch() {
        try {
            final String sql = "SELECT (SELECT id FROM cs_class WHERE name ILIKE '"
                        + VerdisConstants.MC.VERANLAGUNGSNUMMER + "'), " + VerdisConstants.PROP.VERANLAGUNGSNUMMER.ID
                        + " " + "FROM " + VerdisConstants.MC.VERANLAGUNGSNUMMER;
            final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);
            final List<MetaObjectNode> result = new ArrayList<>();
            final ArrayList<ArrayList> searchResult = ms.performCustomSearch(sql);
            for (final ArrayList al : searchResult) {
                final int cid = (Integer)al.get(0);
                final int oid = (Integer)al.get(1);
                final MetaObjectNode mon = new MetaObjectNode(VerdisConstants.DOMAIN, oid, cid, "", null, null);
                result.add(mon);
            }
            return result;
        } catch (final Exception e) {
            LOG.error("problem during VeranlagungsnummernSearchStatement", e); // NOI18N
            return null;
        }
    }

    @Override
    public SearchInfo getSearchInfo() {
        return searchInfo;
    }
}
