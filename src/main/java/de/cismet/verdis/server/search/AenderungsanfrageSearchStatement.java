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

import de.cismet.cidsx.base.types.Type;

import de.cismet.cidsx.server.api.types.SearchInfo;
import de.cismet.cidsx.server.api.types.SearchParameterInfo;
import de.cismet.cidsx.server.search.RestApiCidsServerSearch;

import de.cismet.verdis.commons.constants.VerdisConstants;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = RestApiCidsServerSearch.class)
public class AenderungsanfrageSearchStatement extends AbstractCidsServerSearch implements RestApiCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(AenderungsanfrageSearchStatement.class);

    //~ Instance fields --------------------------------------------------------

    @Getter @Setter private String stacHash;

    @Getter private final SearchInfo searchInfo;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AenderungsanfrageSearchStatement object.
     */
    public AenderungsanfrageSearchStatement() {
        searchInfo = new SearchInfo();
        searchInfo.setKey(this.getClass().getName());
        searchInfo.setName(this.getClass().getSimpleName());
        searchInfo.setDescription("Search for Aenderungsanfrage");

        final List<SearchParameterInfo> parameterDescription = new LinkedList<>();
        final SearchParameterInfo searchParameterInfo;

        searchParameterInfo = new SearchParameterInfo();
        searchParameterInfo.setKey("stacHash");
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
     * Creates a new AenderungsanfrageSearchStatement object.
     *
     * @param  stacHash  DOCUMENT ME!
     */
    public AenderungsanfrageSearchStatement(final String stacHash) {
        this();
        this.stacHash = stacHash;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<MetaObjectNode> performServerSearch() {
        try {
            if (stacHash == null) {
                return null;
            } else {
                final String cidSubQuery = "SELECT id "
                            + "FROM cs_class "
                            + "WHERE table_name ILIKE '" + VerdisConstants.MC.AENDERUNGSANFRAGE + "'";
                final String query = "SELECT (" + cidSubQuery + ") as cid, a.id as oid "
                            + "FROM " + VerdisConstants.MC.AENDERUNGSANFRAGE + " AS a "
                            + "LEFT JOIN cs_stac ON a." + VerdisConstants.PROP.AENDERUNGSANFRAGE.STAC_ID
                            + " = cs_stac.id "
                            + "WHERE cs_stac.thehash LIKE '" + stacHash + "'";

                final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);
                final List<MetaObjectNode> result = new ArrayList<>();
                final ArrayList<ArrayList> searchResult = ms.performCustomSearch(query + ";");
                LOG.info(query);
                for (final ArrayList al : searchResult) {
                    result.add(new MetaObjectNode(
                            VerdisConstants.DOMAIN,
                            (Integer)al.get(1),
                            (Integer)al.get(0),
                            "",
                            null,
                            null));
                }
                return result;
            }
        } catch (final Exception ex) {
            LOG.error("problem during Aenderungsanfrage search", ex); // NOI18N
            return null;
        }
    }

    @Override
    public SearchInfo getSearchInfo() {
        return searchInfo;
    }
}
