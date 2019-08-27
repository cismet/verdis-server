/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.verdis.server.utils.aenderungsanfrage;


    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public class FlaecheArtJson extends FlaecheJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheArtJson object.
         *
         * @param  art  DOCUMENT ME!
         */
        public FlaecheArtJson(final String art) {
            super(null, null, art, null, null);
        }

        /**
         * Creates a new FlaecheArtJson object.
         *
         * @param  art        DOCUMENT ME!
         * @param  bemerkung  DOCUMENT ME!
         */
        public FlaecheArtJson(final String art, final BemerkungJson bemerkung) {
            super(null, null, art, bemerkung, null);
        }

        /**
         * Creates a new FlaecheArtJson object.
         *
         * @param  art             DOCUMENT ME!
         * @param  bemerkung       DOCUMENT ME!
         * @param  pruefungStatus  DOCUMENT ME!
         */
        public FlaecheArtJson(final String art, final BemerkungJson bemerkung, final String pruefungStatus) {
            super(null, null, art, bemerkung, pruefungStatus);
        }
    }
