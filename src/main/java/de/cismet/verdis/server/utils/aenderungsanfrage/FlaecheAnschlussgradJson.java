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
    public class FlaecheAnschlussgradJson extends FlaecheJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheAnschlussgradJson object.
         *
         * @param  anschlussgrad  DOCUMENT ME!
         */
        public FlaecheAnschlussgradJson(final String anschlussgrad) {
            super(null, anschlussgrad, null, null, null);
        }

        /**
         * Creates a new FlaecheAnschlussgradJson object.
         *
         * @param  anschlussgrad  DOCUMENT ME!
         * @param  bemerkung      DOCUMENT ME!
         */
        public FlaecheAnschlussgradJson(final String anschlussgrad, final BemerkungJson bemerkung) {
            super(null, anschlussgrad, null, bemerkung, null);
        }

        /**
         * Creates a new FlaecheAnschlussgradJson object.
         *
         * @param  anschlussgrad   DOCUMENT ME!
         * @param  bemerkung       DOCUMENT ME!
         * @param  pruefungStatus  DOCUMENT ME!
         */
        public FlaecheAnschlussgradJson(final String anschlussgrad,
                final BemerkungJson bemerkung,
                final String pruefungStatus) {
            super(null, anschlussgrad, null, bemerkung, pruefungStatus);
        }
    }
