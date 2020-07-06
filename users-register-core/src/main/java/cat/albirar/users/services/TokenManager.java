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
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import cat.albirar.communications.channels.models.ECommunicationChannelType;
import cat.albirar.users.models.tokens.AbstractTokenBean;
import cat.albirar.users.models.tokens.AbstractTokenBean.AbstractTokenBeanBuilder;
import cat.albirar.users.models.tokens.ApprobationTokenBean;
import cat.albirar.users.models.tokens.ETokenClass;
import cat.albirar.users.models.tokens.RecoverPasswordTokenBean;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.utils.LocaleUtils;
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
    public <T extends AbstractTokenBean> Optional<T> decodeToken(Class<T> tokenClass, String token) {
        T tkBean;
        Claims body;

        try {
            body = decodeClaims(token);
            if(isValidClaims(body)) {
                tkBean = claimsToBean(tokenClass, body);
                return Optional.of(tkBean);
            }
        }
        catch(ClassCastException | ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            // Nothing to do, an empty is returned
        }
        return Optional.empty();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> decodeUserId(@NotBlank String token) {
        Claims body;

        try {
            body = decodeClaims(token);
            return Optional.ofNullable(body.get(CLAIM_USERID, String.class));
        }
        catch(ClassCastException | ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            // Nothing to do, an empty is returned
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
        
        if(tokenBean.getTokenClass() != null) {
            jBuilder = Jwts.builder()
                    .setId(tokenBean.getTokenId())
                    .setIssuer(issuer)
                    .setSubject(tokenBean.getUsername())
                    .setIssuedAt(Date.from(tokenBean.getIssued().atZone(ZoneId.systemDefault()).toInstant()))
                    .setExpiration(Date.from(tokenBean.getExpire().atZone(ZoneId.systemDefault()).toInstant()))
                    .claim(CLAIM_USERID, tokenBean.getIdUser())
                    .claim(CLAIM_LOCALE, tokenBean.getLocale().toString())
                    .claim(CLAIM_TOKEN_CLASS, tokenBean.getTokenClass().name())
                    ;
                if(tokenBean.getTokenClass() == ETokenClass.VERIFICATION) {
                    jBuilder = encodeVerificationInformation(jBuilder, (VerificationTokenBean)tokenBean);
                }
                if(tokenBean.getTokenClass() == ETokenClass.APPROBATION) {
                    jBuilder = encodeApprobationInformation(jBuilder, (ApprobationTokenBean)tokenBean);
                }
                if(tokenBean.getTokenClass() == ETokenClass.RECOVER_PASSWORD) {
                    jBuilder = encodeRecoverPasswordInformation(jBuilder, (RecoverPasswordTokenBean)tokenBean);
                }
        } else {
            throw new IllegalArgumentException(String.format("The %s class is not recognized as acceptable token bean", tokenBean.getClass().getCanonicalName()));
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
        if(process == EVerificationProcess.NONE) {
            return Optional.empty();
        }
        return Optional.of(buildAbstractToken(VerificationTokenBean.builder(), user)
                .process(process)
                .build())
                ;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ApprobationTokenBean> generateApprobationTokenBean(UserBean user, UserBean approver) {
        if(StringUtils.hasText(user.getId())
                && StringUtils.hasText(approver.getId())) {
            return Optional.of(buildAbstractToken(ApprobationTokenBean.builder(), user)
                    .approverId(approver.getId())
                    .approverUsername(approver.getUsername())
                    .build()
                    )
                    ;
        }
        return Optional.empty();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RecoverPasswordTokenBean> generateRecoverPasswordTokenBean(UserBean user, boolean preferredChannel) {
        ECommunicationChannelType t;
        if(StringUtils.hasText(user.getId())) {
            if(preferredChannel || user.getSecondaryChannel() == null) {
                t = user.getPreferredChannel().getChannelType();
            } else {
                t = user.getSecondaryChannel().getChannelType();
            }
            return Optional.of(buildAbstractToken(RecoverPasswordTokenBean.builder(), user)
                    .origin(t)
                    .build()
                    )
                    ;
        }
        return Optional.empty();
    }
    /**
     * Build the abstract part of any token.
     * @param <T> The token builder type
     * @param tokenBeanBuilder The token builder instance to populate 
     * @param user The user associated with token
     * @return The populated same {@code tokenBeanBuilder}
     */
    private <T extends AbstractTokenBeanBuilder<?,?>> T buildAbstractToken(T tokenBeanBuilder, UserBean user) {
        LocalDateTime ldt;
        
        ldt = LocalDateTime.now();
        tokenBeanBuilder
            .tokenId(UUID.randomUUID().toString())
            .issued(ldt)
            .expire(ldt.plusDays(daysToExpire))
            .idUser(user.getId())
            .locale(user.getPreferredLocale())
            .username(user.getUsername())
            ;
        return tokenBeanBuilder;
    }
    /**
     * Return the {@link ETokenClass} value of the {@link ITokenManager#CLAIM_TOKEN_CLASS} claim of {@code body}.
     * @param body The body
     * @return The value of claim or {@link Optional#empty()} if the content of the claim is not one of {@link ETokenClass} elements 
     */
    private Optional<ETokenClass> decodeTokenClass(Claims body) {
        try {
            return Optional.of(ETokenClass.valueOf(body.get(CLAIM_TOKEN_CLASS, String.class)));
        } catch (NullPointerException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTokenClass(String token, ETokenClass tokenClass) {
        Optional<ETokenClass> otk;
        
        try {
            otk = decodeTokenClass(decodeClaims(token));
            return (otk.isPresent() && otk.get() == tokenClass);
        } catch (Exception e) {
            // Error, return false
            return false;
        }
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
    @SuppressWarnings("unchecked")
    private <T extends AbstractTokenBean> T claimsToBean(Class<T> tokenClass, Claims body) {
        ETokenClass tks;
        
        tks = decodeTokenClass(body).get();
        if(tks == ETokenClass.VERIFICATION && tokenClass.equals(VerificationTokenBean.class)) {
            return (T) buildAbstractBean(VerificationTokenBean.builder(), body)
                    .process(EVerificationProcess.valueOf(body.getAudience()))
                    .build()
                    ;
        }
        if(tks == ETokenClass.APPROBATION && tokenClass.equals(ApprobationTokenBean.class)) {
            return (T) buildAbstractBean(ApprobationTokenBean.builder(), body)
                    .approverId(body.get(CLAIM_APPROVER_ID, String.class))
                    .approverUsername(body.get(CLAIM_APPROVER_USERNAME, String.class))
                    .build()
                    ;
        }
        if(tks == ETokenClass.RECOVER_PASSWORD && tokenClass.equals(RecoverPasswordTokenBean.class)) {
            return (T) buildAbstractBean(RecoverPasswordTokenBean.builder(), body)
                    .origin(ECommunicationChannelType.valueOf(body.get(CLAIM_ORIGIN_CHANNEL, String.class)))
                    .build()
                    ;
        }
        // Others elements not recognized here...
        throw new IllegalArgumentException("Not a recognized token class!");
    }

    /**
     * Build the abstract part of any token.
     * @param <T> The token builder type
     * @param tokenBeanBuilder The token builder instance to populate 
     * @param user The user associated with token
     * @return The populated same {@code tokenBeanBuilder}
     */
    private <T extends AbstractTokenBeanBuilder<?,?>> T buildAbstractBean(T tokenBeanBuilder, Claims body) {
        tokenBeanBuilder
            .tokenId(body.getId())
            .issued(LocalDateTime.from(body.getIssuedAt().toInstant().atZone(ZoneId.systemDefault())))
            .expire(LocalDateTime.from(body.getExpiration().toInstant().atZone(ZoneId.systemDefault())))
            .idUser(body.get(CLAIM_USERID, String.class))
            .locale(LocaleUtils.stringToLocale(body.get(CLAIM_LOCALE, String.class)))
            .username(body.getSubject())
            ;
        return tokenBeanBuilder;
    }
    /**
     * Verify if {@code body} is valid for decode into {@link VerificationTokenBean}, included checking dates.
     * Check that:
     * <ul>
     * <li>{@link Claims#getId()} is not-blank</li>
     * <li>{@link Claims#getIssuer()} is not-blank and is equal to value of property named {@value #PROP_ISSUER}</li>
     * <li>{@link Claims#getIssuedAt()} is not-null and is before now</li>
     * <li>{@link Claims#getExpiration()} is not-null</li>
     * <li>{@link Claims#getSubject()} is not-blank</li>
     * <li>{@code body} {@link Claims#containsKey(Object) contains} a claim named {@value ITokenManager#CLAIM_USERID} and his content is not-blank</li>
     * <li>{@code body} {@link Claims#containsKey(Object) contains} a claim named {@value ITokenManager#CLAIM_LOCALE} and his content is not-blank</li>
     * <li>{@code body} {@link Claims#containsKey(Object) contains} a claim named {@value ITokenManager#CLAIM_TOKEN_CLASS} and his content is not-blank and is equal to one of {@link ETokenClass} element {@link Enum#name() name}</li>
     * </ul>
     * Depending on value of {@link Claims#get(String, Class) claim} named {@value ITokenManager#CLAIM_TOKEN_CLASS}, make further checks:
     * <ul>
     * <li>If value is {@link ETokenClass#VERIFICATION}:
     *    <ul>
     *       <li>{@code body} {@link Claims#getAudience()} is not-blank and is one of {@link EVerificationProcess#ONE_STEP} or {@link EVerificationProcess#TWO_STEP}</li>
     *    </ul>
     * <li>
     * <li>If value is {@link ETokenClass#APPROBATION}:
     *    <ul>
     *       <li>{@code body} {@link Claims#containsKey(Object) contains} a claim named {@value ITokenManager#CLAIM_APPROVER_ID} and his content is not-blank</li>
     *       <li>{@code body} {@link Claims#containsKey(Object) contains} a claim named {@value ITokenManager#CLAIM_APPROVER_USERNAME} and his content is not-blank</li>
     *    </ul>
     * <li>
     * <li>If value is {@link ETokenClass#RECOVER_PASSWORD}:
     *    <ul>
     *       <li>{@code body} {@link Claims#containsKey(Object) contains} a claim named {@value ITokenManager#CLAIM_ORIGIN_CHANNEL} and his content is not-blank and is one of {@link ECommunicationChannelType} element {@link Enum#name() name}</li>
     *    </ul>
     * <li>
     * </ul>
     * @param body The claims
     * @return true if valid and false if not
     */
    private boolean isValidClaims(Claims body) {
        Optional<ETokenClass> tkcls;
        
        if (StringUtils.hasText(body.getId())
                && StringUtils.hasText(body.getIssuer())
                && body.getIssuer().equals(issuer)
                && body.getIssuedAt() != null
                && body.getIssuedAt().before(new Date())
                && body.getExpiration() != null
                && StringUtils.hasText(body.getSubject())
                && StringUtils.hasText(body.get(CLAIM_USERID, String.class))
                && StringUtils.hasText(body.get(CLAIM_LOCALE, String.class))
                && StringUtils.hasText(body.get(CLAIM_TOKEN_CLASS, String.class)) ) {
            
            tkcls = decodeTokenClass(body);
            
            if(tkcls.isPresent()) {
                if(tkcls.get() == ETokenClass.VERIFICATION) {
                    return (StringUtils.hasText(body.getAudience())
                            && (body.getAudience().equals(EVerificationProcess.ONE_STEP.name())
                                    || body.getAudience().equals(EVerificationProcess.TWO_STEP.name()))
                            );
                }
                
                if(tkcls.get() == ETokenClass.APPROBATION) {
                    return (body.containsKey(CLAIM_APPROVER_ID)
                            && StringUtils.hasText(body.get(CLAIM_APPROVER_ID, String.class))
                            && body.containsKey(CLAIM_APPROVER_USERNAME)
                            && StringUtils.hasText(body.get(CLAIM_APPROVER_USERNAME, String.class))
                            );
                }
                return (body.containsKey(CLAIM_ORIGIN_CHANNEL)
                    && StringUtils.hasText(body.get(CLAIM_ORIGIN_CHANNEL, String.class))
                    && (body.get(CLAIM_ORIGIN_CHANNEL, String.class).equals(ECommunicationChannelType.EMAIL.name())
                            || body.get(CLAIM_ORIGIN_CHANNEL, String.class).equals(ECommunicationChannelType.MOBILE.name())) );
            }
        }
        return false;
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
