package de.cismet.verdis.server.json.aenderungsanfrage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
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
    
    private static String JSON_ANFRAGE_COMPLEX = "de/cismet/verdis/server/json/aenderungsanfrage/anfrage_complex.json";
    private static String JSON_ANFRAGE_SIMPLE = "de/cismet/verdis/server/json/aenderungsanfrage/anfrage_simple.json";
    
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
    
    private AenderungsanfrageJson getSimpleAnfrageJson() {
            final Map<String, FlaecheAenderungJson> flaechen = new HashMap<>();
            flaechen.put("5", new FlaecheAenderungJson.Groesse(12));
            final AenderungsanfrageJson anederungsanfrage = new AenderungsanfrageJson(
                    60004629,
                    flaechen,
                    new ArrayList<>(
                        Arrays.asList(
                            new NachrichtJson(NachrichtJson.Typ.CITIZEN,
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
        flaechen.put("1", new FlaecheAenderungJson.Groesse(1430));            
        flaechen.put("2", new FlaecheAenderungJson.Groesse(921, 
                new FlaechePruefungJson.Groesse(new PruefungJson.Groesse(921, "SteinbacherD102", new Date(47110815)))));
        flaechen.put("8", new FlaecheAenderungJson.Flaechenart(
                new FlaecheFlaechenartJson("Gründachfläche", "GDF"), 
                new FlaechePruefungJson.Flaechenart(
                        new PruefungJson.Flaechenart(new FlaecheFlaechenartJson("Gründachfläche", "GDF"), "SteinbacherD102", new Date(47110815))
                )
        ));

        final List<NachrichtJson> nachrichten = new ArrayList<>();
        nachrichten.add(new NachrichtJson(NachrichtJson.Typ.CLERK,
            new Date(1562059800000l),
            "Sehr geehrte*r Nutzer*in, hier haben Sie die Möglichkeit Änderungen an Ihren Flächen mitzuteilen.",
            "verdis"
        ));
        nachrichten.add(new NachrichtJson(NachrichtJson.Typ.CITIZEN,
            new Date(1562060700000l),
            "Fläche B ist kleiner. Sie ist nicht 40 m² groß, sondern nur 37 m². Sie ist auch nicht an dem Kanal angeschlossen, sondern besteht aus Ökopflaster und versickert. Siehe Foto.",
            "...",
            Arrays.asList(new NachrichtAnhangJson("Ökopflasterfoto.pdf", "1337"))                
        ));
        nachrichten.add(new NachrichtJson(NachrichtJson.Typ.CLERK,
            new Date(1562136300000l),
            "Die Änderung der Fläche werde ich übernehmen. Das Foto ist nicht ausreichend. Bitte übersenden Sie zusätzlich ein Foto der gesamten Fläche. Ökopflaster wird auch nicht als vollständig versickernd angesehen, sondern muss laut Satzung mit 70% seiner Flächen zur Gebührenerhebung herangezogen werden.",
            "Dirk Steinbacher" 
        ));
        nachrichten.add(new NachrichtJson(
            new Date(1562136360000l),
            "SYSTEMMESSAGE({ type: 'changed', flaeche: '1' ,flaechenart:'Dachfläche'})"
        ));
        nachrichten.add(new NachrichtJson(NachrichtJson.Typ.CITIZEN,
            new Date(1562179560000l),
            "Hier das gewünschte Foto. Die Zufahrt entwässert seitlich in die Beete.",
            "...",
            Arrays.asList(new NachrichtAnhangJson("Foto2.pdf", "13374"))                
        ));
        nachrichten.add(new NachrichtJson(NachrichtJson.Typ.CLERK,
            new Date(1562227500000l),
            "Auf dem 2ten Foto sind Rasenkantensteine und ein Gully zu erkennen. Aus diesem Grund muss ich für diese Fläche 24 m² (70% von 37 m²) zur Veranlagung an das Steueramt weitergeben.",
            "Dirk Steinbacher" 
        ));
        nachrichten.add(new NachrichtJson(
            new Date(1562227560000l),
            "SYSTEMMESSAGE({ type: 'changed', flaeche: '1', flaechenart:'Dachfläche' })"
        ));
        nachrichten.add(new NachrichtJson(Boolean.TRUE, 
            NachrichtJson.Typ.CITIZEN,
            new Date(1562486760000l),
            "So wird eine Nachricht visualisiert, die noch nicht abgesschickt ist.",
            "..."
        ));

        final AenderungsanfrageJson aenderungsanfrage = new AenderungsanfrageJson(
                60004629,
                flaechen,
                nachrichten
        );
        return aenderungsanfrage;
    }

    @Test
    public void testComplexAnfrageJson() throws JsonProcessingException, Exception {
        final AenderungsanfrageJson aenderungsanfrage = getComplexAnfrageJson();
        String anfrageString = aenderungsanfrage.toJson(true);
        Assert.assertNotNull(anfrageString);
        System.out.println(anfrageString);            

        final String testString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(JSON_ANFRAGE_COMPLEX), "UTF-8");
        Assert.assertEquals(anfrageString, testString);
    }
    
    @Test
    public void testSimpleAnfrageJson() throws JsonProcessingException, Exception {
        final AenderungsanfrageJson aenderungsanfrage = getSimpleAnfrageJson();
        String anfrageString = aenderungsanfrage.toJson(true);
        Assert.assertNotNull(anfrageString);
        System.out.println(anfrageString);            

        final String testString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(JSON_ANFRAGE_SIMPLE), "UTF-8");
        Assert.assertEquals(anfrageString, testString);
    }
    
    @Test
    public void testSimpleAnfrageJsonDeserializer() throws JsonProcessingException, Exception {
            final AenderungsanfrageJson aenderungsanfrage = getSimpleAnfrageJson();
            String anfrageString = aenderungsanfrage.toJson();
            Assert.assertNotNull(anfrageString);

            final AenderungsanfrageJson anfrageTest = AenderungsanfrageJson.readValue(aenderungsanfrage.toJson());            
            final String testString = anfrageTest.toJson();
            System.out.println(testString);            
            Assert.assertEquals(anfrageString, testString);
        
    }
    
    @Test
    public void testComplexAnfrageJsonDeserializer() throws JsonProcessingException, Exception {
            final AenderungsanfrageJson aenderungsanfrage = getComplexAnfrageJson();
            String anfrageString = aenderungsanfrage.toJson();
            Assert.assertNotNull(anfrageString);

            final AenderungsanfrageJson anfrageTest = AenderungsanfrageJson.readValue(aenderungsanfrage.toJson());            
            final String testString = anfrageTest.toJson();
            System.out.println(testString);            
            Assert.assertEquals(anfrageString, testString);
        
    }
}
