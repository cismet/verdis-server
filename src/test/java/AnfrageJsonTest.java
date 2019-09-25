/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cismet.verdis.server.utils.aenderungsanfrage.AnfrageJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaecheAenderungFlaechenartJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaecheFlaechenartJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaecheAenderungGroesseJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaecheAenderungJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaechePruefungFlaechenartJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaechePruefungGroesseJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.NachrichtAnhangJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.NachrichtJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.PruefungFlaechenartJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.PruefungGroesseJson;
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
public class AnfrageJsonTest {
    private ObjectMapper mapper;
    
    public AnfrageJsonTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);        
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }
    
    @After
    public void tearDown() {
    }
    
    private AnfrageJson getSimpleAnfrageJson() {
            final Map<String, FlaecheAenderungJson> flaechen = new HashMap<>();
            flaechen.put("5", new FlaecheAenderungGroesseJson(12));
            final AnfrageJson anfrageJson = new AnfrageJson(
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
            return anfrageJson;
    }

    private AnfrageJson getComplexAnfrageJson() {
        final Map<String, FlaecheAenderungJson> flaechen = new HashMap<>();
        flaechen.put("1", new FlaecheAenderungGroesseJson(1430));            
        flaechen.put("2", new FlaecheAenderungGroesseJson(921, new FlaechePruefungGroesseJson(new PruefungGroesseJson(921, "SteinbacherD102", new Date(47110815)))));
        flaechen.put("8", new FlaecheAenderungFlaechenartJson(
                new FlaecheFlaechenartJson("Gründachfläche", "GDF"), 
                new FlaechePruefungFlaechenartJson(
                        new PruefungFlaechenartJson(new FlaecheFlaechenartJson("Gründachfläche", "GDF"), "SteinbacherD102", new Date(47110815))
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

        final AnfrageJson anfrageJson = new AnfrageJson(
                60004629,
                flaechen,
                nachrichten
        );
        return anfrageJson;
    }

    @Test
    public void testComplexAnfrageJson() throws JsonProcessingException, Exception {
        final AnfrageJson anfrageJson = getComplexAnfrageJson();
        String anfrageString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrageJson);
        Assert.assertNotNull(anfrageString);
        System.out.println(anfrageString);            

        final String testString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("anfrage_complex.json"), "UTF-8");
        Assert.assertEquals(anfrageString, testString);
    }
    
    @Test
    public void testSimpleAnfrageJson() throws JsonProcessingException, Exception {
        final AnfrageJson anfrageJson = getSimpleAnfrageJson();
        String anfrageString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrageJson);
        Assert.assertNotNull(anfrageString);
        System.out.println(anfrageString);            

        final String testString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("anfrage_simple.json"), "UTF-8");
        Assert.assertEquals(anfrageString, testString);
    }
    
    @Test
    public void testSimpleAnfrageJsonDeserializer() throws JsonProcessingException, Exception {
            final AnfrageJson anfrageJson = getSimpleAnfrageJson();
            String anfrageString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrageJson);
            Assert.assertNotNull(anfrageString);

            final AnfrageJson anfrageJsonTest = AnfrageJson.readValue(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrageJson));            
            final String testString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrageJsonTest);
            System.out.println(testString);            
            Assert.assertEquals(anfrageString, testString);
        
    }
    
    @Test
    public void testComplexAnfrageJsonDeserializer() throws JsonProcessingException, Exception {
            final AnfrageJson anfrageJson = getComplexAnfrageJson();
            String anfrageString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrageJson);
            Assert.assertNotNull(anfrageString);

            final AnfrageJson anfrageJsonTest = AnfrageJson.readValue(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrageJson));            
            final String testString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(anfrageJsonTest);
            System.out.println(testString);            
            Assert.assertEquals(anfrageString, testString);
        
    }
}
