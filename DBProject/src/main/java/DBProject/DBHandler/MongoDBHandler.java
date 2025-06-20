package DBProject.DBHandler;

import DBProject.DBConnect.MongoDBConnection;
import DBProject.DBModels.Provider;
import DBProject.DBModels.Service;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class MongoDBHandler {

    // Inserts a provider and a service into the MongoDB collection
    public static void insertProviderWithService(Provider provider, Service service) {
        MongoDatabase database = MongoDBConnection.getMongoClient().getDatabase("dbproject");
        MongoCollection<Document> collection = database.getCollection("providers");

        Document newProvider = new Document("name", provider.getName())
                .append("email", provider.getEmail());

        // Only add the services field if services are provided
        if (service != null && service.getName() != null && !service.getName().isEmpty()) {
            List<Document> services = new ArrayList<>();
            services.add(new Document("name", service.getName())
                    .append("description", service.getDescription()));
            newProvider.append("services", services);
        }

        collection.insertOne(newProvider);
    }

    // Adds a service to an existing provider
    public static void addServiceToExistingProvider(String name, String email, Service service) {
        MongoDatabase database = MongoDBConnection.getMongoClient().getDatabase("dbproject");
        MongoCollection<Document> collection = database.getCollection("providers");

        collection.updateOne(and(eq("name", name), eq("email", email)),
                push("services", new Document("name", service.getName())
                        .append("description", service.getDescription())));
    }

    // Updates the provider's name and email
    public static void updateProvider(String oldName, String oldEmail, String newName, String newEmail) {
        MongoDatabase database = MongoDBConnection.getMongoClient().getDatabase("dbproject");
        MongoCollection<Document> collection = database.getCollection("providers");

        collection.updateOne(and(eq("name", oldName), eq("email", oldEmail)),
                combine(set("name", newName), set("email", newEmail)));
    }

    // Updates a service's name and description for a provider
    public static void updateService(String name, String email, String oldServiceName, Service updatedService) {
        MongoDatabase database = MongoDBConnection.getMongoClient().getDatabase("dbproject");
        MongoCollection<Document> collection = database.getCollection("providers");

        collection.updateOne(and(eq("name", name), eq("email", email), elemMatch("services", eq("name", oldServiceName))),
                combine(set("services.$.name", updatedService.getName()),
                        set("services.$.description", updatedService.getDescription())));
    }

    // Deletes a service from a provider
    public static void deleteService(String name, String email, String serviceName) {
        MongoDatabase database = MongoDBConnection.getMongoClient().getDatabase("dbproject");
        MongoCollection<Document> collection = database.getCollection("providers");

        collection.updateOne(and(eq("name", name), eq("email", email)),
                pull("services", eq("name", serviceName)));
    }
}