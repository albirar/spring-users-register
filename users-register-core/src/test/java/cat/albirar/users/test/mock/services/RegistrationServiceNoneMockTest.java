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
package cat.albirar.users.test.mock.services;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import cat.albirar.users.repos.IUserRepo;
import cat.albirar.users.test.mock.MockUtils;
import cat.albirar.users.test.services.RegistrationServiceNoneTest;

/**
 * The mock backed for {@link RegistrationServiceNoneTest}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public class RegistrationServiceNoneMockTest extends RegistrationServiceNoneTest {

    @Autowired
    private IUserRepo userRepo;
    
    @BeforeEach
    public void setupTest() {
        MockUtils.instance().setupRegisteredUsers(userRepo);
    }
}
