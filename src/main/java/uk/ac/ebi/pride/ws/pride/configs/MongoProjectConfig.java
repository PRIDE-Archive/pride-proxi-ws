package uk.ac.ebi.pride.ws.pride.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.ac.ebi.pride.mongodb.configs.AbstractPrideMongoConfiguration;

/**
 * @author ypriverol
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {"uk.ac.ebi.pride.mongodb.archive.service"})
@EnableMongoRepositories(basePackages = "uk.ac.ebi.pride.mongodb.archive.repo")
@EnableAutoConfiguration
public class MongoProjectConfig extends AbstractPrideMongoConfiguration {

    @Value("${mongodb.project.database}")
    private String mongoProjectDatabase;

    @Value("${mongodb.project.app.user}")
    private String user;

    @Value("${mongodb.project.app.password}")
    private String password;

    @Value("${mongodb.project.app.authenticationDatabase}")
    private String authenticationDatabse;

    @Value("${mongodb.projects.replicate.hosts}")
    private String mongoHosts;

    @Value("${mongodb.projects.replicate.ports}")
    private String mongoPorts;

    @Value("${mongodb.project.app.machine.port}")
    private String port;

    @Value("${mongo.single.machine}")
    private String singleMachine;

    @Value("${mongodb.projects.single.machine.host}")
    private String mongoHost;

    @Value("${mongodb.projects.machine.uri}")
    private String mongoURI;

    @Override
    protected String getDatabaseName() {
        return mongoProjectDatabase;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getAuthenticationDatabse() {
        return authenticationDatabse;
    }

    @Override
    public String getMongoHosts() {
        return mongoHosts;
    }

    @Override
    public String getMongoPorts() {
        return mongoPorts;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public String getSingleMachine() {
        return singleMachine;
    }

    @Override
    public String getMongoHost() {
        return mongoHost;
    }

    @Override
    public String getMongoURI() {
        return mongoURI;
    }
}