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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import cat.albirar.users.models.auth.AuthorizationBean;
import cat.albirar.users.models.auth.ERole;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.repos.IUserRepo;
import cat.albirar.users.test.mock.MockUtils;
import cat.albirar.users.test.services.RegistrationServiceOneStepTest;
import cat.albirar.users.verification.EVerificationProcess;

/**
 * The mock backed for {@link RegistrationServiceOneStepTest}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public class RegistrationServiceOneStepMockTest extends RegistrationServiceOneStepTest {

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
        // Mocks...
        Mockito.when(userRepo.existsByUsername(DUMMY_USERNAME)).thenReturn(false);
        Mockito.when(userRepo.existsByPreferredChannel(CHANNELS[0])).thenReturn(false);
        Mockito.when(userRepo.save(anyObject(UserBean.class, USERS[0]))).thenReturn(SAMPLE_NEW_USER.toBuilder().id(SAMPLE_ID).registered(null).enabled(false).build());
        
        super.testRegistration();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void testAcceptance() {
        LocalDateTime ldt;
        VerificationTokenBean vtk;
        UserBean ub, ub1;

        // Mocks...
        ldt = LocalDateTime.now();
        ub = SAMPLE_NEW_USER.toBuilder()
                .created(ldt)
                .id(SAMPLE_ID)
                .enabled(false)
                .authorities(Arrays.asList(new AuthorizationBean [] {AuthorizationBean.builder().authority(ERole.User.name()).build()}))
                .build()
                ;
        ub1 = ub.toBuilder()
                .registered(ldt)
                .verified(ldt)
                .enabled(true)
                .build()
                ;
        // Mocks...
        vtk = VerificationTokenBean.builder()
                .expire(LocalDateTime.now().plusDays(5))
                .idUser(ub.getId())
                .issued(ldt)
                .process(EVerificationProcess.ONE_STEP)
                .username(ub.getUsername())
                .build();
        
        // Mocks...
        Mockito.when(userRepo.existsByUsername(ub.getUsername())).thenReturn(false);
        Mockito.when(userRepo.existsByPreferredChannel(ub.getPreferredChannel())).thenReturn(false);
        Mockito.when(userRepo.save(anyObject(UserBean.class, USERS[0]))).thenReturn(ub.toBuilder().build() , ub1);
        Mockito.when(userRepo.findById(vtk.getIdUser())).thenReturn(Optional.of(ub.toBuilder().build()));
        Mockito.when(userRepo.findByUsername(ub.getUsername())).thenReturn(Optional.of(ub1.toBuilder().build()));

        super.testAcceptance();
    }    
}
