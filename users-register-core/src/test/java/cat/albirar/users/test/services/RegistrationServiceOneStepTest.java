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
import java.util.Optional;

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
 * Test for registration process with own confirmation ({@link EVerificationProcess#ONE_STEP}).
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class RegistrationServiceOneStepTest extends UsersRegisterTests {

    @DynamicPropertySource
    public static void assignProperties(DynamicPropertyRegistry registry) {
        registry.add(IRegistrationService.VERIFICATION_MODE_PROPERTY_NAME, () -> EVerificationProcess.ONE_STEP.name());
    }

    @Test
    public void testRegistration() {
        RegistrationProcessResultBean r;
        UserBean usr;
        
        r = registrationService.registerUser(SAMPLE_NEW_USER.getUsername(), SAMPLE_NEW_USER.getPreferredChannel(), SAMPLE_NEW_USER.getPassword());
        // Conditions after registration with one step verification...
        Assertions.assertNotNull(r);
        Assertions.assertNotNull(r.getUser());
        Assertions.assertNotNull(r.getVerificationProcess());
        Assertions.assertTrue(r.getToken().isPresent() && StringUtils.hasText(r.getToken().get()));
        usr = r.getUser();
        Assertions.assertNotNull(usr.getId());
        Assertions.assertEquals(EVerificationProcess.ONE_STEP, r.getVerificationProcess());
        Assertions.assertFalse(usr.isEnabled());
        Assertions.assertNotNull(usr.getCreated());
        Assertions.assertNull(usr.getVerified());
        Assertions.assertNull(usr.getRegistered());
        Assertions.assertFalse(usr.isEnabled());
        
    }

    @Test
    public void testAcceptance() {
        RegistrationProcessResultBean r;
        UserBean rUsr;
        Optional<UserBean> oUsr;
        Optional<Boolean> b;
        LocalDateTime ldt1, ldt2;
        
        ldt1 = LocalDateTime.now().minusSeconds(1);
        r = registrationService.registerUser(SAMPLE_NEW_USER.getUsername(), SAMPLE_NEW_USER.getPreferredChannel(), SAMPLE_NEW_USER.getPassword());
        ldt2 = LocalDateTime.now().plusSeconds(1);
        Assertions.assertNotNull(r);
        Assertions.assertTrue(r.getToken().isPresent() && StringUtils.hasText(r.getToken().get()));
        Assertions.assertEquals(EVerificationProcess.ONE_STEP, r.getVerificationProcess());
        Assertions.assertNotNull(r.getUser());

        // Check if user is created but not registered
        oUsr = registrationService.getUserByToken(r.getToken().get());
        Assertions.assertNotNull(oUsr);
        Assertions.assertTrue(oUsr.isPresent());
        Assertions.assertFalse(oUsr.get().isEnabled());
        Assertions.assertNotNull(oUsr.get().getCreated());
        Assertions.assertTrue(ldt1.isBefore(oUsr.get().getCreated()) && ldt2.isAfter(oUsr.get().getCreated()));
        Assertions.assertNull(oUsr.get().getRegistered());
        Assertions.assertNull(oUsr.get().getVerified());
        
        // Do the acceptance, should return TRUE and the persisted user should to be ENABLED and REGISTERED
        ldt1 = LocalDateTime.now().minusSeconds(1);
        b = registrationService.verifyUser(r.getToken().get());
        ldt2 = LocalDateTime.now().plusSeconds(1);
        Assertions.assertNotNull(b);
        Assertions.assertTrue(b.isPresent());
        Assertions.assertTrue(b.get());
        
        oUsr = registrationService.getUserByUsername(r.getUser().getUsername());
        Assertions.assertNotNull(oUsr);
        Assertions.assertTrue(oUsr.isPresent());
        rUsr = oUsr.get();
        
        Assertions.assertNotNull(rUsr);
        Assertions.assertNotNull(rUsr.getRegistered());
        Assertions.assertNotNull(rUsr.getVerified());
        Assertions.assertTrue(ldt1.isBefore(oUsr.get().getRegistered()) && ldt2.isAfter(oUsr.get().getRegistered()));
        Assertions.assertTrue(ldt1.isBefore(oUsr.get().getVerified()) && ldt2.isAfter(oUsr.get().getVerified()));
        Assertions.assertTrue(rUsr.isEnabled());
    }
}
