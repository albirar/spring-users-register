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
package cat.albirar.users.test.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.validation.ValidationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DuplicateKeyException;

import cat.albirar.users.models.tokens.ApprobationTokenBean;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.services.RegistrationService;
import cat.albirar.users.test.UsersRegisterAbstractDataTest;
import cat.albirar.users.test.UsersRegisterTests;
import cat.albirar.users.verification.EVerificationProcess;

/**
 * Test for invalid constraints on {@link RegistrationService}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class RegistrationServiceInvalidConstraintsTest extends UsersRegisterTests {
    /**
     * Test {@link RegistrationService#registerUser(String, cat.albirar.users.models.communications.CommunicationChannel, String)} for:
     * <ul>
     * <li>Username exists ({@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER})</li>
     * <li>Preferred Channel exists ({@link UsersRegisterAbstractDataTest#DUMMY_USERNAME}, {@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER})</li>
     * </ul>
     */
    @Test
    public void testUserChannelInvalidConstraints() {
        Assertions.assertThrows(DuplicateKeyException.class, () ->
            registrationService.registerUser(SAMPLE_REGISTERED_USER.getUsername(), SAMPLE_REGISTERED_USER.getPreferredChannel(), SAMPLE_REGISTERED_USER.getPassword()));
        
        Assertions.assertThrows(DuplicateKeyException.class, () ->
        registrationService.registerUser(DUMMY_USERNAME, SAMPLE_REGISTERED_USER.getPreferredChannel(), SAMPLE_REGISTERED_USER.getPassword()));
    }

    /**
     * Test {@link RegistrationService#verifyUser(String)} for:
     * <ul>
     * <li>Invalid token</li>
     * <li>Token expired</li>
     * <li>Process NONE, not verifiable</li>
     * <li>User not found ({@link UsersRegisterAbstractDataTest#SAMPLE_ID})</li>
     * <li>User was verified ({@link UsersRegisterAbstractDataTest#SAMPLE_VERIFIED_USER})</li>
     * </ul>
     */
    @Test
    public void testUserVerifiedInvalidConstraints() {
        Optional<Boolean> r;
        VerificationTokenBean vtk;
        
        // Invalid token
        r = registrationService.verifyUser(DUMMY_TOKEN);
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());

        // Token template
        vtk = buildAbstractToken(VerificationTokenBean.builder(), SAMPLE_VERIFIED_USER)
                .process(EVerificationProcess.TWO_STEP)
                .build()
                ;
        
        // Token expired
        r = registrationService.verifyUser(tokenManager.encodeToken(vtk.toBuilder().expire(LocalDateTime.now().minusDays(2L)).build()));
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());
        
        // Process NONE, not verifiable
        r = registrationService.verifyUser(tokenManager.encodeToken(vtk.toBuilder().process(EVerificationProcess.NONE).build()));
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());
        
        // User not found
        r = registrationService.verifyUser(tokenManager.encodeToken(vtk.toBuilder().idUser(SAMPLE_ID).build()));
        Assertions.assertFalse(r.isPresent());
        
        // User was verified
        r = registrationService.verifyUser(tokenManager.encodeToken(vtk.toBuilder().build()));
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());
    }

    /**
     * Test {@link RegistrationService#approveUser(String)} for:
     * <ul>
     * <li>Invalid token</li>
     * <li>Token expired</li>
     * <li>Process NONE, not approvable</li>
     * <li>Process ONE_STEP, not approvable</li>
     * <li>User not found ({@link UsersRegisterAbstractDataTest#SAMPLE_ID})</li>
     * <li>User was NOT verified ({@link UsersRegisterAbstractDataTest#SAMPLE_CREATED_USER})</li>
     * <li>User was registered and approved ({@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER})</li>
     * </li>
     */
    @Test
    public void testUserAproveInvalidConstraints() {
        Optional<Boolean> r;
        ApprobationTokenBean atkb;
        
        // Invalid token
        r = registrationService.approveUser(DUMMY_TOKEN);
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());

        // Token template
        atkb = tokenManager.generateApprobationTokenBean(SAMPLE_REGISTERED_USER, SAMPLE_REGISTERED_USER).get();
        
        // Token expired
        r = registrationService.approveUser(tokenManager.encodeToken(atkb.toBuilder().expire(LocalDateTime.now().minusDays(5)).build()));
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());
        
        // User not found
        r = registrationService.approveUser(tokenManager.encodeToken(atkb.toBuilder().idUser(SAMPLE_ID).build()));
        Assertions.assertFalse(r.isPresent());
        
        // User was NOT verified
        r = registrationService.approveUser(tokenManager.encodeToken(atkb.toBuilder().idUser(SAMPLE_CREATED_USER.getId()).build()));
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());
        
        // User was registered (approved)
        r = registrationService.approveUser(tokenManager.encodeToken(atkb.toBuilder().build()));
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());
    }
    
    /**
     * Test {@link RegistrationService#getUserByUsername(String)} for:
     * <ul>
     * <li>null</li>
     * <li>Empty</li>
     * <li>Blank</li>
     * <li>User not found ({@link UsersRegisterAbstractDataTest#DUMMY_USERNAME})</li>
     * <li>User found ({@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER})</li>
     * </ul>
     */
    @Test
    public void testGetUserByUsername() {
        Optional<UserBean> r;
        
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByUsername(null));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByUsername(""));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByUsername("   "));
        
        r = registrationService.getUserByUsername(DUMMY_USERNAME);
        Assertions.assertNotNull(r);
        Assertions.assertFalse(r.isPresent());

        r = registrationService.getUserByUsername(SAMPLE_REGISTERED_USER.getUsername());
        Assertions.assertNotNull(r);
        Assertions.assertTrue(r.isPresent());
        Assertions.assertEquals(SAMPLE_REGISTERED_USER.getUsername(), r.get().getUsername());
    }
    /**
     * Test {@link RegistrationService#getUserByToken(String)} for:
     * <ul>
     * <li>null</li>
     * <li>Empty</li>
     * <li>Blank</li>
     * <li>Invalid token</li>
     * <li>Token expired</li>
     * <li>User not found ({@link UsersRegisterAbstractDataTest#SAMPLE_ID})</li>
     * <li>User found ({@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER})</li>
     * </ul>
     */
    @Test
    public void testGetUserByToken() {
        Optional<UserBean> usr;
        VerificationTokenBean vtk;
        
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByToken(null));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByToken(""));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByToken("   "));

        // Token template
        vtk = buildAbstractToken(VerificationTokenBean.builder(), SAMPLE_REGISTERED_USER)
                .process(EVerificationProcess.TWO_STEP)
                .build()
                ;
        // Invalid token
        usr = registrationService.getUserByToken(DUMMY_TOKEN);
        Assertions.assertNotNull(usr);
        Assertions.assertFalse(usr.isPresent());

        // Token expired
        usr = registrationService.getUserByToken(tokenManager.encodeToken(vtk.toBuilder().expire(LocalDateTime.now().minusDays(1)).build()));
        Assertions.assertNotNull(usr);
        Assertions.assertFalse(usr.isPresent());
        
        // User not found
        usr = registrationService.getUserByToken(tokenManager.encodeToken(vtk.toBuilder().idUser(SAMPLE_ID).build()));
        Assertions.assertNotNull(usr);
        Assertions.assertFalse(usr.isPresent());

        // User found
        usr = registrationService.getUserByToken(tokenManager.encodeToken(vtk));
        Assertions.assertNotNull(usr);
        Assertions.assertTrue(usr.isPresent());
    }
    
    /**
     * Test {@link RegistrationService#updateUser(UserBean)} for:
     * <ul>
     * <li>null</li>
     * <li>User with id null</li>
     * <li>User with id empty</li>
     * <li>User with id blank</li>
     * <li>User with username null</li>
     * <li>User with username empty</li>
     * <li>User with username blank</li>
     * <li>User with preferred channel null</li>
     * <li>User with preferred channel type null</li>
     * <li>User with preferred channel id null</li>
     * <li>User with preferred channel id empty</li>
     * <li>User with preferred channel id blank</li>
     * <li>User with password null</li>
     * <li>User with password empty</li>
     * <li>User with password blank</li>
     * <li>User with created timestamp null</li>
     * <li>User not found ({@link UsersRegisterAbstractDataTest#SAMPLE_NEW_USER})</li>
     * <li>User not yet registered ({@link UsersRegisterAbstractDataTest#SAMPLE_CREATED_USER})</li>
     * <li>Attempt to update created timestamp ({@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER})</li>
     * <li>Attempt to update registered timestamp ({@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER})</li>
     * <li>Attempt to update verified timestamp ({@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER})</li>
     * <li>Update nothing ({@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER})</li>
     * <li>Update locked timestamp ({@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER})</li>
     * </ul>
     */
    @Test
    public void testUpdateUser() {
        Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(null));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().username(null).build()));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().username("").build()));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().username("   ").build()));

        Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().preferredChannel(null).build()));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().preferredChannel(SAMPLE_REGISTERED_USER.getPreferredChannel().toBuilder().channelType(null).build()).build()));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().preferredChannel(SAMPLE_REGISTERED_USER.getPreferredChannel().toBuilder().channelId(null).build()).build()));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().preferredChannel(SAMPLE_REGISTERED_USER.getPreferredChannel().toBuilder().channelId("").build()).build()));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().preferredChannel(SAMPLE_REGISTERED_USER.getPreferredChannel().toBuilder().channelId("  ").build()).build()));

        // User not found
        Assertions.assertThrows(DataRetrievalFailureException.class, () ->  registrationService.updateUser(SAMPLE_NEW_USER.toBuilder().id(DUMMY_ID).created(LocalDateTime.now().minusDays(1)).build()));
        
        // User not yet registered (set locked to be not equals as read from register)
        Assertions.assertThrows(IllegalStateException.class, () ->  registrationService.updateUser(SAMPLE_CREATED_USER.toBuilder().password(PASSWORDS[0]).locked(LocalDate.now()).build()));
        
        // Attempt to update created timestamp
        Assertions.assertThrows(IllegalArgumentException.class, () ->  registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().password(PASSWORDS[0]).created(SAMPLE_REGISTERED_USER.getCreated().minusDays(10))
                        .build()));
        // Attempt to update registered timestamp
        Assertions.assertThrows(IllegalArgumentException.class, () ->  registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().password(PASSWORDS[0]).registered(SAMPLE_REGISTERED_USER.getRegistered().minusDays(9))
                        .build()));
        // Attempt to update verified timestamp
        Assertions.assertThrows(IllegalArgumentException.class, () ->  registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().password(PASSWORDS[0]).verified(SAMPLE_REGISTERED_USER.getVerified().minusDays(8))
                        .build()));
        
        // Update nothing
        Assertions.assertFalse(registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().password(PASSWORDS[0]).build()));
        
        // Update locked timestamp
        Assertions.assertTrue(registrationService.updateUser(SAMPLE_REGISTERED_USER.toBuilder().password(PASSWORDS[0]).enabled(false).locked(LocalDate.now()).build()));
        
    }
}
