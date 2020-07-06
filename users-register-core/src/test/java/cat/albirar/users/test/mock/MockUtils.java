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
package cat.albirar.users.test.mock;

import static cat.albirar.users.test.mock.ValidMatchers.anyObject;
import static cat.albirar.users.test.mock.ValidMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import cat.albirar.communications.channels.models.CommunicationChannelBean;
import cat.albirar.users.models.account.AccountBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.repos.IAccountRepo;
import cat.albirar.users.repos.IUserRepo;
import cat.albirar.users.test.UsersRegisterAbstractDataTest;

/**
 * Utilities for testing purposes.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public class MockUtils extends UsersRegisterAbstractDataTest {
    private UserBean [] REAL_USERS;
    private AccountBean [] REAL_ACCOUNTS;
    
    public static MockUtils instance() {
        return new MockUtils(); 
    }
    private MockUtils() {
        REAL_USERS = new UserBean[USERS.length];
        for(int n = 0; n < REAL_USERS.length; n++) {
            REAL_USERS[n] = USERS[n].toBuilder().build();
        }

        // Populate real accounts
        REAL_ACCOUNTS = new AccountBean [ACCOUNTS.length];
        
        for(int n = 0; n < REAL_ACCOUNTS.length; n++) {
            REAL_ACCOUNTS[n] = ACCOUNTS[n].toBuilder().build();
        }
    }
    /**
     * Setup mocked users on the indicated mocked {@code userRepo}.
     * Setup as:
     * <ul>
     * <li>{@link IUserRepo#existsById(String)} with {@link UsersRegisterAbstractDataTest#DUMMY_USERNAME} return false</li>
     * <li>{@link IUserRepo#findById(String)} with {@link UsersRegisterAbstractDataTest#SAMPLE_ID} return {@link Optional#empty()}</li>
     * <li>For each {@code user} in {@link UsersRegisterAbstractDataTest#USERS} do:
     *    <ul>
     *       <li>{@link IUserRepo#findById(String)} with {@code user.getId()} return {@link Optional#of(Object) Optional.of(user)}</li>
     *       <li>{@link IUserRepo#existsByUsername(String)} with {@code user.getUsername()} return true</li>
     *       <li>{@link IUserRepo#existsByPreferredChannel(cat.albirar.communications.channels.models.CommunicationChannelBean)} with {@code user.getPreferredChannel()} return true</li>
     *    </ul>
     * </li>
     * </ul>
     * @param userRepo The mocked repository
     * @return This instance
     */
    public MockUtils setupRegisteredUsers(IUserRepo userRepo) {
        when(userRepo.findAll()).thenReturn(Arrays.asList(REAL_USERS));
        when(userRepo.existsById(anyString(DUMMY_ID))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                String id = invocation.getArgument(0);
                return Stream.of(REAL_USERS).anyMatch(u -> u.getId().equals(id));
            }
        });
        when(userRepo.findById(anyString(DUMMY_ID))).thenAnswer(new Answer<Optional<UserBean>>() {
            @Override
            public Optional<UserBean> answer(InvocationOnMock invocation) throws Throwable {
                String id = invocation.getArgument(0);
                return Stream.of(REAL_USERS).filter(u -> u.getId().equals(id)).findFirst();
            }
        });
        when(userRepo.existsByUsername(anyString(DUMMY_USERNAME))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                String name = invocation.getArgument(0);
                return Stream.of(REAL_USERS).anyMatch(u -> u.getUsername().equals(name));
            }
        });
        when(userRepo.findByUsername(anyString(DUMMY_USERNAME))).thenAnswer(new Answer<Optional<UserBean>>() {
            @Override
            public Optional<UserBean> answer(InvocationOnMock invocation) throws Throwable {
                String username = invocation.getArgument(0);
                return Stream.of(REAL_USERS).filter(u -> u.getUsername().equals(username)).findFirst();
            }
        });
        when(userRepo.existsByPreferredChannel(anyObject(CommunicationChannelBean.class, CHANNELS[0]))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CommunicationChannelBean commChannel = invocation.getArgument(0);
                return Stream.of(REAL_USERS).anyMatch(u -> u.getPreferredChannel().equals(commChannel));
            }
        });
        when(userRepo.existsBySecondaryChannel(anyObject(CommunicationChannelBean.class, CHANNELS[0]))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CommunicationChannelBean commChannel = invocation.getArgument(0);
                return Stream.of(REAL_USERS).anyMatch(u -> ObjectUtils.nullSafeEquals(u.getSecondaryChannel(), commChannel));
            }
        });
        when(userRepo.count()).thenReturn((long)REAL_USERS.length);
        
        when(userRepo.save(anyObject(UserBean.class, USERS[0]))).thenAnswer(new Answer<UserBean>() {
            @Override
            public UserBean answer(InvocationOnMock invocation) throws Throwable {
                UserBean newUser = invocation.getArgument(0);
                UserBean savedUser;
                
                if(StringUtils.hasText(newUser.getId())
                        && Stream.of(REAL_USERS).anyMatch(u -> u.getId().equals(newUser.getId()))) {
                    // UPDATE
                    // Constraints
                    if(Stream.of(REAL_USERS).anyMatch(u -> !u.getId().equals(newUser.getId()) 
                            && (u.getUsername().equals(newUser.getUsername())
                                    || u.getPreferredChannel().equals(newUser.getPreferredChannel())))) {
                        throw new DataIntegrityViolationException(String.format("Another user with the username %s or preferred channel %s is found, cannot be updated!", newUser.getUsername(), newUser.getPreferredChannel()));
                    }
                    savedUser = newUser.toBuilder().build();
                    for(int n = 0; n < REAL_USERS.length; n++) {
                        if(REAL_USERS[n].getId().equals(newUser.getId())) {
                            REAL_USERS[n] = savedUser;
                        }
                    }
                } else {
                    // CREATE
                    // Constraints
                    if(Stream.of(REAL_USERS).anyMatch(u -> u.getUsername().equals(newUser.getUsername())
                                    || u.getPreferredChannel().equals(newUser.getPreferredChannel()))) {
                        throw new DataIntegrityViolationException(String.format("Another user with the username %s or preferred channel %s is found, cannot be updated!", newUser.getUsername(), newUser.getPreferredChannel()));
                    }
                    if(!StringUtils.hasText(newUser.getId())) {
                        savedUser = newUser.toBuilder().id(UUID.randomUUID().toString()).build();
                    } else {
                        savedUser = newUser.toBuilder().build();
                    }
                    
                    REAL_USERS = Arrays.copyOf(REAL_USERS, REAL_USERS.length + 1);
                    REAL_USERS[REAL_USERS.length - 1] = savedUser;
                }
                return savedUser;
            }
        });
        
        return this;
    }
    public MockUtils setupAccounts(IAccountRepo accountRepo) {
        
        when(accountRepo.findAll()).thenReturn(Arrays.asList(REAL_ACCOUNTS));
        when(accountRepo.findByName(anyString("XX"))).thenAnswer(new Answer<Optional<AccountBean>>() {
            @Override
            public Optional<AccountBean> answer(InvocationOnMock invocation) throws Throwable {
                String name = invocation.getArgument(0);
                return Stream.of(REAL_ACCOUNTS).filter(ac -> ac.getName().equals(name)).findFirst();
            }
        });
        when(accountRepo.findById(anyString(DUMMY_ID))).thenAnswer(new Answer<Optional<AccountBean>>() {
            @Override
            public Optional<AccountBean> answer(InvocationOnMock invocation) throws Throwable {
                String id = invocation.getArgument(0);
                return Stream.of(REAL_ACCOUNTS).filter(ac -> ac.getId().equals(id)).findFirst();
            }
        });
        when(accountRepo.count()).thenReturn((long)REAL_ACCOUNTS.length);
        when(accountRepo.save(anyObject(AccountBean.class, REAL_ACCOUNTS[0]))).thenAnswer(new Answer<AccountBean>() {
            @Override
            public AccountBean answer(InvocationOnMock invocation) throws Throwable {
                AccountBean newAccount = invocation.getArgument(0);
                AccountBean savedAccount;
                
                if(StringUtils.hasText(newAccount.getId())
                        && Stream.of(REAL_ACCOUNTS).anyMatch(p -> p.getId().equals(newAccount.getId()))) {
                    // UPDATE
                    if(Stream.of(REAL_ACCOUNTS).anyMatch(p -> p.getName().equals(newAccount.getName()) && !p.getId().equals(newAccount.getId()))) {
                        throw new DataIntegrityViolationException(String.format("Another account with the name %s is found, cannot be updated!", newAccount.getName()));
                    }
                    if(Stream.of(REAL_ACCOUNTS).anyMatch(p -> p.equals(newAccount))) {
                        return newAccount; // No changes was made
                    }
                    savedAccount = newAccount.toBuilder().build();
                    for(int n = 0; n < REAL_ACCOUNTS.length; n++) {
                        if(REAL_ACCOUNTS[n].getId().equals(newAccount.getId())) {
                            REAL_ACCOUNTS[n] = savedAccount;
                        }
                    }
                } else {
                    // CREATE
                    if(Stream.of(REAL_ACCOUNTS).noneMatch(p -> p.getName().equals(newAccount.getName()))) {
                        if(!StringUtils.hasText(newAccount.getId())) {
                            savedAccount = newAccount.toBuilder().id(UUID.randomUUID().toString()).build();
                        } else {
                            savedAccount = newAccount.toBuilder().build();
                        }
                        REAL_ACCOUNTS = Arrays.copyOf(REAL_ACCOUNTS, REAL_ACCOUNTS.length + 1);
                        REAL_ACCOUNTS[REAL_ACCOUNTS.length - 1] = savedAccount;
                    } else {
                        throw new DataIntegrityViolationException(String.format("The name %s is on the account's registry", newAccount.getName()));
                    }
                }
                return savedAccount;
            }
        });
        return this;
    }
}
