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

import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@Getter
public class AenderungsanfrageConf {

    //~ Instance fields --------------------------------------------------------

    private final String webdavUrl;
    private final String webdavUser;
    private final String webdavPassword;
    private final String sachbearbeiterKontaktdaten;
    private final String sachbearbeiterDefaultname;

    private final String mailCmd;
    private final String mailbetreffVerifikation;
    private final String mailtemplateVerifikation;
    private final String mailbetreffBestaetigung;
    private final String mailtemplateBestaetigung;
    private final String mailtemplatesDirStatusupdate;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ServerAlkisConf object.
     *
     * @param   properties  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public AenderungsanfrageConf(final Properties properties) throws Exception {
        webdavUrl = properties.getProperty("WEBDAV_URL");
        webdavUser = properties.getProperty("WEBDAV_USER");
        webdavPassword = properties.getProperty("WEBDAV_PASSWORD");

        mailCmd = properties.getProperty("MAIL_CMD");
        mailbetreffVerifikation = properties.getProperty("MAILBETREFF_VERIFIKATION");
        mailtemplateVerifikation = properties.getProperty("MAILTEMPLATE_VERIFIKATION");
        mailbetreffBestaetigung = properties.getProperty("MAILBETREFF_BESTAETIGUNG");
        mailtemplateBestaetigung = properties.getProperty("MAILTEMPLATE_BESTAETIGUNG");
        mailtemplatesDirStatusupdate = properties.getProperty("MAILTEMPLATEDIR_STATUSUPDATE");

        sachbearbeiterKontaktdaten = properties.getProperty("SACHBEARBEITER_KONTAKTDATEN");
        sachbearbeiterDefaultname = properties.getProperty("SACHBEARBEITER_DEFAULTNAME");
    }
}
