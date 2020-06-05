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
package cat.albirar.users.test.verification;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.validation.ValidationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import cat.albirar.communications.models.ECommunicationChannelType;
import cat.albirar.users.models.tokens.AbstractTokenBean;
import cat.albirar.users.models.tokens.ApprobationTokenBean;
import cat.albirar.users.models.tokens.ApprobationTokenBean.ApprobationTokenBeanBuilder;
import cat.albirar.users.models.tokens.ETokenClass;
import cat.albirar.users.models.tokens.RecoverPasswordTokenBean;
import cat.albirar.users.models.tokens.RecoverPasswordTokenBean.RecoverPasswordTokenBeanBuilder;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.tokens.VerificationTokenBean.VerificationTokenBeanBuilder;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.services.TokenManager;
import cat.albirar.users.test.UsersRegisterTests;
import cat.albirar.users.verification.EVerificationProcess;
import cat.albirar.users.verification.ITokenManager;
import io.jsonwebtoken.JwtBuilder;

/**
 * Test for {@link TokenManager}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public class TokenManagerTest extends UsersRegisterTests {
    @Test
    public void testDecodeToken() {
        VerificationTokenBean vtk, eVtk;
        ApprobationTokenBean atk, eAtk;
        RecoverPasswordTokenBean rptk, eRptk;
        JwtBuilder jwtBuilder;
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken(VerificationTokenBean.class, null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken(VerificationTokenBean.class, ""));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken(VerificationTokenBean.class, "      "));

        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken(ApprobationTokenBean.class, null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken(ApprobationTokenBean.class, ""));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken(ApprobationTokenBean.class, "      "));

        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken(RecoverPasswordTokenBean.class, null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken(RecoverPasswordTokenBean.class, ""));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken(RecoverPasswordTokenBean.class, "      "));

        assertDecodeTokenIsFalse("XXX");
        // Builder
        jwtBuilder = createJwtBuilder();
        // Models
        eVtk = buildAbstractToken(VerificationTokenBean.builder(), SAMPLE_CREATED_USER)
                .process(EVerificationProcess.ONE_STEP)
                .build()
                ;
        eAtk = buildAbstractToken(ApprobationTokenBean.builder(), SAMPLE_VERIFIED_USER)
                .approverId(DUMMY_ID)
                .approverUsername(DUMMY_USERNAME_2)
                .build()
                ;
        eRptk = buildAbstractToken(RecoverPasswordTokenBean.builder(), SAMPLE_REGISTERED_USER)
                .origin(ECommunicationChannelType.EMAIL)
                .build()
                ;
        
        // Token id
        jwtBuilder.setId(null);
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setId("");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setId("     ");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setId(eVtk.getTokenId());
        assertDecodeTokenIsFalse(jwtBuilder.compact());

        // Issuer
        jwtBuilder.setIssuer(null);
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setIssuer("");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setIssuer("    ");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setIssuer((String)ReflectionTestUtils.getField(tokenManager, "issuer"));
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        
        // Issued at
        jwtBuilder.setIssuedAt(null);
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setIssuedAt(Date.from(LocalDateTime.now().plusDays(2).atZone(ZoneId.systemDefault()).toInstant()));
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setIssuedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setIssuedAt(Date.from(eVtk.getIssued().atZone(ZoneId.systemDefault()).toInstant()));
        assertDecodeTokenIsFalse(jwtBuilder.compact());

        // expiration
        jwtBuilder.setExpiration(null);
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setExpiration(Date.from(eVtk.getExpire().atZone(ZoneId.systemDefault()).toInstant()));
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        
        // Subject
        jwtBuilder.setSubject(null);
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setSubject("");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setSubject("  ");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setSubject("XXX");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setSubject(eVtk.getUsername());
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        
        // Claim user id
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, null);
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "    ");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, eVtk.getIdUser());
        assertDecodeTokenIsFalse(jwtBuilder.compact());

        // Token class
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, null);
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, "");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, "   ");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, "XXX");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        
        // Verification: Audience
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, ETokenClass.VERIFICATION.name());
        jwtBuilder.setAudience(null);
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setAudience("");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.setAudience("   ");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        
        // Verification: equals beans
        jwtBuilder.setAudience(EVerificationProcess.NONE.name());
        assertDecodeTokenIsFalse(jwtBuilder.compact());

        jwtBuilder.setAudience(eVtk.getProcess().name());
        assertDecodeTokenIsEquals(jwtBuilder.compact(), true, false, false);
        vtk = tokenManager.decodeToken(VerificationTokenBean.class, jwtBuilder.compact()).get();
        Assertions.assertEquals(eVtk, vtk);
        eVtk.setProcess(EVerificationProcess.TWO_STEP);
        jwtBuilder.setAudience(eVtk.getProcess().name());
        assertDecodeTokenIsEquals(jwtBuilder.compact(), true, false, false);
        vtk = tokenManager.decodeToken(VerificationTokenBean.class, jwtBuilder.compact()).get();
        Assertions.assertEquals(eVtk, vtk);

        // Approbation
        jwtBuilder.setSubject(eAtk.getUsername());
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, eAtk.getIdUser());
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, ETokenClass.APPROBATION.name());
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_APPROVER_ID, null);
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_APPROVER_ID, "");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_APPROVER_ID, "     ");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_APPROVER_ID, eAtk.getApproverId());
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        
        jwtBuilder.claim(ITokenManager.CLAIM_APPROVER_USERNAME, null);
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_APPROVER_USERNAME, "");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_APPROVER_USERNAME, "   ");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        
        jwtBuilder.claim(ITokenManager.CLAIM_APPROVER_USERNAME, eAtk.getApproverUsername());
        assertDecodeTokenIsEquals(jwtBuilder.compact(), false, true, false);
        
        // Approbation: equals beans
        atk = tokenManager.decodeToken(ApprobationTokenBean.class, jwtBuilder.compact()).get();
        Assertions.assertEquals(eAtk, atk);
        
        // Recover: channel
        jwtBuilder.setSubject(eRptk.getUsername());
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, eRptk.getIdUser());
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, ETokenClass.RECOVER_PASSWORD.name());
        jwtBuilder.claim(ITokenManager.CLAIM_ORIGIN_CHANNEL, null);
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_ORIGIN_CHANNEL, "");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_ORIGIN_CHANNEL, "  ");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        jwtBuilder.claim(ITokenManager.CLAIM_ORIGIN_CHANNEL, "XXX");
        assertDecodeTokenIsFalse(jwtBuilder.compact());
        
        jwtBuilder.claim(ITokenManager.CLAIM_ORIGIN_CHANNEL, eRptk.getOrigin().name());
        assertDecodeTokenIsEquals(jwtBuilder.compact(), false, false, true);
        
        // Recover: equals beans
        rptk = tokenManager.decodeToken(RecoverPasswordTokenBean.class, jwtBuilder.compact()).get();
        Assertions.assertEquals(eRptk, rptk);

        eRptk.setOrigin(ECommunicationChannelType.MOBILE);
        jwtBuilder.claim(ITokenManager.CLAIM_ORIGIN_CHANNEL, eRptk.getOrigin().name());
        assertDecodeTokenIsEquals(jwtBuilder.compact(), false, false, true);
        rptk = tokenManager.decodeToken(RecoverPasswordTokenBean.class, jwtBuilder.compact()).get();
        Assertions.assertEquals(eRptk, rptk);
        
    }

    @Test
    public void testEncodeVerificationToken() {
        @SuppressWarnings("rawtypes")
        VerificationTokenBeanBuilder tk;
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(null));
        
        tk = VerificationTokenBean.builder();
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.tokenId(UUID.randomUUID().toString());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.idUser(null);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.idUser("");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.idUser("   ");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.idUser(SAMPLE_ID);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.username(null);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.username("");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.username("  ");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.username(DUMMY_USERNAME);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));

        tk.issued(LocalDateTime.now());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.expire(LocalDateTime.now().plusDays(5).withNano(0));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));

        tk.process(EVerificationProcess.ONE_STEP);
        // Now should be OK
        tokenManager.encodeToken(tk.build());
    }
    
    @Test
    public void testEncodeApprobationToken() {
        @SuppressWarnings("rawtypes")
        ApprobationTokenBeanBuilder tk;
        
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(null));
        
        tk = ApprobationTokenBean.builder();
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.tokenId(UUID.randomUUID().toString());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.idUser(null);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.idUser("");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.idUser("   ");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.idUser(SAMPLE_ID);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.username(null);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.username("");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.username("  ");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.username(DUMMY_USERNAME);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.issued(LocalDateTime.now());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.expire(LocalDateTime.now().plusDays(5).withNano(0));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.approverId(null);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.approverId("");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.approverId("  ");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.approverId(DUMMY_ID);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        
        tk.approverUsername(null);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.approverUsername("");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.approverUsername("  ");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.approverUsername(DUMMY_USERNAME_2);
        // Now should be OK
        tokenManager.encodeToken(tk.build());
    }
    
    @Test
    public void testEncodeRecoverPasswordToken() {
        @SuppressWarnings("rawtypes")
        RecoverPasswordTokenBeanBuilder tk;
        
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(null));
        
        tk = RecoverPasswordTokenBean.builder();
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.tokenId(UUID.randomUUID().toString());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.idUser(null);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.idUser("");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.idUser("   ");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.idUser(SAMPLE_ID);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.username(null);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.username("");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.username("  ");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.username(DUMMY_USERNAME);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.issued(LocalDateTime.now());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.expire(LocalDateTime.now().plusDays(5).withNano(0));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        
        tk.origin(null);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(tk.build()));
        tk.origin(ECommunicationChannelType.EMAIL);
        // Now should be OK
        tokenManager.encodeToken(tk.build());
        tk.origin(ECommunicationChannelType.MOBILE);
        // Now should be OK
        tokenManager.encodeToken(tk.build());
    }
    
    @Test
    public void testEncodeDerivedTokenBean() {
        DerivedTokenBeanForTest dtk;
        
        dtk = new DerivedTokenBeanForTest(buildAbstractToken(VerificationTokenBean.builder(), SAMPLE_CREATED_USER));
        Assertions.assertThrows(IllegalArgumentException.class, () -> tokenManager.encodeToken(dtk));
    }
    
    @Test
    public void testGenerateVerificationToken() {
        VerificationTokenBean token;

        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(null, null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(null, EVerificationProcess.ONE_STEP));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(null, EVerificationProcess.TWO_STEP));
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(UserBean.builder().build(), EVerificationProcess.ONE_STEP));
        // No verification token is generated for NONE verification process
        Assertions.assertFalse(tokenManager.generateVerificationTokenBean(SAMPLE_NEW_USER.toBuilder().build(), EVerificationProcess.NONE).isPresent());

        token = tokenManager.generateVerificationTokenBean(SAMPLE_CREATED_USER.toBuilder().build(), EVerificationProcess.ONE_STEP).get();
        Assertions.assertNotNull(token);
        
        assertBasicInfoTokenBean(token, SAMPLE_CREATED_USER.getId(), SAMPLE_CREATED_USER.getUsername());

        Assertions.assertEquals(EVerificationProcess.ONE_STEP, token.getProcess());
    }
    
    @Test
    public void testGenerateApprobationTokenBean() {
        ApprobationTokenBean token;
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateApprobationTokenBean(null, null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateApprobationTokenBean(SAMPLE_VERIFIED_USER, null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateApprobationTokenBean(null, SAMPLE_REGISTERED_USER));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateApprobationTokenBean(UserBean.builder().build(), SAMPLE_REGISTERED_USER));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateApprobationTokenBean(SAMPLE_VERIFIED_USER, UserBean.builder().build()));
        
        Assertions.assertFalse(tokenManager.generateApprobationTokenBean(SAMPLE_VERIFIED_USER.toBuilder().id(null).build(), SAMPLE_REGISTERED_USER).isPresent());
        Assertions.assertFalse(tokenManager.generateApprobationTokenBean(SAMPLE_VERIFIED_USER, SAMPLE_REGISTERED_USER.toBuilder().id(null).build()).isPresent());
        Assertions.assertFalse(tokenManager.generateApprobationTokenBean(SAMPLE_VERIFIED_USER.toBuilder().id(null).build(), SAMPLE_REGISTERED_USER.toBuilder().id(null).build()).isPresent());
        
        token = tokenManager.generateApprobationTokenBean(SAMPLE_VERIFIED_USER, SAMPLE_REGISTERED_USER).get();
        Assertions.assertNotNull(token);
        
        assertBasicInfoTokenBean(token, SAMPLE_VERIFIED_USER.getId(), SAMPLE_VERIFIED_USER.getUsername());

        Assertions.assertNotNull(token.getApproverId());
        Assertions.assertEquals(SAMPLE_REGISTERED_USER.getId(), token.getApproverId());
        Assertions.assertNotNull(token.getApproverUsername());
        Assertions.assertEquals(SAMPLE_REGISTERED_USER.getUsername(), token.getApproverUsername());
    }
    
    @Test
    public void testGenerateRecoverPasswordTokenBean() {
        RecoverPasswordTokenBean token;
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateRecoverPasswordTokenBean(null, true));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateRecoverPasswordTokenBean(UserBean.builder().build(), true));
        
        Assertions.assertFalse(tokenManager.generateRecoverPasswordTokenBean(SAMPLE_REGISTERED_USER.toBuilder().id(null).build(), true).isPresent());
        
        // A user with two channels: preferred
        token = tokenManager.generateRecoverPasswordTokenBean(SAMPLE_REGISTERED_USER, true).get();
        Assertions.assertNotNull(token);
        
        assertBasicInfoTokenBean(token, SAMPLE_REGISTERED_USER.getId(), SAMPLE_REGISTERED_USER.getUsername());
        
        Assertions.assertEquals(SAMPLE_REGISTERED_USER.getPreferredChannel().getChannelType(), token.getOrigin());

        // A user with two channels: secondary
        token = tokenManager.generateRecoverPasswordTokenBean(SAMPLE_REGISTERED_USER, false).get();
        Assertions.assertNotNull(token);
        
        assertBasicInfoTokenBean(token, SAMPLE_REGISTERED_USER.getId(), SAMPLE_REGISTERED_USER.getUsername());
        
        Assertions.assertEquals(SAMPLE_REGISTERED_USER.getSecondaryChannel().getChannelType(), token.getOrigin());

        // A user with only one channel: preferred
        token = tokenManager.generateRecoverPasswordTokenBean(SAMPLE_VERIFIED_USER, true).get();
        Assertions.assertNotNull(token);
        
        assertBasicInfoTokenBean(token, SAMPLE_VERIFIED_USER.getId(), SAMPLE_VERIFIED_USER.getUsername());
        
        Assertions.assertEquals(SAMPLE_VERIFIED_USER.getPreferredChannel().getChannelType(), token.getOrigin());

        // A user with only one channel: secondary, but use primary because leak of primary
        token = tokenManager.generateRecoverPasswordTokenBean(SAMPLE_VERIFIED_USER, false).get();
        Assertions.assertNotNull(token);
        
        assertBasicInfoTokenBean(token, SAMPLE_VERIFIED_USER.getId(), SAMPLE_VERIFIED_USER.getUsername());
        
        Assertions.assertEquals(SAMPLE_VERIFIED_USER.getPreferredChannel().getChannelType(), token.getOrigin());
    }
    
    @Test
    public void testIsTokenValid() {
        JwtBuilder jwtBuilder;

        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenValid(null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenValid(""));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenValid("      "));
        
        Assertions.assertFalse(tokenManager.isTokenValid("XXXX"));
        
        // Builder
        jwtBuilder = createJwtBuilder();
        
        // Token id
        jwtBuilder.setId(null);
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setId("");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setId("     ");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setId(UUID.randomUUID().toString());
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        
        // Issuer
        jwtBuilder.setIssuer(null);
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuer("");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuer("    ");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuer("XXX");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuer((String)ReflectionTestUtils.getField(tokenManager, "issuer"));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        
        // Issued at
        jwtBuilder.setIssuedAt(null);
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuedAt(Date.from(LocalDateTime.now().plusDays(2).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuedAt(Date.from(LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        
        // expiration
        jwtBuilder.setExpiration(null);
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().plusDays(5).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));

        // Subject
        jwtBuilder.setSubject(null);
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setSubject("");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setSubject("  ");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setSubject("XXX");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        
        // Claim user id
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, null);
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "    ");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, SAMPLE_ID);
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));

        // Token class
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, null);
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, "");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, "   ");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, "XXX");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, ETokenClass.VERIFICATION.name());
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));        
        
        // Audience
        jwtBuilder.setAudience("XXX");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setAudience(EVerificationProcess.ONE_STEP.name());
        Assertions.assertTrue(tokenManager.isTokenValid(jwtBuilder.compact()));
    }
    
    @Test
    public void testIsTokenClass() {
        JwtBuilder jwtBuilder;

        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenClass(null, null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenClass("", null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenClass("      ", null));
        
        Assertions.assertFalse(tokenManager.isTokenClass("XXX", ETokenClass.VERIFICATION));
        Assertions.assertFalse(tokenManager.isTokenClass("XXX", ETokenClass.APPROBATION));
        Assertions.assertFalse(tokenManager.isTokenClass("XXX", ETokenClass.RECOVER_PASSWORD));
        
        jwtBuilder = createJwtBuilder()
                .setId(UUID.randomUUID().toString())
                .setIssuer((String)ReflectionTestUtils.getField(tokenManager, "issuer"))
                .setSubject("XXX")
                .setIssuedAt(Date.from(LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusDays(5).atZone(ZoneId.systemDefault()).toInstant()))
                .claim(ITokenManager.CLAIM_USERID, SAMPLE_ID)
                .setAudience(EVerificationProcess.ONE_STEP.name())
                .claim(ITokenManager.CLAIM_TOKEN_CLASS, null)
                ;

        Assertions.assertFalse(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.VERIFICATION));

        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, "");
        Assertions.assertFalse(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.VERIFICATION));
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, "   ");
        Assertions.assertFalse(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.VERIFICATION));
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, "XXX");
        Assertions.assertFalse(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.VERIFICATION));
        
        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, ETokenClass.VERIFICATION.name());
        Assertions.assertTrue(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.VERIFICATION));
        Assertions.assertFalse(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.APPROBATION));
        Assertions.assertFalse(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.RECOVER_PASSWORD));

        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, ETokenClass.APPROBATION.name());
        Assertions.assertFalse(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.VERIFICATION));
        Assertions.assertTrue(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.APPROBATION));
        Assertions.assertFalse(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.RECOVER_PASSWORD));

        jwtBuilder.claim(ITokenManager.CLAIM_TOKEN_CLASS, ETokenClass.RECOVER_PASSWORD.name());
        Assertions.assertFalse(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.VERIFICATION));
        Assertions.assertFalse(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.APPROBATION));
        Assertions.assertTrue(tokenManager.isTokenClass(jwtBuilder.compact(), ETokenClass.RECOVER_PASSWORD));

    }

    private void assertBasicInfoTokenBean(AbstractTokenBean token, String idUser, String username) {
        LocalDateTime ldt;
        
        Assertions.assertTrue(StringUtils.hasText(token.getTokenId()));
        ldt = LocalDateTime.now();
        Assertions.assertTrue(ldt.plusSeconds(1).isAfter(token.getIssued()));
        Assertions.assertTrue(ldt.minusSeconds(2).isBefore(token.getIssued()));
        
        ldt = ldt.plusDays((int)ReflectionTestUtils.getField(tokenManager, "daysToExpire"));
        Assertions.assertTrue(ldt.plusSeconds(1).isAfter(token.getExpire()));
        Assertions.assertTrue(ldt.minusSeconds(2).isBefore(token.getExpire()));
        
        Assertions.assertNotNull(token.getIdUser());
        Assertions.assertEquals(idUser, token.getIdUser());
        Assertions.assertNotNull(token.getUsername());
        Assertions.assertEquals(username, token.getUsername());
    }

    private void assertDecodeTokenIsFalse(String token) {
        Optional<VerificationTokenBean> rv;
        Optional<ApprobationTokenBean> ra;
        Optional<RecoverPasswordTokenBean> rr;
        
        rv = tokenManager.decodeToken(VerificationTokenBean.class, token);
        Assertions.assertFalse(rv.isPresent());
        ra = tokenManager.decodeToken(ApprobationTokenBean.class, token);
        Assertions.assertFalse(ra.isPresent());
        rr = tokenManager.decodeToken(RecoverPasswordTokenBean.class, token);
        Assertions.assertFalse(rr.isPresent());
    }

    private void assertDecodeTokenIsEquals(String token, boolean v, boolean a, boolean r) {
        Optional<VerificationTokenBean> rv;
        Optional<ApprobationTokenBean> ra;
        Optional<RecoverPasswordTokenBean> rr;
        
        rv = tokenManager.decodeToken(VerificationTokenBean.class, token);
        Assertions.assertEquals(v, rv.isPresent());
        ra = tokenManager.decodeToken(ApprobationTokenBean.class, token);
        Assertions.assertEquals(a, ra.isPresent());
        rr = tokenManager.decodeToken(RecoverPasswordTokenBean.class, token);
        Assertions.assertEquals(r, rr.isPresent());
    }
}
