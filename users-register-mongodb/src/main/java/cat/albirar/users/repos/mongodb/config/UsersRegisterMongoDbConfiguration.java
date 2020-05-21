/*
 * This file is part of "albirar users-register-mongodb".
 * 
 * "albirar users-register-mongodb" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "albirar users-register-mongodb" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "albirar users-register-mongodb" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.repos.mongodb.config;

import static cat.albirar.users.repos.mongodb.config.PropertiesMongodb.MONGODB_DATABASE;
import static cat.albirar.users.repos.mongodb.config.PropertiesMongodb.MONGODB_HOST;
import static cat.albirar.users.repos.mongodb.config.PropertiesMongodb.MONGODB_PORT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import cat.albirar.users.config.UsersRegisterConfiguration;
import cat.albirar.users.models.account.AccountBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.repos.IAccountRepo;
import cat.albirar.users.repos.IUserRepo;
import cat.albirar.users.repos.mongodb.IAccountMongoRepo;
import cat.albirar.users.repos.mongodb.IUserMongoRepo;

/**
 * Autoconfiguration for users register repositories and entities with mongodb.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Configuration
@PropertySource(value = "classpath:/cat/albirar/users/mongodb/users-mongodb.properties")
@Import(UsersRegisterConfiguration.class)
@EnableMongoRepositories(basePackageClasses = IUserMongoRepo.class)
public class UsersRegisterMongoDbConfiguration extends AbstractMongoClientConfiguration {
    
    @Value("${" + MONGODB_HOST + "}")
    private String MONGO_DB_HOST;

    @Value("${" + MONGODB_PORT + "}")
    private int MONGO_DB_PORT;

    @Value("${" + MONGODB_DATABASE + "}")
    private String MONGO_DB_NAME;
    
    @Bean
    public MongoTransactionManager transactionManager(MongoDbFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(String.format("mongodb://%s:%s", MONGO_DB_HOST, MONGO_DB_PORT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDatabaseName() {
        return MONGO_DB_NAME;
    }

    @Bean
    @Primary
    public IUserRepo userRepo(MongoOperations mongoOps) throws Exception {
        MongoRepositoryFactory factory;
        
        mongoOps.indexOps(UserBean.class).ensureIndex(new Index().on("username", Direction.ASC).unique());
        mongoOps.indexOps(UserBean.class).ensureIndex(new Index().on("preferredChannel", Direction.ASC).unique());
        factory = new MongoRepositoryFactory(mongoOps);
        return factory.getRepository(IUserMongoRepo.class);
    }
    @Bean
    @Primary
    public IAccountRepo accountRepo(MongoOperations mongoOps) throws Exception {
        MongoRepositoryFactory factory;
        
        mongoOps.indexOps(AccountBean.class).ensureIndex(new Index().on("name", Direction.ASC).unique());
        factory = new MongoRepositoryFactory(mongoOps);
        return factory.getRepository(IAccountMongoRepo.class);
    }
    
}
