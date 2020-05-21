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

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StringUtils;

import cat.albirar.users.models.registration.RegistrationProcessResultBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.registration.IRegistrationService;
import cat.albirar.users.test.UsersRegisterTests;
import cat.albirar.users.verification.EVerificationProcess;

/**
 * Test for registration without any verification ({@link EVerificationProcess#NONE}).
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class RegistrationServiceNoneTest extends UsersRegisterTests {

    @DynamicPropertySource
    public static void assignProperties(DynamicPropertyRegistry registry) {
        registry.add(IRegistrationService.VERIFICATION_MODE_PROPERTY_NAME, () -> EVerificationProcess.NONE.name());
    }

    @Test
    public void testRegistration() {
        RegistrationProcessResultBean r;
        UserBean usr;
        
        r = registrationService.registerUser(SAMPLE_NEW_USER.getUsername(), SAMPLE_NEW_USER.getPreferredChannel(), SAMPLE_NEW_USER.getPassword());
        // Conditions after registration without verification...
        Assertions.assertNotNull(r);
        Assertions.assertNotNull(r.getUser());
        Assertions.assertNotNull(r.getVerificationProcess());
        Assertions.assertFalse(r.getToken().isPresent());
        usr = r.getUser();
        Assertions.assertNotNull(usr.getId());
        Assertions.assertEquals(EVerificationProcess.NONE, r.getVerificationProcess());
        Assertions.assertNotNull(usr.getRegistered());
        Assertions.assertTrue(usr.isEnabled());
    }

    @Test
    public void testAcceptance() {
        RegistrationProcessResultBean r;
        UserBean usr;
        LocalDateTime ldt;
        
        ldt = LocalDateTime.now();
        r = registrationService.registerUser(SAMPLE_NEW_USER.getUsername(), SAMPLE_NEW_USER.getPreferredChannel(), SAMPLE_NEW_USER.getPassword());
        Assertions.assertNotNull(r);
        Assertions.assertNotNull(r.getUser());
        Assertions.assertEquals(EVerificationProcess.NONE, r.getVerificationProcess());
        Assertions.assertFalse(r.getToken().isPresent());
        usr = r.getUser();
        Assertions.assertTrue(StringUtils.hasText(usr.getId()));
        Assertions.assertEquals(EVerificationProcess.NONE, r.getVerificationProcess());
        Assertions.assertNotNull(usr.getVerified());
        Assertions.assertTrue(usr.getVerified().minusSeconds(1).isBefore(ldt)
                && usr.getVerified().plusSeconds(1).isAfter(ldt));
        Assertions.assertNotNull(usr.getRegistered());
        Assertions.assertTrue(usr.getRegistered().minusSeconds(1).isBefore(ldt)
                && usr.getRegistered().plusSeconds(1).isAfter(ldt));
        Assertions.assertFalse(usr.isEnabled());        
    }
}
