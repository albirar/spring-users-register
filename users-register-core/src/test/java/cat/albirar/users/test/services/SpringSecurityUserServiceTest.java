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
package cat.albirar.users.test.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import cat.albirar.users.services.SpringSecurityUserService;
import cat.albirar.users.test.UsersRegisterTests;

/**
 * Test for {@link SpringSecurityUserService}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class SpringSecurityUserServiceTest extends UsersRegisterTests {

    @Autowired
    protected SpringSecurityUserService springSecurityUserService;

    @Test
    public void testConstraints() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> springSecurityUserService.loadUserByUsername(null));
        Assertions.assertThrows(UsernameNotFoundException.class, () -> springSecurityUserService.loadUserByUsername(""));
        Assertions.assertThrows(UsernameNotFoundException.class, () -> springSecurityUserService.loadUserByUsername("   "));
    }
    @Test
    public void testUserNotFound() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> springSecurityUserService.loadUserByUsername(DUMMY_USERNAME));
    }
    @Test
    public void testUserWithoutAuthorities() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> springSecurityUserService.loadUserByUsername(SAMPLE_REGISTERED_USER_NO_AUTHORITIES.getUsername()));
    }
    @Test
    public void testUserOk() {
        UserDetails ud;
        
        ud = springSecurityUserService.loadUserByUsername(SAMPLE_REGISTERED_USER.getUsername());
        Assertions.assertNotNull(ud);
    }
}
