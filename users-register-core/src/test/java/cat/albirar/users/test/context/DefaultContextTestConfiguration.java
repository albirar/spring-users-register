/*
 * This file is part of "albirar users-register".
 * 
 * "albirar users-register" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "albirar users-register" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "albirar users-register" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.test.context;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cat.albirar.users.repos.IAccountRepo;
import cat.albirar.users.repos.IUserRepo;

/**
 * The default mock test configuration, applicable only if no other beans are defined.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Configuration
public class DefaultContextTestConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultContextTestConfiguration.class);
    
    @Bean
    public IUserRepo userRepo() {
        LOGGER.debug("Mocking user repo...");
        return Mockito.mock(IUserRepo.class);
    }
    
    @Bean
    public IAccountRepo accountRepo() {
        LOGGER.debug("Mocking account repo...");
        return Mockito.mock(IAccountRepo.class);
    }
}
