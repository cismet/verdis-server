/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.verdis.server.search;

import com.vividsolutions.jts.geom.Geometry;

import de.cismet.cids.server.search.AbstractCidsServerSearch;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public abstract class GeomServerSearch extends AbstractCidsServerSearch {

    //~ Instance fields --------------------------------------------------------

    private Geometry geometry;
    private String crs;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geometry  DOCUMENT ME!
     */
    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCrs() {
        return crs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  crs  DOCUMENT ME!
     */
    public void setCrs(final String crs) {
        this.crs = crs;
    }
}
