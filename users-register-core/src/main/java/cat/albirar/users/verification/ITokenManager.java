/*
 * This file is part of "albirar users-register".
 * 
 * "albirar users-register" is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * "albirar users-register" is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with "albirar users-register" source code. If not,
 * see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.verification;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import cat.albirar.users.models.communications.ECommunicationChannelType;
import cat.albirar.users.models.tokens.AbstractTokenBean;
import cat.albirar.users.models.tokens.ApprobationTokenBean;
import cat.albirar.users.models.tokens.ETokenClass;
import cat.albirar.users.models.tokens.RecoverPasswordTokenBean;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.users.UserBean;
import io.jsonwebtoken.Claims;

/**
 * Contract for token management.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public interface ITokenManager {
    /**
     * Claim name for {@link AbstractTokenBean#getTokenClass()}.
     */
    public final static String CLAIM_TOKEN_CLASS = "tokenClass";
    /**
     * Claim name for {@link UserBean#getId()}.
     */
    public final static String CLAIM_USERID = "userId";
    /**
     * Claim name for {@link ApprobationTokenBean#getIdApprover()}.
     */
    public final static String CLAIM_APPROVER_ID = "approverId";
    /**
     * Claim name for {@link ApprobationTokenBean#getUsernameApprover()}.
     */
    public final static String CLAIM_APPROVER_USERNAME = "approverUsername";
    /**
     * Claim name for {@link RecoverPasswordTokenBean#getOrigin()}.
     */
    public final static String CLAIM_ORIGIN_CHANNEL = "originChannel";
    
    /**
     * Return the {@link ETokenClass token class} for the indicated {@code token}.
     * @param token The token
     * @return The token class or {@link Optional#empty()} if no token class is recognized on {@code token}
     * @see #isTokenValid(String)
     */
    Optional<ETokenClass> getTokenClass(@NotBlank String token);
    /**
     * Decode the indicated {@code token} to get information from only if {@code token} is {@link #isTokenValid(String) valid}.
     * @param <T> The {@link AbstractTokenBean#getTokenClass()} if {@code token} is not that type, an {@link Optional#empty()} is returned
     * @param token The token to decode
     * @return The {@link VerificationTokenBean} with the decoded information from token or {@link Optional#empty()} if token cannot be decoded or isn't a {@code T} token class
     * @see #isTokenValid(String)
     */
    <T extends AbstractTokenBean> Optional<T> decodeToken(@NotBlank String token);
    /**
     * Decode the indicated {@code token} to get information from, only if {@code token} is {@link #isTokenValidNotCheckExpired(String) valid} even if expired.
     * @param <T> The {@link AbstractTokenBean#getTokenClass()} if {@code token} is not that type, an {@link Optional#empty()} is returned
     * @param token The token to decode
     * @return The {@link VerificationTokenBean} with the decoded information from token or {@link Optional#empty()} if token cannot be decoded or isn't a {@code T} token class
     * @see #isTokenValidNotCheckExpired(String)
     */
    <T extends AbstractTokenBean> Optional<T> decodeTokenEvenExpired(@NotBlank String token);

    /**
     * Encode the indicated {@code tokenBean} class.
     * @param tokenBean The token bean class, required
     * @return The encoded token
     */
    String encodeToken(@NotNull @Valid AbstractTokenBean tokenBean);

    /**
     * Generate a token bean from the {@code user} and for the indicated verification {@code process}.
     * The generated token bean contains the following values:
     * <ul>
     * <li>{@link VerificationTokenBean#setTokenId(String)} with a random unique id</li>
     * <li>{@link VerificationTokenBean#setIssued(LocalDateTime)} at now</li>
     * <li>{@link VerificationTokenBean#setExpire(LocalDateTime)} at now plus the days indicated in property value named {@value #PROP_EXPIRE}</li>
     * <li>{@link VerificationTokenBean#setIdUser(ObjectId)} as {@link UserBean#getId()} from {@code user}</li>
     * <li>{@link VerificationTokenBean#setUsername(String)} as {@link UserBean#getUsername()} from {@code user}</li>
     * <li>{@link VerificationTokenBean#setProcess(EVerificationProcess)} as {@link Enum#name()} value from {@code process} (that should to be {@link EVerificationProcess#ONE_STEP} or {@link EVerificationProcess#TWO_STEP})</li>
     * </ul>
     * @param user The user, should to be informed and should to be valid
     * @param process The verification process for this token bean
     * @return The token bean or {@link Optional#empty()} if {@code process} is {@link EVerificationProcess#NONE}
     */
    Optional<VerificationTokenBean> generateVerificationTokenBean(@NotNull @Valid UserBean user, @NotNull EVerificationProcess process);
    /**
     * Generate the token for approbation if running a {@link EVerificationProcess#TWO_STEP} verification process.
     * @param user The user to approve
     * @param approver The approver
     * @return The {@link ApprobationTokenBean}
     */
    ApprobationTokenBean generateApprobationTokenBean(@NotNull @Valid UserBean user, @NotNull @Valid UserBean approver);
    /**
     * Generate the token for recover password.
     * @param user The user of what a recover password token should to be generated
     * @param preferredChannel true if the {@link UserBean#getPreferredChannel()} should to be used, false for use the {@link UserBean#getSecondaryChannel()} (if no secondary channel exists, the preferred will be used)
     * @return The {@link RecoverPasswordTokenBean} 
     */
    RecoverPasswordTokenBean generateRecoverPasswordTokenBean(@NotNull @Valid UserBean user, boolean preferredChannel);

    /**
     * Verify if indicated {@code token} is valid and not expired.
     * Check all of {@link #isTokenValidNotCheckExpired(String)} and also if:
     * <ul>
     * <li>{@link Claims#getExpiration()} is not-null and is after now</li>
     * </ul>
     * @param token The token
     * @return true if valid and false if not valid or expired
     */
    boolean isTokenValid(@NotBlank String token);
    /**
     * Verify if indicated {@code token} is valid even if expired.
     * Check if:
     * <ul>
     * <li>{@link Claims#getId()} is not-blank</li>
     * <li>{@link Claims#getIssuer()} is not-blank and is equal to value of property named {@value #PROP_ISSUER}</li>
     * <li>{@link Claims#getIssuedAt()} is not-null and is before now</li>
     * <li>{@link Claims#getSubject()} is not-blank</li>
     * <li>{@code body} {@link Claims#containsKey(Object) contains} a claim named {@value #CLAIM_USERID} and his content is not-blank</li>
     * </ul>
     * Additional verification depending on {@link AbstractTokenBean#getTokenClass() token class}:
     * <ul>
     * <li>For {@link ETokenClass#VERIFICATION}:
     *    <ul>
     *       <li>{@link Claims#getAudience()} is not-blank</li>
     *    </ul>
     * </li>
     * <li>For {@link ETokenClass#APPROBATION}:
     *    <ul>
     *       <li>{@code body} {@link Claims#containsKey(Object) contains} a claim named {@value #CLAIM_APPROVER_ID} and his content is not-blank</li>
     *       <li>{@code body} {@link Claims#containsKey(Object) contains} a claim named {@value #CLAIM_APPROVER_USERNAME} and his content is not-blank</li>
     *    </ul>
     * </li>
     * <li>For {@link ETokenClass#RECOVER_PASSWORD}:
     *    <ul>
     *       <li>{@code body} {@link Claims#containsKey(Object) contains} a claim named {@value #CLAIM_ORIGIN_CHANNEL} and his content is not-blank and is one of {@link ECommunicationChannelType}</li>
     *    </ul>
     * </li>
     * </ul>
     * @param token The token
     * @return true if valid and false if not valid
     */
    boolean isTokenValidNotCheckExpired(@NotBlank String token);
}