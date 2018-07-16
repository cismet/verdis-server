/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.verdis.server.utils;

import lombok.Getter;

import de.cismet.cids.utils.serverresources.JasperReportServerResource;
import de.cismet.cids.utils.serverresources.ServerResource;
import de.cismet.cids.utils.serverresources.TextServerResource;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public enum VerdisServerResources {

    //~ Enum constants ---------------------------------------------------------

    MOTD_VERDIS_GRUNDIS_PROPERTIES(new TextServerResource("/motd/verdis_grundis.properties")),

    EB_REPORT_PROPERTIES(new TextServerResource("/reports/eb_report.properties")),

    EB_FLAECHEN_JASPER(new JasperReportServerResource("/reports/feb_flaechen.jasper")),
    EB_FRONTEN_JASPER(new JasperReportServerResource("/reports/fronten.jasper")),

    EB_MAP_A4LS_JASPER(new JasperReportServerResource("/reports/feb_mapA4LS.jasper")),
    EB_MAP_A3LS_JASPER(new JasperReportServerResource("/reports/feb_mapA3LS.jasper")),
    EB_MAP_A4P_JASPER(new JasperReportServerResource("/reports/feb_mapA4P.jasper")),
    EB_MAP_A3P_JASPER(new JasperReportServerResource("/reports/feb_mapA3P.jasper")),

    WEBDAV(new TextServerResource("/webdav/WebDav.properties"));

    //~ Instance fields --------------------------------------------------------

    @Getter private final ServerResource value;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Props object.
     *
     * @param  value  DOCUMENT ME!
     */
    private VerdisServerResources(final ServerResource value) {
        this.value = value;
    }
}
