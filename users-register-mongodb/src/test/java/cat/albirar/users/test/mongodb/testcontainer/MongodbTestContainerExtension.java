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
package cat.albirar.users.test.mongodb.testcontainer;

import static cat.albirar.users.repos.mongodb.config.PropertiesMongodb.MONGODB_HOST;
import static cat.albirar.users.repos.mongodb.config.PropertiesMongodb.MONGODB_PORT;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * The jupiter extension to start and stop the mongodb test container.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public class MongodbTestContainerExtension implements BeforeAllCallback, AfterAllCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbTestContainerExtension.class);
    
    private static final Integer MONGO_PORT = 27017;
    
    @SuppressWarnings("rawtypes")
    private static GenericContainer container = null;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"rawtypes", "resource"})
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        LOGGER.debug("Start MongoDb testcontainers albirar extension...");
    
        if(container == null) {
            container = new GenericContainer("mongo:latest")
                    .withExposedPorts(MONGO_PORT);
        } else {
            if(container.isRunning()) {
                container.stop();
            }
        }
    
        container.start();
    
        System.setProperty(MONGODB_HOST, container.getContainerIpAddress());
        System.setProperty( MONGODB_PORT, container.getMappedPort(MONGO_PORT).toString());
        LOGGER.debug("MongoDb testcontainers albirar extension started with host: {} and port: {}", container.getContainerIpAddress(), container.getMappedPort(MONGO_PORT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        LOGGER.debug("End MongoDb testcontainers albirar extension...");
        if(container != null && container.isRunning()) {
            LOGGER.debug("Stopping MongoDb testcontainers albirar extension...");
            container.stop();
            LOGGER.debug("MongoDb testcontainers albirar extension stopped");
            container = null;
        }
    }
}
