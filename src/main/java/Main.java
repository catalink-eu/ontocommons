import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Main {
    private static Logger logger; // Logging facility
    private ObjectMapper objectMapper; // For JSON serialization & deserialization

    public Main(String path, String endpoint) {
        logger = LoggerFactory.getLogger(Main.class);
        objectMapper = new ObjectMapper(); // JSON encoder
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Ignore null fields
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY); // Ignore empty fields

        try {
            ArrayList<String> fileNames = getFileNames(path); // Retrieve the filenames at the given path
            ArrayList<Output> outputs = parseFiles(fileNames); // Parse the JSON files and create in-memory POJOs

            for(Output output : outputs) {
                //System.out.println(output);
                //System.out.println("------");
            }

            // Populate ontology with instance data
            populateOntology(outputs, endpoint);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    private void populateOntology(ArrayList<Output> outputs, String endpoint) {
        logger.info("Populating ontology...");
        org.apache.jena.query.ARQ.init();

        for(Output output : outputs) {
            String queryString = queryString = "PREFIX : <http://cpsosaware.eu/ontology#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "INSERT DATA {\n";
            for(Result result : output.getResults()) {
                UUID resultUUID = UUID.randomUUID();
                queryString += "    :" + resultUUID + " rdf:type :AnalysisOutput ;\n" +
                        "           sosa:resultTime \"" + output.getTimestamp() + "\"^^xsd:dateTime ;\n" +
                        "           sosa:madeBySensor :" + output.getSource() + " ;\n" +
                        "           sosa:hasSimpleResult \"" + result.getResult() + "\"^^xsd:double ;\n" +
                        "           sosa:observedProperty :" + result.getProperty() + " .\n";
            }
            queryString += "}";

            logger.info("Submitting UPDATE query:\n" + queryString);

            // Submit the UPDATE query
            RDFConnection conn = RDFConnectionFactory.
                    connect(endpoint + "/statements"); // GraphDB endpoint for UPDATE
            UpdateRequest update = UpdateFactory.create(queryString);
            conn.update(update);
            logger.info("Ontology population complete!");
        }
    }

    // Parses JSON files & creates a list of Java objects
    private ArrayList<Output> parseFiles(ArrayList<String> fileNames) throws IOException {
        logger.info("Converting JSON files to Java objects");
        ArrayList<Output> outputs = new ArrayList<>();
        for(String fileName : fileNames) {
            outputs.add(objectMapper.readValue(Files.readString(Paths.get(fileName)), Output.class));
        }
        logger.info("Total objects created: " + outputs.size());
        return outputs;
    }

    // Retrieves a list of filenames residing in a directory
    private ArrayList<String> getFileNames(String path) {
        logger.info("Reading file list from directory: " + new File(path).getAbsolutePath());
        ArrayList<String> fileNames = new ArrayList<>();
        for(File file : new File(path).listFiles()) {
            fileNames.add(file.getAbsolutePath());
        }
        logger.info("Total files found: " + fileNames.size());
        return fileNames;
    }

    public static void main(String[] args) {
        new Main("input", "http://localhost:7200/repositories/cpsosaware"); // ToDo: parameters: (1) input JSON folder, (2) sparql endpoint
    }
}
