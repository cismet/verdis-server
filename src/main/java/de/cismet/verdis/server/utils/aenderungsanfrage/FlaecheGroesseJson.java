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
    public class FlaecheGroesseJson extends FlaecheJson {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FlaecheGroesseJson object.
         *
         * @param  groesse  DOCUMENT ME!
         */
        public FlaecheGroesseJson(final Double groesse) {
            super(groesse, null, null, null, null);
        }

        /**
         * Creates a new FlaecheGroesseJson object.
         *
         * @param  groesse    DOCUMENT ME!
         * @param  bemerkung  DOCUMENT ME!
         */
        public FlaecheGroesseJson(final Double groesse, final BemerkungJson bemerkung) {
            super(groesse, null, null, bemerkung, null);
        }

        /**
         * Creates a new FlaecheGroesseJson object.
         *
         * @param  groesse         DOCUMENT ME!
         * @param  bemerkung       DOCUMENT ME!
         * @param  pruefungStatus  DOCUMENT ME!
         */
        public FlaecheGroesseJson(final Double groesse, final BemerkungJson bemerkung, final String pruefungStatus) {
            super(groesse, null, null, bemerkung, pruefungStatus);
        }
    }
