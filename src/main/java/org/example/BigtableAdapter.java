package org.example;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.BigtableDataSettings;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.cloud.bigtable.data.v2.models.RowCell;
import com.google.cloud.bigtable.data.v2.models.TableId;
import com.google.cloud.bigtable.data.v2.models.TargetId;

public class BigtableAdapter{

    private static final Logger logger = Logger.getLogger(BigtableAdapter.class.getName());
    private static final String DEFAULT_CREDENTIAL_PATH = "src/main/java/cred/gcs_bucket.json";
    private static final String bigtableCred = System.getenv("BIGTABLE_CREDENTIALS") != null
            ? System.getenv("BIGTABLE_CREDENTIALS")
            : DEFAULT_CREDENTIAL_PATH;
    private static final String projectId = "marketcheck-gcp";
    private static final String instanceId = "marketcheck-cars-cbt";
    private static final String tableId = "mc-delta-neovin";
    private static BigtableDataClient dataClient; // Singleton Client
    private static final TargetId targetId = TableId.of(tableId);
    private static final Gson gson = new Gson();


    // Initialize Client Once
    static{
        try{
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(bigtableCred));
            logger.info("Google credentials " + credentials);
            // Create a CredentialsProvider
            FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);

            // Build settings using the CredentialsProvider
            BigtableDataSettings settings = BigtableDataSettings.newBuilder()
                    .setProjectId(projectId)
                    .setInstanceId(instanceId)
                    .setCredentialsProvider(credentialsProvider)
                    .build();

            // Initialize the Bigtable Client
            dataClient = BigtableDataClient.create(settings);
            logger.info("Google client " + dataClient);
            logger.info("Bigtable Client Initialized Successfully");

            // Register shutdown hook for cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Closing Bigtable client...");
                closeClient();
            }));
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Failed to initialize Bigtable client");
        }
    }


    public static String getTaxonomyData(String vin){
        Map<String, String> vinDataMap = new HashMap<>();
        try{
            long startTime = System.currentTimeMillis();
            Row row = dataClient.readRow(targetId, vin); // Corrected method usage
            long endTime = System.currentTimeMillis();
            logger.info(String.format("NeoVIN Bigtable lookup execution time %s ms for vin %s",
                    (endTime - startTime), vin));
            if(row != null){
                List<RowCell> rowCellList = row.getCells();
                for(RowCell rowCell : rowCellList){
                    vinDataMap.put(rowCell.getQualifier().toStringUtf8(), rowCell.getValue().toStringUtf8());
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            logger.severe("Error while getting data from Bigtable");
        }
        return gson.toJson(vinDataMap);
    }



    //close Bigtable client
    private static void closeClient() {
        if (dataClient != null) {
            dataClient.close();
            dataClient = null;
        }
    }
}
