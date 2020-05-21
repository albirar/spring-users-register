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
import cat.albirar.users.test.services.RegistrationServiceNoneTest;
import cat.albirar.users.verification.EVerificationProcess;

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
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void testRegistration() {
        LocalDateTime ldt;
        UserBean ub;

        // Mocks...
        ldt = LocalDateTime.now();
        ub = SAMPLE_NEW_USER.toBuilder()
                .created(ldt)
                .registered(ldt)
                .verified(ldt)
                .id(SAMPLE_ID)
                .enabled(true)
                .authorities(Arrays.asList(new AuthorizationBean [] {AuthorizationBean.builder().authority(ERole.User.name()).build()}))
                .build()
                ;
        Mockito.when(userRepo.existsByUsername(ub.getUsername())).thenReturn(false);
        Mockito.when(userRepo.existsByPreferredChannel(ub.getPreferredChannel())).thenReturn(false);
        Mockito.when(userRepo.save(anyObject(UserBean.class, USERS[0]))).thenReturn(ub.toBuilder().build());
        
        super.testRegistration();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void testAcceptance() {
        VerificationTokenBean vtk;
        LocalDateTime ldt;
        UserBean ub;

        // Mocks...
        ldt = LocalDateTime.now();
        ub = SAMPLE_NEW_USER.toBuilder()
                .created(ldt)
                .registered(ldt)
                .verified(ldt)
                .id(SAMPLE_ID)
                .enabled(true)
                .authorities(Arrays.asList(new AuthorizationBean [] {AuthorizationBean.builder().authority(ERole.User.name()).build()}))
                .build()
                ;
        vtk = VerificationTokenBean.builder()
                .expire(LocalDateTime.now().plusDays(5))
                .idUser(ub.getId())
                .issued(LocalDateTime.now())
                .process(EVerificationProcess.NONE)
                .username(ub.getUsername())
                .build();
        Mockito.when(userRepo.existsByUsername(ub.getUsername())).thenReturn(false);
        Mockito.when(userRepo.existsByPreferredChannel(ub.getPreferredChannel())).thenReturn(false);
        Mockito.when(userRepo.save(anyObject(UserBean.class, USERS[0]))).thenReturn(ub.toBuilder().build());
        
        Mockito.when(userRepo.findById(vtk.getIdUser())).thenReturn(Optional.of(ub.toBuilder().build()));
        
        super.testAcceptance();
    }
}
