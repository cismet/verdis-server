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
public class AenderungsanfrageStatusSearchStatement extends AbstractCidsServerSearch
        implements RestApiCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(AenderungsanfrageStatusSearchStatement.class);

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum SearchMode {

        //~ Enum constants -----------------------------------------------------

        AND, OR,
    }

    //~ Instance fields --------------------------------------------------------

    @Getter @Setter private String schluessel;

    @Getter private final SearchInfo searchInfo;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AenderungsanfrageSearchStatement object.
     */
    public AenderungsanfrageStatusSearchStatement() {
        searchInfo = new SearchInfo();
        searchInfo.setKey(this.getClass().getName());
        searchInfo.setName(this.getClass().getSimpleName());
        searchInfo.setDescription("Search for AenderungsanfrageStatus");

        final List<SearchParameterInfo> parameterDescription = new LinkedList<>();
        final SearchParameterInfo searchParameterInfo = new SearchParameterInfo();

        searchParameterInfo.setKey("schluessel");
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
     * @param  schluessel  stacId DOCUMENT ME!
     */
    public AenderungsanfrageStatusSearchStatement(final String schluessel) {
        this();
        this.schluessel = schluessel;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<MetaObjectNode> performServerSearch() {
        try {
            final Collection<String> froms = new ArrayList<>();
            final Collection<String> wheres = new ArrayList<>();

            froms.add(VerdisConstants.MC.AENDERUNGSANFRAGE_STATUS + " AS s");
            final String from = String.join(" LEFT JOIN ", froms);
            final String where;

            if (getSchluessel() != null) {
                wheres.add("s.schluessel ILIKE '" + getSchluessel() + "'");
            }
            if (wheres.isEmpty()) {
                where = "WHERE true";
            } else {
                where = "WHERE " + String.join(" AND ", wheres);
            }

            final String cidSubQuery = "SELECT id "
                        + "FROM cs_class "
                        + "WHERE table_name ILIKE '" + VerdisConstants.MC.AENDERUNGSANFRAGE_STATUS + "'";
            final String query = "SELECT (" + cidSubQuery + ") as cid, s.id as oid "
                        + "FROM " + from + " "
                        + where;

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
        } catch (final Exception ex) {
            LOG.error("problem during AenderungsanfrageStatus search", ex); // NOI18N
            return null;
        }
    }
}
