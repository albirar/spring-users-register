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
package cat.albirar.users.test.mocks.services;

import static cat.albirar.users.test.mock.ValidMatchers.anyObject;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.repos.IUserRepo;
import cat.albirar.users.test.mock.MockUtils;
import cat.albirar.users.test.services.RegistrationServiceTwoStepTest;

/**
 * The mock backed for {@link RegistrationServiceTwoStepTest}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public class RegistrationServiceTwoStepMockTest extends RegistrationServiceTwoStepTest {

    @Autowired
    private IUserRepo userRepo;
    
    @BeforeEach
    public void setupTest() {
        MockUtils.instance().setupRegisteredUsers(userRepo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void testRegistration() {
        Mockito.when(userRepo.existsByUsername(SAMPLE_NEW_USER.getUsername())).thenReturn(false);
        Mockito.when(userRepo.existsByPreferredChannel(SAMPLE_NEW_USER.getPreferredChannel())).thenReturn(false);
        Mockito.when(userRepo.save(anyObject(UserBean.class, USERS[0]))).thenReturn(SAMPLE_NEW_USER.toBuilder().id(SAMPLE_ID).registered(null).enabled(false).build());

        super.testRegistration();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void testAcceptance() {
        
        // Mocks...
        Mockito.when(userRepo.existsByUsername(SAMPLE_NEW_USER.getUsername())).thenReturn(false);
        Mockito.when(userRepo.existsByPreferredChannel(SAMPLE_NEW_USER.getPreferredChannel())).thenReturn(false);
        
        Mockito.when(userRepo.save(anyObject(UserBean.class, USERS[0]))).thenReturn(SAMPLE_NEW_USER_CREATED);
        
        Mockito.when(userRepo.findById(DUMMY_ID)).thenReturn(Optional.of(SAMPLE_NEW_USER_CREATED));
        
        Mockito.when(userRepo.findByUsername(SAMPLE_NEW_USER_CREATED.getUsername())).thenReturn(Optional.of(SAMPLE_NEW_USER_CREATED));
        
        super.testAcceptance();
    }
}
