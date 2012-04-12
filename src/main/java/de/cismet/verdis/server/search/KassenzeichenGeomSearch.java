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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection performServerSearch() {
        final Geometry searchGeometry = getGeometry();

        if (searchGeometry != null) {
            final String sqlKassenzeichenGeom = "SELECT "
                        + "    DISTINCT " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "."
                        + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kassenzeichennumer "
                        + "FROM "
                        + "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen, "
                        + "    " + VerdisMetaClassConstants.MC_GEOM + " AS geom "
                        + "WHERE "
                        + "    kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER
                        + " IS NOT NULL AND "
                        + "    geom.id = kassenzeichen.geometrie AND "
                        + "    ST_Intersects(GeomFromText('" + searchGeometry.toText() + "', "
                        + searchGeometry.getSRID() + "), geom.geo_field) AND "
                        + "    GeometryType(geom.geo_field) = 'POINT' "
                        + "    ORDER BY kassenzeichennumer ASC;";

            final String sqlFlaechenGeom = "SELECT "
                        + "    DISTINCT " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "."
                        + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kassenzeichennumer "
                        + "FROM "
                        + "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen, "
                        + "    flaechen AS flaechen, "
                        + "    " + VerdisMetaClassConstants.MC_FLAECHE + " AS flaeche, "
                        + "    " + VerdisMetaClassConstants.MC_FLAECHENINFO + " AS flaecheninfo, "
                        + "    " + VerdisMetaClassConstants.MC_GEOM + " AS geom "
                        + "WHERE "
                        + "    kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER
                        + " IS NOT NULL AND "
                        + "    flaechen.kassenzeichen_reference = kassenzeichen.id AND "
                        + "    flaechen.flaeche = flaeche.id AND "
                        + "    flaeche.flaecheninfo = flaecheninfo.id AND "
                        + "    geom.id = flaecheninfo.geometrie AND "
                        + "    ST_Intersects(GeomFromText('" + searchGeometry.toText() + "', "
                        + searchGeometry.getSRID() + "), geom.geo_field) "
                        + "    ORDER BY kassenzeichennumer ASC;";

            final String sqlFrontenGeom = "SELECT "
                        + "    DISTINCT " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "."
                        + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kassenzeichennumer "
                        + "FROM "
                        + "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen, "
                        + "    fronten AS fronten, "
                        + "    " + VerdisMetaClassConstants.MC_FRONTINFO + " AS frontinfo, "
                        + "    " + VerdisMetaClassConstants.MC_GEOM + " AS geom "
                        + "WHERE "
                        + "    kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER
                        + " IS NOT NULL AND "
                        + "    fronten.kassenzeichen_reference = kassenzeichen.id AND "
                        + "    fronten.frontinfo = frontinfo.id AND "
                        + "    geom.id = frontinfo.geometrie AND  "
                        + "    ST_Intersects(GeomFromText('" + searchGeometry.toText() + "', "
                        + searchGeometry.getSRID() + "), geom.geo_field) "
                        + "    ORDER BY kassenzeichennumer ASC;";

            final MetaService metaService = (MetaService)getActiveLoaclServers().get(VerdisConstants.DOMAIN);

            // ids der kassenzeichen sammeln
            final Set<Integer> idSet = new HashSet<Integer>();
            if (getLog().isDebugEnabled()) {
                getLog().debug(sqlKassenzeichenGeom);
            }
            try {
                for (final ArrayList fields : metaService.performCustomSearch(sqlKassenzeichenGeom)) {
                    idSet.add((Integer)fields.get(0));
                }
            } catch (Exception ex) {
                getLog().error("problem during kassenzeichen geom search", ex);
            }
            if (getLog().isDebugEnabled()) {
                getLog().debug(sqlFlaechenGeom);
            }
            try {
                for (final ArrayList fields : metaService.performCustomSearch(sqlFlaechenGeom)) {
                    idSet.add((Integer)fields.get(0));
                }
            } catch (Exception ex) {
                getLog().error("problem during flaechen geom search", ex);
            }
            if (getLog().isDebugEnabled()) {
                getLog().debug(sqlFrontenGeom);
            }
            try {
                for (final ArrayList fields : metaService.performCustomSearch(sqlFrontenGeom)) {
                    idSet.add((Integer)fields.get(0));
                }
            } catch (Exception ex) {
                getLog().error("problem during fronten geom search", ex);
            }

            // ids der Kassenzeichen sortieren
            final List<Integer> sortedIdList = Arrays.asList(idSet.toArray(new Integer[0]));
            Collections.sort(sortedIdList);

            //
            return sortedIdList;
        } else {
            getLog().info("searchGeometry is null, geom search is not possible");
        }

        return null;
    }
}
