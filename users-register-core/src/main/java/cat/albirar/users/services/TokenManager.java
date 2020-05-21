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
package cat.albirar.users.services;

import static cat.albirar.users.config.PropertiesCore.TOKEN_PROP_EXPIRE;
import static cat.albirar.users.config.PropertiesCore.TOKEN_PROP_ISSUER;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import cat.albirar.users.models.tokens.AbstractTokenBean;
import cat.albirar.users.models.tokens.ApprobationTokenBean;
import cat.albirar.users.models.tokens.ETokenClass;
import cat.albirar.users.models.tokens.RecoverPasswordTokenBean;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.verification.EVerificationProcess;
import cat.albirar.users.verification.ITokenManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

/**
 * A class to manage tokens.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Component
@Validated
public class TokenManager implements ITokenManager {

    
    @Value("${" + TOKEN_PROP_ISSUER + "}")
    private String issuer;
    
    @Value("${" + TOKEN_PROP_EXPIRE + "}")
    private int daysToExpire;
    
    @Autowired
    private SecretKey jwsSecretKey;
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends AbstractTokenBean> Optional<T> decodeToken(String token) {
        T tkBean;
        Claims body;

        try {
            body = decodeClaims(token);
            if(isValidClaims(body)) {
                tkBean = claimsToBean(body);
                return Optional.of(tkBean);
            }
        }
        catch(ClassCastException | ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            // Nothing to do
        }
        return Optional.empty();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AbstractTokenBean> decodeTokenEvenExpired(String token) {
        VerificationTokenBean tkBean;
        Claims body;

        body = null;
        try {
            try {
                body = decodeClaims(token);
            } catch(ExpiredJwtException e) {
                body = e.getClaims();
            }
            if(isValidClaims(body)) {
                tkBean = claimsToBean(body);
                return Optional.of(tkBean);
            }
        } catch(UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            // Nothing to do
        }
        
        return Optional.empty();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String encodeToken(AbstractTokenBean tokenBean) {
        JwtBuilder jBuilder;
        String jws;
        
        jBuilder = Jwts.builder()
                .setId(tokenBean.getTokenId())
                .setIssuer(issuer)
                .setSubject(tokenBean.getUsername())
                .setIssuedAt(Date.from(tokenBean.getIssued().atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(tokenBean.getExpire().atZone(ZoneId.systemDefault()).toInstant()))
                .claim(CLAIM_USERID, tokenBean.getIdUser())
                .claim(CLAIM_TOKEN_CLASS, tokenBean.getTokenClass().name())
                ;
        
        switch(tokenBean.getTokenClass()) {
            case VERIFICATION:
                jBuilder = encodeVerificationInformation(jBuilder, (VerificationTokenBean)tokenBean);
                break;
            case APPROBATION:
                jBuilder = encodeApprobationInformation(jBuilder, (ApprobationTokenBean)tokenBean);
                break;
            case RECOVER_PASSWORD:
            default:
                jBuilder = encodeRecoverPasswordInformation(jBuilder, (RecoverPasswordTokenBean)tokenBean);
                break;
        }
        jws = jBuilder
                .signWith(jwsSecretKey)
                .compact();
        return jws;
    }
    
    private JwtBuilder encodeVerificationInformation(JwtBuilder jwts, VerificationTokenBean tokenBean) {
        jwts.setAudience(tokenBean.getProcess().name());
        return jwts;
    }

    private JwtBuilder encodeApprobationInformation(JwtBuilder jwts, ApprobationTokenBean tokenBean) {
        jwts.claim(CLAIM_APPROVER_ID, tokenBean.getApproverId())
            .claim(CLAIM_APPROVER_USERNAME, tokenBean.getApproverUsername())
            ;
        return jwts;
    }
    
    private JwtBuilder encodeRecoverPasswordInformation(JwtBuilder jwts, RecoverPasswordTokenBean tokenBean) {
        jwts.claim(CLAIM_ORIGIN_CHANNEL, tokenBean.getOrigin().name())
            ;
        return jwts;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<VerificationTokenBean> generateVerificationTokenBean(UserBean user, EVerificationProcess process) {
        LocalDateTime ldt;
        
        if(process == EVerificationProcess.NONE) {
            return Optional.empty();
        }
        ldt = LocalDateTime.now();
        
        return Optional.of(VerificationTokenBean.builder()
                .tokenId(UUID.randomUUID().toString())
                .issued(ldt)
                .expire(ldt.plusDays(daysToExpire))
                .idUser(user.getId())
                .username(user.getUsername())
                .process(process)
                .build())
                ;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public ApprobationTokenBean generateApprobationTokenBean(UserBean user, UserBean approver) {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public RecoverPasswordTokenBean generateRecoverPasswordTokenBean(UserBean user, boolean preferredChannel) {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ETokenClass> getTokenClass( String token) {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTokenValid(String token) {
        Claims body;
        
        try {
            body = decodeClaims(token);
            return isValidClaims(body);
        } catch(UnsupportedJwtException | MalformedJwtException | SignatureException | ExpiredJwtException | IllegalArgumentException e) {
            // Nothing to do
        }
        return false;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTokenValidNotCheckExpired(String token) {
        Claims body;
        
        try {
            body = decodeClaims(token);
            return isValidClaims(body);
        } catch (ExpiredJwtException  e) {
            // Even if expired!
            return isValidClaims(e.getClaims());
        } catch(UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            // Nothing to do
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    private <T extends AbstractTokenBean> T claimsToBean(Claims body) {
        return (T) VerificationTokenBean.builder()
                .tokenId(body.getId())
                .expire(LocalDateTime.from(body.getExpiration().toInstant().atZone(ZoneId.systemDefault())))
                .issued(LocalDateTime.from(body.getIssuedAt().toInstant().atZone(ZoneId.systemDefault())))
                .process(EVerificationProcess.valueOf(body.getAudience()))
                .idUser(body.get(CLAIM_USERID, String.class))
                .username(body.getSubject())
                .build()
                ;
    }
    /**
     * Verify if {@code body} is valid for decode into {@link VerificationTokenBean}, included checking dates.
     * Check that:
     * <ul>
     * <li>{@link Claims#getId()} is not-blank</li>
     * <li>{@link Claims#getIssuer()} is not-blank and is equal to value of property named {@value #PROP_ISSUER}</li>
     * <li>{@link Claims#getSubject()} is not-blank</li>
     * <li>{@link Claims#getAudience()} is not-blank</li>
     * <li>{@link Claims#getIssuedAt()} is not-null and is before now</li>
     * <li>{@link Claims#getExpiration()} is not-null</li>
     * <li>{@code body} {@link Claims#containsKey(Object) contains} a claim named {@value #CLAIM_USERID} and his content is not-blank</li>
     * </ul>
     * @param body The claims
     * @return true if valid and false if not
     */
    private boolean isValidClaims(Claims body) {
        return (StringUtils.hasText(body.getId())
                && StringUtils.hasText(body.getIssuer())
                && body.getIssuer().equals(issuer)
                && StringUtils.hasText(body.getSubject())
                && StringUtils.hasText(body.getAudience())
                && body.getIssuedAt() != null
                && body.getIssuedAt().before(new Date())
                && body.getExpiration() != null
                && body.containsKey(CLAIM_USERID)
                && StringUtils.hasText(body.get(CLAIM_USERID, String.class))
                );
    }
    /**
     * Decode the indicated {@code token} into {@link Claims} to operate.
     * @param token The token
     * @return The claims
     * @throws UnsupportedJwtException if the {@code token} argument does not represent an Claims JWS
     * @throws MalformedJwtException if the {@code token} string is not a valid JWS
     * @throws SignatureException if the {@code token} JWS signature validation fails
     * @throws ExpiredJwtException if the specified JWT is a Claims JWT and the Claims has an expiration time before the time this method is invoked.
     * @throws IllegalArgumentException if the {@code token} string is {@code null} or empty or only whitespace
     */
    private Claims decodeClaims(String token) {
        JwtParser jwsParser;
        Jws<Claims> jws;
        
        jwsParser = Jwts.parserBuilder()
                .setSigningKey(jwsSecretKey)
                .build();
        jws = jwsParser.parseClaimsJws(token);
        return jws.getBody();
    }
}
