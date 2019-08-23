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

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
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
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = RestApiCidsServerSearch.class)
public class KassenzeichenSearchStatement extends AbstractCidsServerSearch implements MetaObjectNodeServerSearch,
    RestApiCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(KassenzeichenSearchStatement.class);

    //~ Instance fields --------------------------------------------------------

    @Getter @Setter private String searchString;

    @Getter private final SearchInfo searchInfo;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenSearchStatement object.
     */
    public KassenzeichenSearchStatement() {
        this.searchString = "-1";

        searchInfo = new SearchInfo();
        searchInfo.setKey(this.getClass().getName());
        searchInfo.setName(this.getClass().getSimpleName());
        searchInfo.setDescription("Search for Kassenzeichen-Objects by Kassenzeichen");

        final List<SearchParameterInfo> parameterDescription = new LinkedList<SearchParameterInfo>();
        final SearchParameterInfo searchParameterInfo;

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("searchString");
        searchParameterInfo.setType(Type.STRING);
        parameterDescription.add(searchParameterInfo);

        searchInfo.setParameterDescription(parameterDescription);

        final SearchParameterInfo resultParameterInfo = new SearchParameterInfo();
        resultParameterInfo.setKey("return");
        resultParameterInfo.setArray(true);
        resultParameterInfo.setType(Type.NODE);
        searchInfo.setResultDescription(resultParameterInfo);
    }

    /**
     * Creates a new KassenzeichenSearchStatement object.
     *
     * @param  searchString  DOCUMENT ME!
     */
    public KassenzeichenSearchStatement(final String searchString) {
        this();
        if (searchString != null) {
            this.searchString = searchString;
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<MetaObjectNode> performServerSearch() {
        try {
            final String sql;
            try {
                final int kassZInt = new Integer(searchString);
            } catch (Exception e) {
                searchString = "-1";
            }

            if (searchString.length() == 6) {
                sql = "SELECT (select id from cs_class where table_name ilike '"
                            + VerdisConstants.MC.KASSENZEICHEN
                            + "') as cid, id as oid FROM kassenzeichen WHERE " // NOI18N
                            + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER
                            + "/10 = "                                         // NOI18N
                            + searchString;
            } else {
                sql = "SELECT (select id from cs_class where table_name ilike '"
                            + VerdisConstants.MC.KASSENZEICHEN
                            + "') as cid, id as oid FROM kassenzeichen WHERE " // NOI18N
                            + VerdisConstants.PROP.KASSENZEICHEN.KASSENZEICHENNUMMER
                            + " = "                                            // NOI18N
                            + searchString;
            }

            final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);

            final List<MetaObjectNode> result = new ArrayList<MetaObjectNode>();
            final ArrayList<ArrayList> searchResult = ms.performCustomSearch(sql);
            LOG.info(sql);
            for (final ArrayList al : searchResult) {
                final int cid = (Integer)al.get(0);
                final int oid = (Integer)al.get(1);
                final MetaObjectNode mon = new MetaObjectNode(VerdisConstants.DOMAIN, oid, cid, "", null, null); // TODO: Check4CashedGeomAndLightweightJson
                result.add(mon);
            }

            return result;
        } catch (final Exception e) {
            LOG.error("problem during kassenzeichen search", e); // NOI18N

            return null;
        }
    }

    @Override
    public SearchInfo getSearchInfo() {
        return searchInfo;
    }
}
