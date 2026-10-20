/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.action;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.newuser.User;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.server.actions.ServerAction;
import de.cismet.cids.server.actions.ServerActionParameter;
import de.cismet.cids.server.actions.UserAwareServerAction;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import de.cismet.verdis.commons.constants.VerdisConstants;

import de.cismet.verdis.server.search.KassenzeichenGeomSearch;

/**
 * Returns the kassenzeichennummern of all kassenzeichen whose geometries intersect the given geometry. The geometry
 * must be passed as GeoJSON string in the GEOM parameter. A Feature is also accepted, its geometry is used then. The
 * SRID is taken from the optional "crs" member (e.g. "EPSG:25832"), otherwise 25832 is used.
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class KassenzeichenByGeomServerAction implements ServerAction,
    MetaServiceStore,
    UserAwareServerAction,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(KassenzeichenByGeomServerAction.class);
    public static final String TASK_NAME = "KassenzeichenByGeom";
    private static final int DEFAULT_SRID = 25832;

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Parameter {

        //~ Enum constants -----------------------------------------------------

        GEOM
    }

    //~ Instance fields --------------------------------------------------------

    private User user;
    private MetaService metaService;

    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        String geoJson = null;

        try {
            if (params != null) {
                for (final ServerActionParameter sap : params) {
                    if (sap.getKey().equals(Parameter.GEOM.toString())) {
                        geoJson = (String)sap.getValue();
                    }
                }
            }

            if ((geoJson == null) || geoJson.trim().isEmpty()) {
                return new Exception("geom can't be null");
            }

            final Geometry geom = geoJsonToGeometry(geoJson);

            final KassenzeichenGeomSearch search = new KassenzeichenGeomSearch();
            search.setGeometry(geom);
            final Map localServers = new HashMap<>();
            localServers.put(VerdisConstants.DOMAIN, getMetaService());
            search.setActiveLocalServers(localServers);
            search.setUser(getUser());

            return search.performServerSearch();
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            return ex;
        }
    }

    /**
     * Converts the given GeoJSON string to a geometry.
     *
     * @param   geoJson  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception                 DOCUMENT ME!
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private static Geometry geoJsonToGeometry(final String geoJson) throws Exception {
        final JsonNode root = new ObjectMapper().readTree(geoJson);
        final int srid = getSrid(root);
        final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
        final JsonNode geomNode = "Feature".equals(root.path("type").asText()) ? root.get("geometry") : root;

        if ((geomNode == null) || geomNode.isNull()) {
            throw new IllegalArgumentException("GeoJSON contains no geometry");
        }

        final Geometry geom = toGeometry(geomNode, geomFactory);
        geom.setSRID(srid);
        return geom;
    }

    /**
     * Determines the SRID from the "crs" member, e.g. "EPSG:25832" or "urn:ogc:def:crs:EPSG::25832".
     *
     * @param   root  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static int getSrid(final JsonNode root) {
        final String crsName = root.path("crs").path("properties").path("name").asText(null);
        if (crsName != null) {
            final int index = crsName.lastIndexOf(':');
            if (index >= 0) {
                try {
                    return Integer.parseInt(crsName.substring(index + 1).trim());
                } catch (final NumberFormatException ex) {
                    LOG.warn("could not parse crs " + crsName + ", using default srid " + DEFAULT_SRID, ex);
                }
            }
        }
        return DEFAULT_SRID;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node         DOCUMENT ME!
     * @param   geomFactory  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private static Geometry toGeometry(final JsonNode node, final GeometryFactory geomFactory) {
        final String type = node.path("type").asText();
        final JsonNode coords = node.get("coordinates");

        switch (type) {
            case "Point": {
                return geomFactory.createPoint(toCoordinate(coords));
            }
            case "LineString": {
                return geomFactory.createLineString(toCoordinates(coords));
            }
            case "Polygon": {
                return toPolygon(coords, geomFactory);
            }
            case "MultiPoint": {
                final Point[] points = new Point[coords.size()];
                for (int i = 0; i < points.length; i++) {
                    points[i] = geomFactory.createPoint(toCoordinate(coords.get(i)));
                }
                return geomFactory.createMultiPoint(points);
            }
            case "MultiLineString": {
                final LineString[] lines = new LineString[coords.size()];
                for (int i = 0; i < lines.length; i++) {
                    lines[i] = geomFactory.createLineString(toCoordinates(coords.get(i)));
                }
                return geomFactory.createMultiLineString(lines);
            }
            case "MultiPolygon": {
                final Polygon[] polygons = new Polygon[coords.size()];
                for (int i = 0; i < polygons.length; i++) {
                    polygons[i] = toPolygon(coords.get(i), geomFactory);
                }
                return geomFactory.createMultiPolygon(polygons);
            }
            case "GeometryCollection": {
                final JsonNode geometries = node.get("geometries");
                final Geometry[] geoms = new Geometry[geometries.size()];
                for (int i = 0; i < geoms.length; i++) {
                    geoms[i] = toGeometry(geometries.get(i), geomFactory);
                }
                return geomFactory.createGeometryCollection(geoms);
            }
            default: {
                throw new IllegalArgumentException("unsupported GeoJSON type: " + type);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rings        DOCUMENT ME!
     * @param   geomFactory  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static Polygon toPolygon(final JsonNode rings, final GeometryFactory geomFactory) {
        final LinearRing shell = geomFactory.createLinearRing(toCoordinates(rings.get(0)));
        final LinearRing[] holes = new LinearRing[rings.size() - 1];
        for (int i = 0; i < holes.length; i++) {
            holes[i] = geomFactory.createLinearRing(toCoordinates(rings.get(i + 1)));
        }
        return geomFactory.createPolygon(shell, holes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   positions  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static Coordinate[] toCoordinates(final JsonNode positions) {
        final Coordinate[] coordinates = new Coordinate[positions.size()];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = toCoordinate(positions.get(i));
        }
        return coordinates;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static Coordinate toCoordinate(final JsonNode position) {
        return new Coordinate(position.get(0).asDouble(), position.get(1).asDouble());
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    public MetaService getMetaService() {
        return metaService;
    }

    @Override
    public void setMetaService(final MetaService metaService) {
        this.metaService = metaService;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
