package kz.dossier.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableNeo4jRepositories(basePackages = "kz.dossier.neo4j.repository", transactionManagerRef = "neo4j1TransactionManager", neo4jTemplateRef = "neo4j1Template")
@EnableTransactionManagement
public class Neo4j1Configuration extends Neo4jDataAutoConfiguration {
    @Value("${spring.neo4j1.uri}")
    private String neo4jUri;
    @Value("${spring.neo4j1.authentication.username}")
    private String username;
    @Value("${spring.neo4j1.authentication.password}")
    private String password;
    @Bean
    public Driver getConfiguration1(){return GraphDatabase.driver(neo4jUri, AuthTokens.basic(username,password));}
    @Bean(name = "neo4j1TransactionManager")
    public Neo4jTransactionManager neo4jTransactionManager(){
        return new Neo4jTransactionManager(getConfiguration1());
    }
    @Bean(name = "neo4j1Client")
    public Neo4jClient neo4j1Client(DatabaseSelectionProvider databaseNameProvider){
        return Neo4jClient.create(getConfiguration1(),databaseNameProvider);
    }
    @Bean(name = "neo4j1Template")
    public Neo4jTemplate neo4j1Template(@Qualifier("neo4j1Client") Neo4jClient neo4jClient, Neo4jMappingContext neo4jMappingContext){
        return new Neo4jTemplate(neo4jClient,neo4jMappingContext);
    }

}
