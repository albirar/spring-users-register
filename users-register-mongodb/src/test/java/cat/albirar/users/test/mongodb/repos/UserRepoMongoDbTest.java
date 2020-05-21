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
package cat.albirar.users.test.mongodb.repos;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;

import cat.albirar.users.models.communications.CommunicationChannel;
import cat.albirar.users.models.communications.ECommunicationChannelType;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.repos.IAccountRepo;
import cat.albirar.users.repos.IUserRepo;
import cat.albirar.users.test.mongodb.MongodbTestUtils;
import cat.albirar.users.test.mongodb.UsersRegisterMongoDbTestConfig;
import cat.albirar.users.test.mongodb.testcontainer.MongodbTestContainerExtension;
import cat.albirar.users.test.repos.UserRepoTest;

/**
 * Tests for {@link IUserRepo}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@ContextConfiguration(classes = {UsersRegisterMongoDbTestConfig.class})
@ExtendWith(MongodbTestContainerExtension.class)
public class UserRepoMongoDbTest extends UserRepoTest {

    @Autowired
    protected IUserRepo userRepo;
    @Autowired
    protected IAccountRepo accountRepo;
    
    @BeforeEach
    public void setupTest() {
        MongodbTestUtils.instance().setupData(userRepo, accountRepo);
    }
    @AfterEach
    public void teardownTest() {
        MongodbTestUtils.instance().teardownData(userRepo, accountRepo);
    }
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testCheckAuth() {
        for(int n = 0; n < PASSWORDS.length; n++) {
            Optional<UserBean> oUsr;
            
            Assertions.assertTrue(userRepo.existsByUsername(USERS[n].getUsername()));
            oUsr = userRepo.findByUsername(USERS[n].getUsername());
            Assertions.assertTrue(oUsr.isPresent());
            Assertions.assertTrue(StringUtils.hasText(oUsr.get().getPassword()));
            Assertions.assertTrue(passwordEncoder.matches(PASSWORDS[n], oUsr.get().getPassword()), "At " + n);
        }
    }

    @Test
    public void testExistsByUsername() {
        for(int n = 0; n < PASSWORDS.length; n++) {
            Assertions.assertTrue(userRepo.existsByUsername(USERS[n].getUsername()), "At " + n);
        }
    }
    /**
     * Test creation of new user.
     */
    @Test
    public void testNewUser() {
        UserBean usr;
        UserBean newUsr;
        Optional<UserBean> oUsr;
        long cA, cB;
        
        usr = SAMPLE_NEW_USER.toBuilder().build();
        cA = userRepo.count();
        newUsr = userRepo.save(usr);
        cB = userRepo.count();

        Assertions.assertEquals(cA+1, cB);
        
        Assertions.assertNotNull(newUsr);
        Assertions.assertNotNull(newUsr.getId());
        Assertions.assertTrue(StringUtils.hasText(newUsr.getId().toString()));
        
        Assertions.assertEquals(usr.getUsername(), newUsr.getUsername());
        Assertions.assertEquals(usr.getPreferredChannel(), newUsr.getPreferredChannel());
        Assertions.assertEquals(usr.getSecondaryChannel(), newUsr.getSecondaryChannel());
        Assertions.assertEquals(usr.getExpire(), newUsr.getExpire());
        Assertions.assertEquals(usr.getLocked(), newUsr.getLocked());
        Assertions.assertEquals(usr.getExpireCredentials(), newUsr.getExpireCredentials());
        Assertions.assertEquals(usr.isEnabled(), newUsr.isEnabled());
        Assertions.assertEquals(usr.getAuthorities(), newUsr.getAuthorities());
        
        // Finally
        oUsr = userRepo.findById(newUsr.getId());
        Assertions.assertTrue(oUsr.isPresent());
        Assertions.assertEquals(newUsr, oUsr.get());
    }
    
    @Test
    public void testExistsByPreferredChanel() {
        boolean r;
        CommunicationChannel c;
        
        for(UserBean usr : USERS) {
            r = userRepo.existsByPreferredChannel(usr.getPreferredChannel());
            Assertions.assertTrue(r);
            if(usr.getSecondaryChannel() != null) {
                r = userRepo.existsBySecondaryChannel(usr.getSecondaryChannel());
                Assertions.assertTrue(r);
            }
        }
        c = CommunicationChannel.builder()
                .channelType(ECommunicationChannelType.EMAIL)
                .channelId("x@x.com")
                .build();
        r = userRepo.existsByPreferredChannel(c);
        Assertions.assertFalse(r);
        r = userRepo.existsBySecondaryChannel(c);
        Assertions.assertFalse(r);
        c = CommunicationChannel.builder()
                .channelType(ECommunicationChannelType.MOBILE)
                .channelId("999999999")
                .build();
        r = userRepo.existsByPreferredChannel(c);
        Assertions.assertFalse(r);
        r = userRepo.existsBySecondaryChannel(c);
        Assertions.assertFalse(r);
    }
}
