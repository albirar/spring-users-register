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
package cat.albirar.users.test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import cat.albirar.users.models.account.AccountBean;
import cat.albirar.users.models.auth.AuthorizationBean;
import cat.albirar.users.models.communications.CommunicationChannel;
import cat.albirar.users.models.communications.ECommunicationChannelType;
import cat.albirar.users.models.users.UserBean;

/**
 * A root and abstract data class for any test.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class UsersRegisterAbstractDataTest {

    public static final String SAMPLE_ID = "000000000000000000000001";

    public static final String DUMMY_TOKEN = "XXX";
    public static final String DUMMY_USERNAME = "usernameXXX";
    public static final String DUMMY_USERNAME_1 = "usernameYYY";
    public static final String DUMMY_USERNAME_2 = "usernameZZZ";
    public static final String DUMMY_NAME = "XXX";
    public static final String DUMMY_NAME_1 = "YYYY";
    public static final String DUMMY_NAME_2 = "ZZZZ";
    public static final String DUMMY_ID = "000000000000000000999999";
    public static final String DUMMY_ROLE = "roleXXX";
    
    public static final AccountBean [] ACCOUNTS = {
            AccountBean.builder()
                .id("000000000000000000000001")
                .name("Prova")
                .enabled(true)
                .build()
            ,AccountBean.builder()
                .id("000000000000000000000002")
                .name("Prova2")
                .enabled(true)
                .build()
            ,AccountBean.builder()
                .id("000000000000000000000003")
                .name("Prova3")
                .enabled(true)
                .build()
    };
    
    public static final String [] PASSWORDS = {"user1pwd"
            , "user2pwd!"
            , "[7v,;Cb?kM66nqP("
    };
    
    public static final String [] ENCRYPTED_PASSWORDS = {
            "$2y$12$focnwI3Z4HFEgAMB.bZuQe8y3N3JhtoUv0L8qg/L6yljQr.ydrkJu"
            , "$2y$12$C2K1FivCtjTxvcxsrp5RLeD42oym8NZYAP2PbIO968ivTl7tL2wBa"
            , "$2y$12$l6EotzO1duo2FPy7s8KF5.vzvdbl6wwlroJ0xX1Eo/.2/0Mq16y7a"
    };
//    , "$2y$12$YziFtZGfhbR7XPOHgxL0h.5yQuG/aQZ7O/xM3WfqYt8RrH2O2mwvm"
    
    public static final CommunicationChannel [] CHANNELS = {
            CommunicationChannel.builder().channelType(ECommunicationChannelType.MOBILE).channelId("698765432").build()
            ,CommunicationChannel.builder().channelType(ECommunicationChannelType.EMAIL).channelId("user1@test.com").build()
            ,CommunicationChannel.builder().channelType(ECommunicationChannelType.EMAIL).channelId("user2@test.com").build()
            ,CommunicationChannel.builder().channelType(ECommunicationChannelType.MOBILE).channelId("612345678").build()
            ,CommunicationChannel.builder().channelType(ECommunicationChannelType.MOBILE).channelId("635255869").build()
            ,CommunicationChannel.builder().channelType(ECommunicationChannelType.MOBILE).channelId("612744895").build()
            ,CommunicationChannel.builder().channelType(ECommunicationChannelType.MOBILE).channelId("664258359").build()
    };
    public static final CommunicationChannel SAMPLE_NEW_CHANNEL = CommunicationChannel.builder().channelType(ECommunicationChannelType.MOBILE).channelId("654654654").build();
    
    public static final UserBean SAMPLE_NEW_USER = UserBean.builder()
            .username("newUser")
            .password(ENCRYPTED_PASSWORDS[0])
            .expire(LocalDate.of(2032, 1, 1))
            .enabled(false)
            .preferredChannel(CHANNELS[0])
            .build()
            ;
    public static final UserBean SAMPLE_NEW_USER_CREATED = SAMPLE_NEW_USER.toBuilder()
            .id(DUMMY_ID)
            .created(LocalDateTime.now().withNano(0))
            .build()
            ;
    public static final UserBean SAMPLE_CREATED_USER = UserBean.builder()
            .id("000000000000000000000006")
            .username("createdUser")
            .password(ENCRYPTED_PASSWORDS[0])
            .created(LocalDateTime.now().minusDays(5).withNano(0))
            .enabled(false)
            .expire(LocalDate.of(2032, 1, 1))
            .preferredChannel(CHANNELS[1])
            .authorities(Arrays.asList(AuthorizationBean.builder()
                    .authority("User")
                    .build()))
            .build()
            ;
    public static final UserBean SAMPLE_VERIFIED_USER = UserBean.builder()
            .id("000000000000000000000007")
            .username("verifiedUser")
            .password(ENCRYPTED_PASSWORDS[0])
            .created(LocalDateTime.now().minusDays(5).withNano(0))
            .expire(LocalDate.of(2032, 1, 1))
            .verified(LocalDateTime.now().minusDays(4).withNano(0))
            .enabled(false)
            .preferredChannel(CHANNELS[4])
            .authorities(Arrays.asList(AuthorizationBean.builder()
                    .authority("AccountAdministrator")
                    .build()
                    ,AuthorizationBean.builder()
                    .authority("User")
                    .build()))
            .build()
            ;
    public static final UserBean SAMPLE_REGISTERED_USER = UserBean.builder()
            .id("000000000000000000000008")
            .username("registeredUser")
            .password(ENCRYPTED_PASSWORDS[0])
            .created(LocalDateTime.now().minusDays(5).withNano(0))
            .verified(LocalDateTime.now().minusDays(4).withNano(0))
            .registered(LocalDateTime.now().minusDays(3).withNano(0))
            .expire(LocalDate.of(2032, 1, 1))
            .enabled(true)
            .preferredChannel(CHANNELS[2])
            .secondaryChannel(CHANNELS[3])
            .authorities(Arrays.asList(AuthorizationBean.builder()
                    .authority("AccountAdministrator")
                    .build()
                    ,AuthorizationBean.builder()
                    .authority("User")
                    .build()))
            .build()
            ;
    public static final UserBean SAMPLE_REGISTERED_USER_NO_AUTHORITIES = UserBean.builder()
            .id("000000000000000000000009")
            .username("noAuthUser")
            .password(ENCRYPTED_PASSWORDS[0])
            .created(LocalDateTime.now().minusDays(5).withNano(0))
            .verified(LocalDateTime.now().minusDays(4).withNano(0))
            .registered(LocalDateTime.now().minusDays(3).withNano(0))
            .expire(LocalDate.of(2032, 1, 1))
            .enabled(true)
            .preferredChannel(CHANNELS[5])
            .build()
            ;
    
    public static final UserBean [] USERS = {
            SAMPLE_CREATED_USER, SAMPLE_VERIFIED_USER, SAMPLE_REGISTERED_USER, SAMPLE_REGISTERED_USER_NO_AUTHORITIES
    };
}
