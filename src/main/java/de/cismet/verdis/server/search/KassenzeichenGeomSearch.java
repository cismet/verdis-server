/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.search;

import Sirius.server.middleware.interfaces.domainserver.MetaService;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.cismet.verdis.commons.constants.GeomPropertyConstants;
import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
import de.cismet.verdis.commons.constants.VerdisConstants;
import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class KassenzeichenGeomSearch extends GeomServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(KassenzeichenGeomSearch.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        final Geometry searchGeometry = getGeometry();

        if (searchGeometry != null) {
            final String sqlDerived = "SELECT "
                        + "    DISTINCT " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "."
                        + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kassenzeichennumer "
                        + "FROM "
                        + "    cs_attr_object_derived, "
                        + "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen, "
                        + "    " + VerdisMetaClassConstants.MC_GEOM + " AS geom "
                        + "WHERE "
                        + "    cs_attr_object_derived.class_id = 11 "
                        + "    AND cs_attr_object_derived.attr_class_id = 0 "
                        + "    AND kassenzeichen." + KassenzeichenPropertyConstants.PROP__ID
                        + " = cs_attr_object_derived.object_id "
                        + "    AND kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER
                        + " IS NOT NULL "
                        + "    AND geom." + GeomPropertyConstants.PROP__ID + " = cs_attr_object_derived.attr_object_id "
                        + "    AND GeomFromText('" + searchGeometry.toText() + "', " + searchGeometry.getSRID()
                        + ") && geom.geo_field "
                        + "    AND ST_Intersects(GeomFromText('" + searchGeometry.toText() + "', "
                        + searchGeometry.getSRID() + "), geom." + GeomPropertyConstants.PROP__GEO_FIELD + ") "
                        + "ORDER BY kassenzeichennumer ASC;";

            final MetaService metaService = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);

            // ids der kassenzeichen sammeln
            final Set<Integer> idSet = new HashSet<Integer>();
            if (LOG.isDebugEnabled()) {
                LOG.debug(sqlDerived);
            }
            try {
                for (final ArrayList fields : metaService.performCustomSearch(sqlDerived)) {
                    idSet.add((Integer)fields.get(0));
                }
            } catch (Exception ex) {
                LOG.error("problem during kassenzeichen geom search", ex);
            }

            // ids der Kassenzeichen sortieren
            final List<Integer> sortedIdList = Arrays.asList(idSet.toArray(new Integer[0]));
            Collections.sort(sortedIdList);

            //
            return sortedIdList;
        } else {
            LOG.info("searchGeometry is null, geom search is not possible");
        }

        return null;
    }
}
