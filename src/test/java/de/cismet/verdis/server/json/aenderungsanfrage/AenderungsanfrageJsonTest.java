package de.cismet.verdis.server.json.aenderungsanfrage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import de.cismet.verdis.server.json.NachrichtParameterJson;
import de.cismet.verdis.server.json.FlaecheAenderungFlaechenartJson;
import de.cismet.verdis.server.json.NachrichtJson;
import de.cismet.verdis.server.json.FlaechePruefungGroesseJson;
import de.cismet.verdis.server.json.FlaecheFlaechenartJson;
import de.cismet.verdis.server.json.NachrichtAnhangJson;
import de.cismet.verdis.server.json.NachrichtSystemJson;
import de.cismet.verdis.server.json.AenderungsanfrageJson;
import de.cismet.verdis.server.json.FlaechePruefungFlaechenartJson;
import de.cismet.verdis.server.json.NachrichtSachberarbeiterJson;
import de.cismet.verdis.server.json.PruefungGroesseJson;
import de.cismet.verdis.server.json.FlaecheAenderungGroesseJson;
import de.cismet.verdis.server.json.FlaecheAnschlussgradJson;
import de.cismet.verdis.server.json.FlaecheAenderungJson;
import de.cismet.verdis.server.json.NachrichtBuergerJson;
import de.cismet.verdis.server.json.NachrichtParameterAnschlussgradJson;
import de.cismet.verdis.server.json.PruefungFlaechenartJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.cismet.verdis.server.utils.AenderungsanfrageUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.geojson.GeoJsonObject;
import org.geojson.GeoJsonObjectVisitor;
import org.geojson.Polygon;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author pd
 */
public class AenderungsanfrageJsonTest {
    
    private static final String JSON_ANFRAGE_TEST = "de/cismet/verdis/server/json/aenderungsanfrage/anfrage_test.json";

    private static final String JSON_ANFRAGE_COMPLEX = "de/cismet/verdis/server/json/aenderungsanfrage/anfrage_complex.json";
    private static final String JSON_ANFRAGE_SIMPLE = "de/cismet/verdis/server/json/aenderungsanfrage/anfrage_simple.json";

    private static final String JSON_ANFRAGE_PROCESSING_ORIG = "de/cismet/verdis/server/json/aenderungsanfrage/anfrage_processing%d_orig.json";
    private static final String JSON_ANFRAGE_PROCESSING_CHANGE = "de/cismet/verdis/server/json/aenderungsanfrage/anfrage_processing%d_change.json";
    private static final String JSON_ANFRAGE_PROCESSING_PROCESSED = "de/cismet/verdis/server/json/aenderungsanfrage/anfrage_processing%d_processed.json";

    public AenderungsanfrageJsonTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testTest() {
        try {
            final String aenderungsanfrageTestJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(JSON_ANFRAGE_TEST), "UTF-8");
            final AenderungsanfrageJson aenderungsanfrageDeserialized = AenderungsanfrageUtils.createAenderungsanfrageJson(aenderungsanfrageTestJson);                            
            Assert.assertNotNull(aenderungsanfrageDeserialized);
        } catch (final Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    private AenderungsanfrageJson getSimpleAnfrageJson() {
            final Map<String, FlaecheAenderungJson> flaechen = new HashMap<>();
            flaechen.put("5", new FlaecheAenderungGroesseJson(12));
            final Map<String, GeoJsonObject> geometrien = new HashMap<>();
            final AenderungsanfrageJson anederungsanfrage = new AenderungsanfrageJson(
                    60004629,
                    flaechen,
                    geometrien,
                    new ArrayList<>(
                        (List)Arrays.asList(
                            new NachrichtBuergerJson(
                                new Date(47110815),
                                "Da passt was nicht weil isso, siehe lustiges pdf !",
                                "Bürger",
                                Arrays.asList(new NachrichtAnhangJson("lustiges.pdf", "aaa-bbb-ccc"))
                            )
                        )
                    )
            );
            return anederungsanfrage;
    }

    private AenderungsanfrageJson getComplexAnfrageJson() {
        final Map<String, FlaecheAenderungJson> flaechen = new HashMap<>();
        flaechen.put("1", new FlaecheAenderungGroesseJson(1430));            
        flaechen.put("2", new FlaecheAenderungGroesseJson(921, 
                new FlaechePruefungGroesseJson(new PruefungGroesseJson(921, "SteinbacherD102", new Date(47110815)))));
        flaechen.put("8", new FlaecheAenderungFlaechenartJson(
                new FlaecheFlaechenartJson("Gründachfläche", "GDF"), 
                new FlaechePruefungFlaechenartJson(
                        new PruefungFlaechenartJson(new FlaecheFlaechenartJson("Gründachfläche", "GDF"), "SteinbacherD102", new Date(47110815))
                )
        ));
        
        final Map<String, GeoJsonObject> geometrien = new HashMap<>();        

        final List<NachrichtJson> nachrichten = new ArrayList<>();
        nachrichten.add(new NachrichtSachberarbeiterJson(
            new Date(1562059800000l),
            "Sehr geehrte*r Nutzer*in, hier haben Sie die Möglichkeit Änderungen an Ihren Flächen mitzuteilen.",
            "verdis"
        ));
        nachrichten.add(new NachrichtBuergerJson(
            new Date(1562060700000l),
            "Fläche B ist kleiner. Sie ist nicht 40 m² groß, sondern nur 37 m². Sie ist auch nicht an dem Kanal angeschlossen, sondern besteht aus Ökopflaster und versickert. Siehe Foto.",
            "Bürger",
            Arrays.asList(new NachrichtAnhangJson("Ökopflasterfoto.pdf", "1337"))                
        ));
        nachrichten.add(new NachrichtSachberarbeiterJson(
            new Date(1562136300000l),
            "Die Änderung der Fläche werde ich übernehmen. Das Foto ist nicht ausreichend. Bitte übersenden Sie zusätzlich ein Foto der gesamten Fläche. Ökopflaster wird auch nicht als vollständig versickernd angesehen, sondern muss laut Satzung mit 70% seiner Flächen zur Gebührenerhebung herangezogen werden.",
            "Dirk Steinbacher" 
        ));
        nachrichten.add(new NachrichtSystemJson(
            new Date(1562136360000l),
            new NachrichtParameterAnschlussgradJson(NachrichtParameterJson.Type.REJECTED, "1", new FlaecheAnschlussgradJson("Dachfläche", "DF")),
            "Dirk Steinbacher"                
        ));
        nachrichten.add(new NachrichtBuergerJson(
            new Date(1562179560000l),
            "Hier das gewünschte Foto. Die Zufahrt entwässert seitlich in die Beete.",
            "Bürger",
            Arrays.asList(new NachrichtAnhangJson("Foto2.pdf", "13374"))                
        ));
        nachrichten.add(new NachrichtSachberarbeiterJson(
            new Date(1562227500000l),
            "Auf dem 2ten Foto sind Rasenkantensteine und ein Gully zu erkennen. Aus diesem Grund muss ich für diese Fläche 24 m² (70% von 37 m²) zur Veranlagung an das Steueramt weitergeben.",
            "Dirk Steinbacher" 
        ));
        nachrichten.add(new NachrichtSystemJson(
            new Date(1562227560000l),
            new NachrichtParameterAnschlussgradJson(NachrichtParameterJson.Type.CHANGED, "1", new FlaecheAnschlussgradJson("Dachfläche", "DF")), 
            "Dirk Steinbacher"                
        ));
        nachrichten.add(new NachrichtBuergerJson(
            new Date(1562486760000l),
            "So wird eine Nachricht visualisiert, die noch nicht abgesschickt ist.",
            "Bürger",
            true
        ));

        final AenderungsanfrageJson aenderungsanfrage = new AenderungsanfrageJson(
                60004629,
                flaechen,
                geometrien,
                nachrichten
        );
        return aenderungsanfrage;
    }

    private void testProcessing(final String aenderungsanfrageOrigJson, final String aenderungsanfrageChangeJson, final String aenderungsanfrageProcessedJson) throws Exception {
        final AenderungsanfrageJson aenderungsanfrageOrig = aenderungsanfrageOrigJson != null ? AenderungsanfrageUtils.createAenderungsanfrageJson(aenderungsanfrageOrigJson) : null;
        final AenderungsanfrageJson aenderungsanfrageChange = aenderungsanfrageChangeJson != null ? AenderungsanfrageUtils.createAenderungsanfrageJson(aenderungsanfrageChangeJson) : null;

        final AenderungsanfrageJson aenderungsanfrageNew = AenderungsanfrageUtils.getInstance().processAnfrage(60004629, aenderungsanfrageOrig, aenderungsanfrageChange);               
        final String aenderungsanfrageNewJson = aenderungsanfrageNew.toPrettyJson();
        //System.out.println(aenderungsanfrageNewJson);                            
        Assert.assertEquals(aenderungsanfrageNewJson, aenderungsanfrageProcessedJson);        
    }
    
    @Test
    public void testProcessing1() throws JsonProcessingException, Exception {
        final Integer jsonNumber = 1;
        final String aenderungsanfrageOrigJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_ORIG, jsonNumber)), "UTF-8");
        final String aenderungsanfrageChangeJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_CHANGE, jsonNumber)), "UTF-8");
        final String aenderungsanfrageProcessedJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_PROCESSED, jsonNumber)), "UTF-8");        
        
        testProcessing(null, aenderungsanfrageChangeJson, aenderungsanfrageProcessedJson);
        testProcessing(aenderungsanfrageOrigJson, aenderungsanfrageChangeJson, aenderungsanfrageProcessedJson);
    }

    @Test
    public void testProcessing2() throws JsonProcessingException, Exception {
        final Integer jsonNumber = 2;
        final String aenderungsanfrageOrigJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_ORIG, jsonNumber)), "UTF-8");
        final String aenderungsanfrageChangeJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_CHANGE, jsonNumber)), "UTF-8");
        final String aenderungsanfrageProcessedJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_PROCESSED, jsonNumber)), "UTF-8");        
        
        testProcessing(aenderungsanfrageOrigJson, aenderungsanfrageChangeJson, aenderungsanfrageProcessedJson);
    }

    @Test
    public void testProcessing3() throws JsonProcessingException, Exception {
        final Integer jsonNumber = 3;
        final String aenderungsanfrageOrigJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_ORIG, jsonNumber)), "UTF-8");
        final String aenderungsanfrageChangeJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_CHANGE, jsonNumber)), "UTF-8");
        final String aenderungsanfrageProcessedJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_PROCESSED, jsonNumber)), "UTF-8");        
        
        testProcessing(aenderungsanfrageOrigJson, aenderungsanfrageChangeJson, aenderungsanfrageProcessedJson);
    }

    @Test
    public void testProcessing4() throws JsonProcessingException, Exception {
        final Integer jsonNumber = 4;
        final String aenderungsanfrageOrigJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_ORIG, jsonNumber)), "UTF-8");
        final String aenderungsanfrageChangeJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_CHANGE, jsonNumber)), "UTF-8");
        final String aenderungsanfrageProcessedJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_PROCESSED, jsonNumber)), "UTF-8");        
        
        testProcessing(aenderungsanfrageOrigJson, aenderungsanfrageChangeJson, aenderungsanfrageProcessedJson);
    }
    
    @Test
    public void testProcessing5() throws JsonProcessingException, Exception {
        final Integer jsonNumber = 5;
        final String aenderungsanfrageOrigJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_ORIG, jsonNumber)), "UTF-8");
        final String aenderungsanfrageChangeJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_CHANGE, jsonNumber)), "UTF-8");
        final String aenderungsanfrageProcessedJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_PROCESSED, jsonNumber)), "UTF-8");        
        
        testProcessing(aenderungsanfrageOrigJson, aenderungsanfrageChangeJson, aenderungsanfrageProcessedJson);
    }

    @Test
    public void testProcessing6() throws JsonProcessingException, Exception {
        final Integer jsonNumber = 6;
        final String aenderungsanfrageOrigJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_ORIG, jsonNumber)), "UTF-8");
        final String aenderungsanfrageChangeJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_CHANGE, jsonNumber)), "UTF-8");
        final String aenderungsanfrageProcessedJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format(JSON_ANFRAGE_PROCESSING_PROCESSED, jsonNumber)), "UTF-8");        
        
        testProcessing(aenderungsanfrageOrigJson, aenderungsanfrageChangeJson, aenderungsanfrageProcessedJson);
    }

    @Test
    public void testComplexAnfrageSerializer() throws JsonProcessingException, Exception {
        final AenderungsanfrageJson aenderungsanfrage = getComplexAnfrageJson();
        final String aenderungsanfrageJson = aenderungsanfrage.toPrettyJson();
        Assert.assertNotNull(aenderungsanfrageJson);
        //System.out.println(aenderungsanfrageJson);            

        final String aenderungsanfrageComplexJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(JSON_ANFRAGE_COMPLEX), "UTF-8");
        Assert.assertEquals(aenderungsanfrageJson, aenderungsanfrageComplexJson);
    }
    
    @Test
    public void testSimpleAnfrageSerializer() throws JsonProcessingException, Exception {
        final AenderungsanfrageJson aenderungsanfrage = getSimpleAnfrageJson();
        final String aenderungsanfrageJson = aenderungsanfrage.toPrettyJson();
        Assert.assertNotNull(aenderungsanfrageJson);
        //System.out.println(aenderungsanfrageJson);            

        final String aenderungsanfrageSimpleJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(JSON_ANFRAGE_SIMPLE), "UTF-8");
        Assert.assertEquals(aenderungsanfrageJson, aenderungsanfrageSimpleJson);
    }
    
    @Test
    public void testSimpleAnfrageDeserializer() throws JsonProcessingException, Exception {
        final AenderungsanfrageJson aenderungsanfrage = getSimpleAnfrageJson();
        final String aenderungsanfrageJson = aenderungsanfrage.toPrettyJson();
        Assert.assertNotNull(aenderungsanfrageJson);
        //System.out.println(aenderungsanfrageJson);            

        final AenderungsanfrageJson aenderungsanfrageDeserialized = AenderungsanfrageUtils.createAenderungsanfrageJson(aenderungsanfrage.toPrettyJson());                    
        final String aenderungsanfrageDeserializedJson = aenderungsanfrageDeserialized.toPrettyJson();        
        //System.out.println(aenderungsanfrageDeserializedJson);            
        Assert.assertEquals(aenderungsanfrageJson, aenderungsanfrageDeserializedJson);        
    }
    
    @Test
    public void testComplexAnfrageDeserializer() throws JsonProcessingException, Exception {
        final AenderungsanfrageJson aenderungsanfrage = getComplexAnfrageJson();
        final String aenderungsanfrageJson = aenderungsanfrage.toPrettyJson();
        Assert.assertNotNull(aenderungsanfrageJson);
        //System.out.println(aenderungsanfrageJson);

        final AenderungsanfrageJson aenderungsanfrageDeserialized = AenderungsanfrageUtils.createAenderungsanfrageJson(aenderungsanfrage.toPrettyJson());            
        final String aenderungsanfrageDeserializedJson = aenderungsanfrageDeserialized.toPrettyJson();
        //System.out.println(aenderungsanfrageDeserializedJson);            
        Assert.assertEquals(aenderungsanfrageJson, aenderungsanfrageDeserializedJson);        
    }
    
    @Test
    public void testEqualsTest() throws JsonProcessingException, Exception {
        final AenderungsanfrageJson aenderungsanfrage1 = getComplexAnfrageJson();
        final AenderungsanfrageJson aenderungsanfrage2 = getComplexAnfrageJson();
        Assert.assertEquals(aenderungsanfrage1.toPrettyJson(), aenderungsanfrage2.toPrettyJson());        
        Assert.assertEquals(aenderungsanfrage1, aenderungsanfrage2);        
    }
    
}
