package DBProject.DBConnect;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoDBConnection {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";

    // MongoClient instance
    private static MongoClient mongoClient = null;

    // Returns an existing or creates a new MongoClient instance
    public static MongoClient getMongoClient() {
        // If MongoClient is not initialized, create a new connection
        if (mongoClient == null) {
            mongoClient = MongoClients.create(CONNECTION_STRING);
        }
        // Return the existing MongoClient instance
        return mongoClient;
    }
}