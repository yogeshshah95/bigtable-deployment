package org.example;

import java.util.logging.Level;
import java.util.logging.Logger;
import static spark.Spark.get;
import static spark.Spark.port;

public class Main{
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args){
        logger.setLevel(Level.ALL);
        logger.info("Taxonomy data extraction from Bigtable has started");
        // Change Log Level Programmatically

        port(4567); // Set the port number

        get("/hello", (req, res) -> "Hello, Spark Java!");

        get("/vindata/:vin", (req, res) -> {
            String vin = req.params(":vin");
            return BigtableAdapter.getTaxonomyData(vin);
        });
    }
}