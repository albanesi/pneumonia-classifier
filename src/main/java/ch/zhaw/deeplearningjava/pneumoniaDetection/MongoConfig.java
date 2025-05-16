package ch.zhaw.deeplearningjava.pneumoniaDetection;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClient mongoClient() {
        // 💥 Ersetze DEIN_PASSWORT hier durch den richtigen Primary Key aus dem Azure-Portal
        String uri = "mongodb+srv://albanese11:EvianWasser1@mdm-aca-db.global.mongocluster.cosmos.azure.com/?tls=true&retrywrites=false&authSource=admin&authMechanism=SCRAM-SHA-256";
        return MongoClients.create(
            MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build()
        );
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "mdm-aca-db");
    }
}
