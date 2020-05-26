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
package cat.albirar.users.test.repos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;

import cat.albirar.users.models.auth.AuthorizationBean;
import cat.albirar.users.models.auth.ERole;
import cat.albirar.users.models.communications.CommunicationChannel;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.repos.IUserRepo;
import cat.albirar.users.test.UsersRegisterTests;

/**
 * The main test for {@link IUserRepo}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class UserRepoTest extends UsersRegisterTests {
    @Test
    public void testCount() {
        Assertions.assertEquals((long)USERS.length, userRepo.count());
    }
    
    @Test
    public void testFindAll() {
        List<UserBean> r, expected;
        
        r = userRepo.findAll();
        Assertions.assertNotNull(r);
        Assertions.assertFalse(r.isEmpty());
        Assertions.assertEquals(USERS.length, r.size());
        r = r.stream().sorted((c1,c2)->c1.getId().compareTo(c2.getId())).collect(Collectors.toList());
        expected = Stream.of(USERS).sorted((c1,c2)->c1.getId().compareTo(c2.getId())).collect(Collectors.toList());
        for(int n = 0; n < expected.size(); n++) {
            equalsUsers(expected.get(n), r.get(n));
        }
    }
    
    @Test
    public void testExistById() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsById(null));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsById(""));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsById("  "));
        
        Assertions.assertFalse(userRepo.existsById(DUMMY_ID));
        Assertions.assertTrue(userRepo.existsById(USERS[0].getId()));
    }
    
    @Test
    public void testFindById() {
        Optional<UserBean> oUsr;
        
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.findById(null));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.findById(""));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.findById("  "));
        
        oUsr = userRepo.findById(DUMMY_ID);
        Assertions.assertNotNull(oUsr);
        Assertions.assertFalse(oUsr.isPresent());
        
        oUsr = userRepo.findById(USERS[0].getId());
        Assertions.assertNotNull(oUsr);
        Assertions.assertTrue(oUsr.isPresent());
        equalsUsers(USERS[0], oUsr.get());
    }
    
    @Test
    public void testExistByUsername() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsByUsername(null));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsByUsername(""));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsByUsername("  "));
        
        Assertions.assertFalse(userRepo.existsByUsername(DUMMY_USERNAME));
        Assertions.assertTrue(userRepo.existsByUsername(USERS[0].getUsername()));
    }
    
    @Test
    public void testFindByUsername() {
        Optional<UserBean> oUsr;
        
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.findByUsername(null));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.findByUsername(""));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.findByUsername("  "));
        
        oUsr = userRepo.findByUsername(DUMMY_USERNAME);
        Assertions.assertNotNull(oUsr);
        Assertions.assertFalse(oUsr.isPresent());
        
        oUsr = userRepo.findByUsername(USERS[0].getUsername());
        Assertions.assertNotNull(oUsr);
        Assertions.assertTrue(oUsr.isPresent());
        equalsUsers(USERS[0], oUsr.get());
    }
    
    @Test
    public void testExistsByPreferredChannel() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsByPreferredChannel(null));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsByPreferredChannel(CommunicationChannel.builder().build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsByPreferredChannel(CHANNELS[0].toBuilder().channelType(null).build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsByPreferredChannel(CHANNELS[0].toBuilder().channelId(null).build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsByPreferredChannel(CHANNELS[0].toBuilder().channelId("").build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsByPreferredChannel(CHANNELS[0].toBuilder().channelId("  ").build()));
        
        Assertions.assertFalse(userRepo.existsByPreferredChannel(CHANNELS[0].toBuilder().channelId(DUMMY_NAME).build()));
        Assertions.assertTrue(userRepo.existsByPreferredChannel(CHANNELS[1]));
    }
    
    @Test
    public void testExistsBySecondaryChannel() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsBySecondaryChannel(null));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsBySecondaryChannel(CommunicationChannel.builder().build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsBySecondaryChannel(CHANNELS[0].toBuilder().channelType(null).build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsBySecondaryChannel(CHANNELS[0].toBuilder().channelId(null).build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsBySecondaryChannel(CHANNELS[0].toBuilder().channelId("").build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.existsBySecondaryChannel(CHANNELS[0].toBuilder().channelId("  ").build()));
        
        Assertions.assertFalse(userRepo.existsBySecondaryChannel(CHANNELS[0].toBuilder().channelId(DUMMY_NAME).build()));
        Assertions.assertTrue(userRepo.existsBySecondaryChannel(CHANNELS[3]));
    }
    
    @Test
    public void testSave() {
        // Bean constraints validations ***
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.save(null));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.save(UserBean.builder().username(null).build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.save(UserBean.builder().username("").build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.save(UserBean.builder().username("   ").build()));

        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.save(UserBean.builder().username(DUMMY_NAME).preferredChannel(null).build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.save(UserBean.builder().username(DUMMY_NAME).preferredChannel(null).build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.save(UserBean.builder().username(DUMMY_NAME).preferredChannel(CHANNELS[0].toBuilder().channelType(null).build()).build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.save(UserBean.builder().username(DUMMY_NAME).preferredChannel(CHANNELS[0].toBuilder().channelId(null).build()).build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.save(UserBean.builder().username(DUMMY_NAME).preferredChannel(CHANNELS[0].toBuilder().channelId("").build()).build()));
        Assertions.assertThrows(ConstraintViolationException.class, () -> userRepo.save(UserBean.builder().username(DUMMY_NAME).preferredChannel(CHANNELS[0].toBuilder().channelId("  ").build()).build()));
        
        // Repository constraints ***
        // Duplications...
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> userRepo.save(USERS[0].toBuilder().id(USERS[1].getId()).build()));
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> userRepo.save(USERS[0].toBuilder().id(USERS[1].getId()).username(DUMMY_USERNAME).build()));        
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> userRepo.save(USERS[0].toBuilder().id(USERS[1].getId()).preferredChannel(SAMPLE_NEW_CHANNEL).build()));
        
        {
            // Creating OK
            UserBean usr, usr1;
            
            usr = USERS[0].toBuilder().id(null).username(DUMMY_USERNAME).preferredChannel(SAMPLE_NEW_CHANNEL).build();
            usr1 = userRepo.save(usr);
            Assertions.assertNotNull(usr1);
            Assertions.assertTrue(StringUtils.hasText(usr1.getId()));
            usr.setId(usr1.getId());
            equalsUsers(usr, usr1);
            
            // Creating with id
            usr = USERS[0].toBuilder().id(DUMMY_ID).username(DUMMY_USERNAME_1).preferredChannel(SAMPLE_NEW_CHANNEL.toBuilder().channelId("987987987").build()).build();
            usr1 = userRepo.save(usr);
            Assertions.assertNotNull(usr1);
            equalsUsers(usr, usr1);
            
        }
        {
            // Updating OK
            UserBean usr, usr1;
            
            usr = USERS[0].toBuilder().username(DUMMY_USERNAME_2).build();
            usr1 = userRepo.save(usr);
            Assertions.assertNotNull(usr1);
            equalsUsers(usr, usr1);
        }
    }
    
    @Test
    public void testSaveAuthorities() {
        UserBean usr, usr1;
        List<AuthorizationBean> auths, usrauth;
        
        usr = USERS[0].toBuilder().authorities(new ArrayList<>()).build();
        usr1 = userRepo.save(usr);
        Assertions.assertNotNull(usr1);
        equalsUsers(usr, usr1);
        
        auths = new ArrayList<>();
        auths.add(AuthorizationBean.builder().authority(ERole.AccountAdministrator.name()).build());
        usr = USERS[0].toBuilder().authorities(new ArrayList<>(auths)).build();
        usr1 = userRepo.save(usr);
        Assertions.assertNotNull(usr1);
        equalsUsers(usr, usr1);
        Assertions.assertEquals(auths, usr1.getAuthorities());
        
        auths.add(AuthorizationBean.builder().authority(DUMMY_ROLE).build());
        usr = USERS[0].toBuilder().authorities(new ArrayList<>(auths)).build();
        usr1 = userRepo.save(usr);
        Assertions.assertNotNull(usr1);
        equalsUsers(usr, usr1);
        // Sort for equals 
        auths = auths.stream().sorted((a1, a2) -> a1.getAuthority().compareTo(a2.getAuthority())).collect(Collectors.toList());
        usrauth = usr1.getAuthorities().stream().sorted((a1, a2) -> a1.getAuthority().compareTo(a2.getAuthority())).collect(Collectors.toList());
        Assertions.assertEquals(auths, usrauth);
    }
    /**
     * Verify if {@code actual} is equal to {@code expected} without password and compare until seconds for timestamps. 
     * @param expected The expected user
     * @param actual The actual user
     */
    private void equalsUsers(UserBean expected, UserBean actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getUsername(), actual.getUsername());
        Assertions.assertEquals(expected.getPreferredChannel(), actual.getPreferredChannel());
        Assertions.assertEquals(expected.getSecondaryChannel(), actual.getSecondaryChannel());
        Assertions.assertNotNull(actual.getCreated());
        Assertions.assertTrue(expected.getCreated().minusSeconds(1).isBefore(actual.getCreated())
                && expected.getCreated().plusSeconds(1).isAfter(actual.getCreated()));
        if(expected.getVerified() != null) {
            Assertions.assertNotNull(actual.getVerified());
            Assertions.assertTrue(expected.getVerified().minusSeconds(1).isBefore(actual.getVerified())
                    && expected.getVerified().plusSeconds(1).isAfter(actual.getVerified()));
        }
        if(expected.getRegistered() != null) {
            Assertions.assertNotNull(actual.getRegistered());
            Assertions.assertTrue(expected.getRegistered().minusSeconds(1).isBefore(actual.getRegistered())
                    && expected.getRegistered().plusSeconds(1).isAfter(actual.getRegistered()));
        }
        Assertions.assertEquals(expected.getExpire(), actual.getExpire());
        Assertions.assertEquals(expected.getLocked(), actual.getLocked());
        Assertions.assertEquals(expected.getExpireCredentials(), actual.getExpireCredentials());
        Assertions.assertEquals(expected.isEnabled(), actual.isEnabled());
        Assertions.assertEquals(expected.getAuthorities().stream().sorted((c1, c2) -> c1.getAuthority().compareTo(c2.getAuthority())).collect(Collectors.toList())
                , actual.getAuthorities().stream().sorted((c1, c2) -> c1.getAuthority().compareTo(c2.getAuthority())).collect(Collectors.toList()));
    }
}
