/*
 * This file is part of "albirar spring-users-register-core".
 * 
 * "albirar spring-users-register-core" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "albirar spring-users-register-core" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "albirar spring-users-register-core" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.test.services.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.validation.ValidationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;

import cat.albirar.communications.models.CommunicationChannelBean;
import cat.albirar.communications.models.ECommunicationChannelType;
import cat.albirar.users.models.tokens.ETokenClass;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.registration.IRegistrationService;
import cat.albirar.users.test.UsersRegisterTests;
import cat.albirar.users.verification.EVerificationProcess;
import cat.albirar.users.verification.ITokenManager;
import io.jsonwebtoken.JwtBuilder;

/**
 * Test services of {@link IRegistrationService} not related to registration process.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class RegistrationServiceTest extends UsersRegisterTests {
    
    @Test
    public void testGetUserByUsername() {
        Optional<UserBean> oUsr;

        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByUsername(null));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByUsername(""));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByUsername("   "));
        
        oUsr = registrationService.getUserByUsername(DUMMY_USERNAME_2);
        Assertions.assertFalse(oUsr.isPresent());
        
        oUsr = registrationService.getUserByUsername(SAMPLE_REGISTERED_USER.getUsername());
        Assertions.assertTrue(oUsr.isPresent());
        // Ensure password and authorities are for 'equal' compare
        Assertions.assertTrue(equalsEnsurePasswordAuthorities(SAMPLE_REGISTERED_USER.toBuilder().password(PASSWORDS[0]).build(), oUsr.get()));
    }
    @Test
    public void testGetUserById() {
        Optional<UserBean> oUsr;

        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserById(null));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserById(""));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserById("   "));
        
        oUsr = registrationService.getUserById(SAMPLE_ID);
        Assertions.assertFalse(oUsr.isPresent());
        
        oUsr = registrationService.getUserById(SAMPLE_REGISTERED_USER.getId());
        Assertions.assertTrue(oUsr.isPresent());
        // Ensure password and authorities are for 'equal' compare
        Assertions.assertTrue(equalsEnsurePasswordAuthorities(SAMPLE_REGISTERED_USER.toBuilder().password(PASSWORDS[0]).build(), oUsr.get()));
    }
    @Test
    public void testGetUserByToken() {
        JwtBuilder builder;
        Optional<UserBean> oUsr;

        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByToken(null));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByToken(""));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.getUserByToken("   "));
        
        oUsr = registrationService.getUserByToken(createJwtBuilder().compact());
        Assertions.assertFalse(oUsr.isPresent());
        
        builder = addTokenInformation(createJwtBuilder(), ETokenClass.VERIFICATION, SAMPLE_REGISTERED_USER.toBuilder().id(null).build());
        oUsr = registrationService.getUserByToken(builder.compact());
        Assertions.assertFalse(oUsr.isPresent());
        
        builder = addTokenInformation(createJwtBuilder(), ETokenClass.VERIFICATION, SAMPLE_REGISTERED_USER.toBuilder().id("").build());
        oUsr = registrationService.getUserByToken(builder.compact());
        Assertions.assertFalse(oUsr.isPresent());
        
        builder = addTokenInformation(createJwtBuilder(), ETokenClass.VERIFICATION, SAMPLE_REGISTERED_USER.toBuilder().id("   ").build());
        oUsr = registrationService.getUserByToken(builder.compact());
        Assertions.assertFalse(oUsr.isPresent());
        
        builder = addTokenInformation(createJwtBuilder(), ETokenClass.VERIFICATION, SAMPLE_REGISTERED_USER.toBuilder().id(DUMMY_ID).build());
        oUsr = registrationService.getUserByToken(builder.compact());
        Assertions.assertFalse(oUsr.isPresent());
        
        builder = addTokenInformation(createJwtBuilder(), ETokenClass.VERIFICATION, SAMPLE_REGISTERED_USER);
        oUsr = registrationService.getUserByToken(builder.compact());
        Assertions.assertTrue(oUsr.isPresent());
        // Ensure password and authorities are for 'equal' compare
        Assertions.assertTrue(equalsEnsurePasswordAuthorities(SAMPLE_REGISTERED_USER.toBuilder().password(PASSWORDS[0]).build(), oUsr.get()));
    }
    
    @Test
    public void testUpdateUser() {
        UserBean usr, usr1;
        
        // Invalids
        Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(null));
        usr = UserBean.builder().build();
        {
            UserBean usrs  = usr.toBuilder().build();
            Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(usrs));
        }

        usr = usr.toBuilder().id(null).build();
        {
            UserBean usrs  = usr.toBuilder().build();
            Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(usrs));
        }

        usr = usr.toBuilder().id("").build();
        {
            UserBean usrs  = usr.toBuilder().build();
            Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(usrs));
        }

        usr = usr.toBuilder().id("  ").build();
        {
            UserBean usrs  = usr.toBuilder().build();
            Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(usrs));
        }
        usr = usr.toBuilder().id(SAMPLE_ID).username(DUMMY_USERNAME).build();
        {
            UserBean usrs  = usr.toBuilder().build();
            Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(usrs));
        }
        usr = usr.toBuilder().preferredChannel(CommunicationChannelBean.builder().build()).build();
        {
            UserBean usrs  = usr.toBuilder().build();
            Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(usrs));
        }
        usr = usr.toBuilder().preferredChannel(usr.getPreferredChannel().toBuilder().channelType(ECommunicationChannelType.EMAIL).build()).build();
        {
            UserBean usrs  = usr.toBuilder().build();
            Assertions.assertThrows(ValidationException.class, () -> registrationService.updateUser(usrs));
        }
        // Not found user
        usr = usr.toBuilder().preferredChannel(usr.getPreferredChannel().toBuilder().channelId("test@host.com").build()).build();
        {
            UserBean usrs  = usr.toBuilder().build();
            Assertions.assertThrows(DataRetrievalFailureException.class, () -> registrationService.updateUser(usrs));
        }
        // Found users...
        // Illegal state
        usr = SAMPLE_VERIFIED_USER.toBuilder().build();
        {
            UserBean usrs  = usr.toBuilder().username(DUMMY_USERNAME_2).build();
            Assertions.assertThrows(IllegalStateException.class, () -> registrationService.updateUser(usrs));
        }
        // IllegalArgument (read-only fields)
        usr = SAMPLE_REGISTERED_USER.toBuilder().registered(LocalDateTime.now()).build();
        {
            UserBean usrs  = usr.toBuilder().build();
            Assertions.assertThrows(IllegalArgumentException.class, () -> registrationService.updateUser(usrs));
        }
        // DuplicateKey (duplicate username, preferred or secondary channels fields)
        usr = SAMPLE_REGISTERED_USER.toBuilder().preferredChannel(SAMPLE_CREATED_USER.getPreferredChannel()).build();
        {
            UserBean usrs  = usr.toBuilder().build();
            Assertions.assertThrows(DataIntegrityViolationException.class, () -> registrationService.updateUser(usrs));
        }
        usr = SAMPLE_REGISTERED_USER.toBuilder().username(SAMPLE_VERIFIED_USER.getUsername()).build();
        {
            UserBean usrs  = usr.toBuilder().build();
            Assertions.assertThrows(DataIntegrityViolationException.class, () -> registrationService.updateUser(usrs));
        }
        // With changes values
        usr = SAMPLE_REGISTERED_USER.toBuilder().password(PASSWORDS[0]).secondaryChannel(CHANNELS[6]).build();
        Assertions.assertTrue(registrationService.updateUser(usr));
        usr1 = userRepo.findById(usr.getId()).get();
        Assertions.assertEquals(CHANNELS[6], usr1.getSecondaryChannel());
        // Ensure password and authorities are for 'equal' compare
        Assertions.assertTrue(equalsEnsurePasswordAuthorities(usr, usr1));
        
        // With changes NULL
        usr = SAMPLE_REGISTERED_USER.toBuilder().password(PASSWORDS[0]).secondaryChannel(null).build();
        Assertions.assertTrue(registrationService.updateUser(usr));
        usr1 = userRepo.findById(usr.getId()).get();
        Assertions.assertNull(usr1.getSecondaryChannel());
        // Ensure password and authorities are for 'equal' compare
        Assertions.assertTrue(equalsEnsurePasswordAuthorities(usr, usr1));

        // Another time with changes values
        usr = SAMPLE_REGISTERED_USER.toBuilder().password(PASSWORDS[0]).secondaryChannel(CHANNELS[6]).build();
        Assertions.assertTrue(registrationService.updateUser(usr));
        usr1 = userRepo.findById(usr.getId()).get();
        Assertions.assertEquals(CHANNELS[6], usr1.getSecondaryChannel());
        // Ensure password and authorities are for 'equal' compare
        Assertions.assertTrue(equalsEnsurePasswordAuthorities(usr, usr1));
        
        // Without changes
        Assertions.assertFalse(registrationService.updateUser(usr));
        usr1 = userRepo.findById(usr.getId()).get();
        // Ensure password and authorities are for 'equal' compare
        Assertions.assertTrue(equalsEnsurePasswordAuthorities(usr, usr1));
    }
    
    @Test
    public void testRecoverPassword() {
        JwtBuilder builder;
        Optional<Boolean> r;
        UserBean usr;
        
        // Invalids
        Assertions.assertThrows(ValidationException.class, () -> registrationService.recoverPassword(null, null));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.recoverPassword(null, ""));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.recoverPassword(null, "   "));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.recoverPassword(null, PASSWORDS[2]));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.recoverPassword("", PASSWORDS[2]));
        Assertions.assertThrows(ValidationException.class, () -> registrationService.recoverPassword("  ", PASSWORDS[2]));
        
        // Invalid for content ****
        builder = createJwtBuilder();
        // Invalid token
        r = registrationService.recoverPassword(builder.compact(), PASSWORDS[2]);
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());
        
        // Invalid token class (verification)
        builder = addTokenInformation(builder, ETokenClass.VERIFICATION, SAMPLE_REGISTERED_USER);
        builder.setAudience(EVerificationProcess.ONE_STEP.name());
        r = registrationService.recoverPassword(builder.compact(), PASSWORDS[2]);
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());
        
        // Invalid token class (approbation)
        builder = addTokenInformation(createJwtBuilder(), ETokenClass.APPROBATION, SAMPLE_REGISTERED_USER);
        builder.claim(ITokenManager.CLAIM_APPROVER_ID, SAMPLE_REGISTERED_USER.getId())
            .claim(ITokenManager.CLAIM_APPROVER_USERNAME, SAMPLE_REGISTERED_USER.getUsername());
        
        r = registrationService.recoverPassword(builder.compact(), PASSWORDS[2]);
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());
        
        // Valid token but user not found
        builder = addTokenInformation(createJwtBuilder(), ETokenClass.RECOVER_PASSWORD, SAMPLE_NEW_USER.toBuilder().id(DUMMY_ID).build());
        builder.claim(ITokenManager.CLAIM_ORIGIN_CHANNEL, ECommunicationChannelType.EMAIL.name());
        
        r = registrationService.recoverPassword(builder.compact(), PASSWORDS[2]);
        Assertions.assertFalse(r.isPresent());
        
        // Valid token but user not yet registered
        builder = addTokenInformation(createJwtBuilder(), ETokenClass.RECOVER_PASSWORD, SAMPLE_CREATED_USER);
        builder.claim(ITokenManager.CLAIM_ORIGIN_CHANNEL, ECommunicationChannelType.EMAIL.name());
        
        r = registrationService.recoverPassword(builder.compact(), PASSWORDS[2]);
        Assertions.assertTrue(r.isPresent());
        Assertions.assertFalse(r.get());
        
        // Valid token and found and valid user
        builder = addTokenInformation(createJwtBuilder(), ETokenClass.RECOVER_PASSWORD, SAMPLE_REGISTERED_USER);
        builder.claim(ITokenManager.CLAIM_ORIGIN_CHANNEL, ECommunicationChannelType.EMAIL.name());
        
        r = registrationService.recoverPassword(builder.compact(), PASSWORDS[2]);
        Assertions.assertTrue(r.isPresent());
        Assertions.assertTrue(r.get());
        
        // Check password
        usr = userRepo.findById(SAMPLE_REGISTERED_USER.getId()).get();
        Assertions.assertTrue(passwordEncoder.matches(PASSWORDS[2], usr.getPassword()));
    }
}
