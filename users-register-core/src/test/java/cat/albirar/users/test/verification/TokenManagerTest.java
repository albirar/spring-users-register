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

import javax.crypto.SecretKey;
import javax.validation.ValidationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import cat.albirar.users.config.UsersRegisterConfiguration;
import cat.albirar.users.models.communications.CommunicationChannel;
import cat.albirar.users.models.communications.ECommunicationChannelType;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.tokens.VerificationTokenBean.VerificationTokenBeanBuilder;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.models.users.UserBean.UserBeanBuilder;
import cat.albirar.users.test.UsersRegisterTests;
import cat.albirar.users.test.context.DefaultContextTestConfiguration;
import cat.albirar.users.verification.EVerificationProcess;
import cat.albirar.users.verification.ITokenManager;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

/**
 * Test for {@link TokenManager}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@SpringJUnitWebConfig(classes = {UsersRegisterConfiguration.class, DefaultContextTestConfiguration.class})
@DirtiesContext
public class TokenManagerTest extends UsersRegisterTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenManagerTest.class); 
    @Autowired
    private ITokenManager tokenManager;
    
    @BeforeEach
    public void setup() {
        LOGGER.debug("TokenManagerTest begins...");
    }
    
    public void teardown() {
        LOGGER.debug("TokenManagerTest ENDS");
    }
    
    @Test
    public void testDecodeToken() {
        Optional<VerificationTokenBean> r;
        SecretKey jwsSecretKey;
        JwtBuilder jwtBuilder;
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken(null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken(""));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeToken("      "));
        
        r = tokenManager.decodeToken("XXXX");
        Assertions.assertFalse(r.isPresent());
        
        jwsSecretKey = (SecretKey) ReflectionTestUtils.getField(tokenManager, "jwsSecretKey");
        
        // Builder
        jwtBuilder = Jwts.builder()
                .setIssuer(null)
                .setId("XXX")
                .setSubject("")
                .setAudience("XX")
                .setIssuedAt(Date.from(LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusDays(5).atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(jwsSecretKey)
                ;
        
        // Issuer
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setIssuer("");
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setIssuer("    ");
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setIssuer((String)ReflectionTestUtils.getField(tokenManager, "issuer"));
        
        // Token id
        jwtBuilder.setId(null);
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setId("");
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setId("     ");
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setId(UUID.randomUUID().toString());
        
        // Claim user id
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, null);
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "");
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "    ");
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, SAMPLE_ID);
    
        // expiration
        jwtBuilder.setExpiration(null);
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().plusDays(5).atZone(ZoneId.systemDefault()).toInstant()));
        
        // Subject
        jwtBuilder.setSubject(null);
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setSubject("");
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setSubject("  ");
        r = tokenManager.decodeToken(jwtBuilder.compact());
        jwtBuilder.setSubject("XXX");
        
        // Audience
        jwtBuilder.setAudience("XXX");
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        
        jwtBuilder.setAudience(EVerificationProcess.NONE.name());
        r = tokenManager.decodeToken(jwtBuilder.compact());
        Assertions.assertTrue(r.isPresent());
    }
    
    @Test
    public void testDecodeTokenEvenExpired() {
        Optional<VerificationTokenBean> r;
        SecretKey jwsSecretKey;
        JwtBuilder jwtBuilder;
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeTokenEvenExpired(null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeTokenEvenExpired(""));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.decodeTokenEvenExpired("      "));
        
        r = tokenManager.decodeTokenEvenExpired("XXXX");
        Assertions.assertFalse(r.isPresent());
        
        jwsSecretKey = (SecretKey) ReflectionTestUtils.getField(tokenManager, "jwsSecretKey");
        
        // Builder
        jwtBuilder = Jwts.builder()
                .setIssuer(null)
                .setId("XXX")
                .setSubject("")
                .setAudience("XX")
                .setIssuedAt(Date.from(LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(LocalDateTime.now().plusDays(5).atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(jwsSecretKey)
                ;
        
        // Issuer
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setIssuer("");
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setIssuer("    ");
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setIssuer((String)ReflectionTestUtils.getField(tokenManager, "issuer"));
        
        // Token id
        jwtBuilder.setId(null);
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setId("");
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setId("     ");
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setId(UUID.randomUUID().toString());
        
        // Claim user id
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, null);
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "");
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "    ");
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, SAMPLE_ID);
    
        // expiration
        jwtBuilder.setExpiration(null);
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().plusDays(5).atZone(ZoneId.systemDefault()).toInstant()));
        
        // Subject
        jwtBuilder.setSubject(null);
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setSubject("");
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        jwtBuilder.setSubject("  ");
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        jwtBuilder.setSubject("XXX");
        
        // Audience
        jwtBuilder.setAudience("XXX");
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertFalse(r.isPresent());
        
        jwtBuilder.setAudience(EVerificationProcess.NONE.name());
        r = tokenManager.decodeTokenEvenExpired(jwtBuilder.compact());
        Assertions.assertTrue(r.isPresent());
    }

    @Test
    public void testEncodeToken() {
        @SuppressWarnings("rawtypes")
        VerificationTokenBeanBuilder vtk;
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(null));
        
        vtk = VerificationTokenBean.builder();
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));
        
        vtk.tokenId(UUID.randomUUID().toString());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));
        
        vtk.idUser(null);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));
        vtk.idUser("");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));
        vtk.idUser("   ");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));
        vtk.idUser(SAMPLE_ID);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));
        
        vtk.username(null);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));
        vtk.username("");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));
        vtk.username("  ");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));
        vtk.username(DUMMY_USERNAME);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));
        
        vtk.process(EVerificationProcess.NONE);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));

        vtk.issued(LocalDateTime.now());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.encodeToken(vtk.build()));
        
        vtk.expire(LocalDateTime.now().plusDays(5));
        // Now should be OK
        tokenManager.encodeToken(vtk.build());
    }
    
    @Test
    public void testGenerateToken() {
        @SuppressWarnings("rawtypes")
        UserBeanBuilder builder;
        VerificationTokenBean token;
        LocalDateTime ldt;
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(null, null));
        
        builder = UserBean.builder();

        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(builder.build(), null));
        
        builder.id(SAMPLE_ID);
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(builder.build(), null));
        
        builder.username("XXXX");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(builder.build(), null));
        
        builder.preferredChannel(CommunicationChannel.builder().build());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(builder.build(), null));
        builder.preferredChannel(CommunicationChannel.builder().channelType(ECommunicationChannelType.EMAIL).build());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(builder.build(), null));
        builder.preferredChannel(CommunicationChannel.builder().channelType(ECommunicationChannelType.EMAIL).channelId("x@z.com").build());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(builder.build(), null));
        
        builder.password("XXXX");
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(builder.build(), null));
        
        builder.created(LocalDateTime.now());
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.generateVerificationTokenBean(builder.build(), null));
        
        token = tokenManager.generateVerificationTokenBean(builder.build(), EVerificationProcess.NONE).get();
        Assertions.assertNotNull(token);
        Assertions.assertTrue(StringUtils.hasText(token.getTokenId()));
        Assertions.assertNotNull(token.getIdUser());
        Assertions.assertTrue(StringUtils.hasText(token.getUsername()));
        Assertions.assertEquals(EVerificationProcess.NONE, token.getProcess());
        ldt = LocalDateTime.now();
        Assertions.assertTrue(ldt.plusSeconds(1).isAfter(token.getIssued()));
        Assertions.assertTrue(ldt.minusSeconds(2).isBefore(token.getIssued()));
        ldt = ldt.plusDays((int)ReflectionTestUtils.getField(tokenManager, "daysToExpire"));
        Assertions.assertTrue(ldt.plusSeconds(1).isAfter(token.getExpire()));
        Assertions.assertTrue(ldt.minusSeconds(2).isBefore(token.getExpire()));
    }
    
    @Test
    public void testIsTokenValid() {
        SecretKey jwsSecretKey;
        JwtBuilder jwtBuilder;
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenValid(null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenValid(""));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenValid("      "));
        
        Assertions.assertFalse(tokenManager.isTokenValid("XXXX"));
        
        jwsSecretKey = (SecretKey) ReflectionTestUtils.getField(tokenManager, "jwsSecretKey");
        
        // Builder
        jwtBuilder = Jwts.builder()
                .setIssuer(null)
                .setId(null)
                .setSubject(null)
                .setAudience(null)
                .setIssuedAt(null)
                .setExpiration(null)
                .claim("XX", "XX")
                .signWith(jwsSecretKey)
                ;
        
        // Token id
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setId("");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setId("     ");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setId(UUID.randomUUID().toString());
        
        // Issuer
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuer("");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuer("    ");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuer("XXX");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuer((String)ReflectionTestUtils.getField(tokenManager, "issuer"));
        
        // Subject
        jwtBuilder.setSubject(null);
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setSubject("");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setSubject("  ");
        jwtBuilder.setSubject("XXX");
        
        // Audience
        jwtBuilder.setAudience("");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setAudience("   ");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setAudience("XXX");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setAudience(EVerificationProcess.NONE.name());
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        
        // Issued at
        jwtBuilder.setIssuedAt(Date.from(LocalDateTime.now().plusDays(5).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuedAt(Date.from(LocalDateTime.now().plusSeconds(1).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setIssuedAt(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        
        // expiration
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().minusNanos(1).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().plusDays(5).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        
        // Claim user id
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "    ");
        Assertions.assertFalse(tokenManager.isTokenValid(jwtBuilder.compact()));
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, SAMPLE_ID);
        Assertions.assertTrue(tokenManager.isTokenValid(jwtBuilder.compact()));
    }
    
    @Test
    public void testIsTokenValidNotCheckExpired() {
        SecretKey jwsSecretKey;
        JwtBuilder jwtBuilder;
        
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenValidNotCheckExpired(null));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenValidNotCheckExpired(""));
        Assertions.assertThrows(ValidationException.class, () -> tokenManager.isTokenValidNotCheckExpired("      "));

        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired("XXXX"));
        
        jwsSecretKey = (SecretKey) ReflectionTestUtils.getField(tokenManager, "jwsSecretKey");
        
        // Builder
        jwtBuilder = Jwts.builder()
                .setIssuer(null)
                .setId(null)
                .setSubject(null)
                .setAudience(null)
                .setIssuedAt(null)
                .setExpiration(null)
                .claim("XX", "XX")
                .signWith(jwsSecretKey)
                ;
        
        // Token id
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setId("");
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setId("     ");
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setId(UUID.randomUUID().toString());
        
        // Issuer
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setIssuer("");
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setIssuer("    ");
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setIssuer("XXX");
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setIssuer((String)ReflectionTestUtils.getField(tokenManager, "issuer"));
        
        // Subject
        jwtBuilder.setSubject(null);
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setSubject("");
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setSubject("  ");
        jwtBuilder.setSubject("XXX");
        
        // Audience
        jwtBuilder.setAudience("");
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setAudience("   ");
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setAudience("XXX");
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setAudience(EVerificationProcess.NONE.name());
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        
        // Issued at
        jwtBuilder.setIssuedAt(Date.from(LocalDateTime.now().plusDays(5).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setIssuedAt(Date.from(LocalDateTime.now().plusSeconds(1).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setIssuedAt(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        
        // expiration
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().minusNanos(1).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.setExpiration(Date.from(LocalDateTime.now().plusDays(5).atZone(ZoneId.systemDefault()).toInstant()));
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        
        // Claim user id
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "");
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, "    ");
        Assertions.assertFalse(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));
        jwtBuilder.claim(ITokenManager.CLAIM_USERID, SAMPLE_ID);
        Assertions.assertTrue(tokenManager.isTokenValidNotCheckExpired(jwtBuilder.compact()));        
    }
}
