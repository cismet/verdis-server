/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cismet.verdis.server.utils.aenderungsanfrage.AnfrageJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaecheGroesseJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaecheJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.FlaechePruefungGroesseJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.NachrichtJson;
import de.cismet.verdis.server.utils.aenderungsanfrage.PruefungJson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
    }
    
    @After
    public void tearDown() {
    }
    
    private AnfrageJson getSimpleAnfrageJson() {
            final Map<String, FlaecheJson> flaechen = new HashMap<>();
            flaechen.put("5", new FlaecheGroesseJson(12));
            final AnfrageJson anfrageJson = new AnfrageJson(
                    60004629,
                    flaechen,
                    new ArrayList<>(
                        Arrays.asList(
                            new NachrichtJson(
                                new Date(47110815),
                                "Bürger",
                                "Da passt was nicht weil isso, siehe lustiges pdf !",
                                "http://meine.domain.de/lustiges.pdf"))));
            return anfrageJson;
    }

    private AnfrageJson getComplexAnfrageJson() {
        final AnfrageJson anfrageJson = getSimpleAnfrageJson();        
        anfrageJson.getNachrichten().add(new NachrichtJson(
            new Date(47110815), "Dirk Steinbacher",
            "Konnte nichts feststellen, alles in Ordnung."));
        anfrageJson.getFlaechen().get("5")
                .setPruefung(new FlaechePruefungGroesseJson(new PruefungJson(PruefungJson.Status.REJECTED, "test", new Date(47110815))));
        anfrageJson.getNachrichten().add(new NachrichtJson(
            new Date(47110815), "Bürger",
            "Oh, falsches PDF, siehe richtiges pdf.",
            "http://meine.domain.de/richtiges.pdf"));
        anfrageJson.getNachrichten().add(new NachrichtJson(
            new Date(47110815), "Dirk Steinbacher", 
            "Ach so, verstehe. Alles Klar !"));
        anfrageJson.getNachrichten().add(new NachrichtJson(
                new Date(47110815), "Bürger", 
                "Geht doch, danke."));
        anfrageJson.getFlaechen().get("5")
                .setPruefung(new FlaechePruefungGroesseJson(new PruefungJson(PruefungJson.Status.ACCEPTED, "test", new Date(47110815))));

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
