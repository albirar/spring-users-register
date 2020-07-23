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
package cat.albirar.users.registration;

import java.util.Locale;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import cat.albirar.communications.channels.models.CommunicationChannelBean;
import cat.albirar.communications.channels.models.ContactBean;
import cat.albirar.users.config.PropertiesCore;
import cat.albirar.users.models.registration.RegistrationProcessResultBean;
import cat.albirar.users.models.tokens.AbstractTokenBean;
import cat.albirar.users.models.tokens.ApprobationTokenBean;
import cat.albirar.users.models.tokens.RecoverPasswordTokenBean;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.verification.EVerificationProcess;

/**
 * Service for registration process.
 * The registration process depends on application property {@value IRegistrationService#VERIFICATION_MODE_PROPERTY_NAME}, that can be:
 * <ul>
 * <li>{@link EVerificationProcess#NONE} without any verification in order to register the new user, that becomes {@link UserBean#isEnabled() enabled} upon creation</li>
 * <li>{@link EVerificationProcess#ONE_STEP} the owner of {@link UserBean#getPreferredChannel() preferred channel} should to confirm the registration of the new user, that becomes {@link UserBean#isEnabled() disabled} until confirmation is made</li>
 * <li>{@link EVerificationProcess#TWO_STEP} the owner of {@link UserBean#getPreferredChannel() preferred channel} should to confirm the registration and a supervisor should to approve the registration; meanwhile the new user becomes {@link UserBean#isEnabled() disabled} until confirmation AND approbation is made</li>
 * </ul>
 * <p>Also offers access to registry, to get, update or delete users.</p>
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public interface IRegistrationService {
    /**
     * Property name to establish the {@link EVerificationProcess verification process} of register.
     */
    public static final String VERIFICATION_MODE_PROPERTY_NAME = PropertiesCore.VERIFICATION_MODE_PROPERTY_NAME;
    /**
     * Register a new user from {@link PropertiesCore#ROOT_SENDER default sender} with the {@code username}, {@code preferredChannel}, {@link Locale#getDefault() default locale} and {@code password} (optional). 
     * The process is:
     * <ol>
     * <li>Create the user at the registry but {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered}</li>
     * <li>Depending on value of property named {@value #VERIFICATION_MODE_PROPERTY_NAME}:
     *    <ul>
     *       <li>If value is {@link EVerificationProcess#NONE}, no verification nor approbation is needed in order to register the new user, that becomes {@link UserBean#isEnabled() enabled}, {@link UserBean#getVerified() verified} and {@link UserBean#getRegistered() registered} upon creation</li>
     *       <li>If value is {@link EVerificationProcess#ONE_STEP}, the owner of {@link UserBean#getPreferredChannel() preferred channel} should to verify the registration of the new user, that becomes {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered} until verification is made</li>
     *       <li>If value is {@link EVerificationProcess#TWO_STEP}, in addition to verification, a supervisor should to approve the registration; meanwhile the new user becomes {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered} until verification AND approbation is made</li>
     *    </ul>
     * </li>
     * <li>The user is enabled or disabled</li>
     * </ol>
     * @param senderChannel The sender channel (from on email)
     * @param username The user name of new user
     * @param preferredChannel The preferred channel to register user and send verification message
     * @param password An optional password provided in case of 
     * @return The result, with {@link UserBean#getId()} informed and disabled depending on registration process configuration
     * @throws DuplicateKeyException If {@code username} or {@code preferredChannel} exists in register
     * @see #verifyUser(String)
     * @see #approveUser(String) 
     */
    public RegistrationProcessResultBean registerUser(@NotBlank String username, @NotNull @Validated CommunicationChannelBean preferredChannel, @Nullable String password);
    /**
     * Register a new user from {@link PropertiesCore#ROOT_SENDER default sender} with the {@code username}, {@code preferredChannel}, {@code locale} and {@code password} (optional). 
     * The process is:
     * <ol>
     * <li>Create the user at the registry but {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered}</li>
     * <li>Depending on value of property named {@value #VERIFICATION_MODE_PROPERTY_NAME}:
     *    <ul>
     *       <li>If value is {@link EVerificationProcess#NONE}, no verification nor approbation is needed in order to register the new user, that becomes {@link UserBean#isEnabled() enabled}, {@link UserBean#getVerified() verified} and {@link UserBean#getRegistered() registered} upon creation</li>
     *       <li>If value is {@link EVerificationProcess#ONE_STEP}, the owner of {@link UserBean#getPreferredChannel() preferred channel} should to verify the registration of the new user, that becomes {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered} until verification is made</li>
     *       <li>If value is {@link EVerificationProcess#TWO_STEP}, in addition to verification, a supervisor should to approve the registration; meanwhile the new user becomes {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered} until verification AND approbation is made</li>
     *    </ul>
     * </li>
     * <li>The user is enabled or disabled</li>
     * </ol>
     * @param username The user name of new user
     * @param preferredChannel The preferred channel to register user and send verification message
     * @param locale The preferred locale for this user
     * @param password An optional password provided in case of 
     * @return The result, with {@link UserBean#getId()} informed and disabled depending on registration process configuration
     * @throws DuplicateKeyException If {@code username} or {@code preferredChannel} exists in register
     * @see #verifyUser(String)
     * @see #approveUser(String) 
     */
    public RegistrationProcessResultBean registerUser(@NotBlank String username, @NotNull @Validated CommunicationChannelBean preferredChannel, @NotNull Locale locale, @Nullable String password);
    /**
     * Register a new user from {@code sender} with the {@code username}, {@code preferredChannel}, {@link Locale#getDefault() default locale} and {@code password} (optional). 
     * The process is:
     * <ol>
     * <li>Create the user at the registry but {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered}</li>
     * <li>Depending on value of property named {@value #VERIFICATION_MODE_PROPERTY_NAME}:
     *    <ul>
     *       <li>If value is {@link EVerificationProcess#NONE}, no verification nor approbation is needed in order to register the new user, that becomes {@link UserBean#isEnabled() enabled}, {@link UserBean#getVerified() verified} and {@link UserBean#getRegistered() registered} upon creation</li>
     *       <li>If value is {@link EVerificationProcess#ONE_STEP}, the owner of {@link UserBean#getPreferredChannel() preferred channel} should to verify the registration of the new user, that becomes {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered} until verification is made</li>
     *       <li>If value is {@link EVerificationProcess#TWO_STEP}, in addition to verification, a supervisor should to approve the registration; meanwhile the new user becomes {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered} until verification AND approbation is made</li>
     *    </ul>
     * </li>
     * <li>The user is enabled or disabled</li>
     * </ol>
     * @param sender The sender to use
     * @param username The user name of new user
     * @param preferredChannel The preferred channel to register user and send verification message
     * @param password An optional password provided in case of 
     * @return The result, with {@link UserBean#getId()} informed and disabled depending on registration process configuration
     * @throws DuplicateKeyException If {@code username} or {@code preferredChannel} exists in register
     * @see #verifyUser(String)
     * @see #approveUser(String) 
     */
    public RegistrationProcessResultBean registerUser(@NotNull @Valid ContactBean sender, @NotBlank String username, @NotNull @Validated CommunicationChannelBean preferredChannel, @Nullable String password);
    /**
     * Register a new user from {@code sender} with the {@code username}, {@code preferredChannel}, {@code locale} and {@code password} (optional). 
     * The process is:
     * <ol>
     * <li>Create the user at the registry but {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered}</li>
     * <li>Depending on value of property named {@value #VERIFICATION_MODE_PROPERTY_NAME}:
     *    <ul>
     *       <li>If value is {@link EVerificationProcess#NONE}, no verification nor approbation is needed in order to register the new user, that becomes {@link UserBean#isEnabled() enabled}, {@link UserBean#getVerified() verified} and {@link UserBean#getRegistered() registered} upon creation</li>
     *       <li>If value is {@link EVerificationProcess#ONE_STEP}, the owner of {@link UserBean#getPreferredChannel() preferred channel} should to verify the registration of the new user, that becomes {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered} until verification is made</li>
     *       <li>If value is {@link EVerificationProcess#TWO_STEP}, in addition to verification, a supervisor should to approve the registration; meanwhile the new user becomes {@link UserBean#isEnabled() disabled}, {@link UserBean#getVerified() not verified} and {@link UserBean#getRegistered() not registered} until verification AND approbation is made</li>
     *    </ul>
     * </li>
     * <li>The user is enabled or disabled</li>
     * </ol>
     * @param sender The sender to use
     * @param username The user name of new user
     * @param preferredChannel The preferred channel to register user and send verification message
     * @param locale The preferred locale for this user
     * @param password An optional password provided in case of 
     * @return The result, with {@link UserBean#getId()} informed and disabled depending on registration process configuration
     * @throws DuplicateKeyException If {@code username} or {@code preferredChannel} exists in register
     * @see #verifyUser(String)
     * @see #approveUser(String) 
     */
    public RegistrationProcessResultBean registerUser(@NotNull @Valid ContactBean sender, @NotBlank String username, @NotNull @Validated CommunicationChannelBean preferredChannel, @NotNull Locale locale, @Nullable String password);
    /**
     * Update the associated user of {@code token} to indicate the new state of {@link UserBean#getVerified() verified}.
     * <p>If value of property named {@value #VERIFICATION_MODE_PROPERTY_NAME} is {@link EVerificationProcess#ONE_STEP}, the verification sets the user {@link UserBean#isEnabled() enabled} and {@link UserBean#getRegistered() registered}.</p>
     * <p>If value of property named {@value #VERIFICATION_MODE_PROPERTY_NAME} is {@link EVerificationProcess#TWO_STEP} a {@link #approveUser(String) approbation} should be made to enabling the user.</p>
     * @param token The token used to {@link #registerUser(String, CommunicationChannelBean, String)}
     * @return true if the user exists and the previous state is "not-verified", false if user exists but the previous state is not "not-verified" and {@link Optional#empty()} if no user exists with the indicated {@link VerificationTokenBean#getIdUser()}
     */
    public Optional<Boolean> verifyUser(@NotBlank String token);
    /**
     * Update the user to indicate the new state of approved.
     * <p>This is the second step in {@link EVerificationProcess#TWO_STEP} process verification. This step is committed by supervisor user.</p>
     * <p>Before call to this step, a call to {@link #verifyUser(ObjectId)} should to be made by the link with token and called by owner of {@link CommunicationChannelBean} indicated in {@link #registerUser(String, CommunicationChannelBean, String)}.</p>
     * @param token The token used to {@link #registerUser(String, CommunicationChannelBean, String)}
     * @return true if the user exists and the previous state is "verified", false if user exists but the previous state is not "verified" or if token is invalid and {@link Optional#empty()} if no user exists with the indicated {@link ApprobationTokenBean#getIdUser()}
     */
    public Optional<Boolean> approveUser(@NotBlank String token);
    /**
     * Assigns the {@code newPassword} to the {@link RecoverPasswordTokenBean#getIdUser() user} on {@code token}.
     * @param token A valid {@link RecoverPasswordTokenBean recover password token}
     * @param newPassword The new password to assign to the related user, raw password without encoding, this method will encode it
     * @return true if the users exists and state is {@code verified} or {@code registered}, false if state of user is {@code created} or if token is not valid {@link RecoverPasswordTokenBean}; {@link Optional#empty()} if no user exists with the indicated {@link RecoverPasswordTokenBean#getIdUser()}
     */
    public Optional<Boolean> recoverPassword(@NotBlank String token, @NotBlank String newPassword);
    /**
     * Find the user by the indicated {@code username}.
     * @param username The username
     * @return The user, if found, or {@link Optional#empty()} if not found
     */
    public Optional<UserBean> getUserByUsername(@NotBlank String username);
    /**
     * Find the user by the indicated {@code id}.
     * @param id The user id
     * @return The user, if found, or {@link Optional#empty()} if not found
     */
    public Optional<UserBean> getUserById(@NotBlank String id);
    /**
     * Find the user by the indicated {@code registrationToken}.
     * @param token A valid token (derived from {@link AbstractTokenBean})
     * @return The user, if found, or {@link Optional#empty()} if not found or if token is not valid
     */
    public Optional<UserBean> getUserByToken(@NotBlank String token);
    /**
     * Update the specified {@code user}.
     * A few properties can't be updated:
     * <ul>
     * <li>{@link UserBean#getCreated()} the created timestamp</li>
     * <li>{@link UserBean#getVerified()} the verified timestamp</li>
     * <li>{@link UserBean#getRegistered()} the registered timestamp</li>
     * </ul>
     * <p>The indicated {@link UserBean#getPassword() password} on {@code user} should to be NON-ENCRYPTED raw password, so this method encoded them.</p>
     * @param user The user
     * @return true if the user was updated and false if no differences are between indicated {@code user} and persisted user
     * @throws ValidationException If {@link UserBean#getId()} is blank or null
     * @throws DataRetrievalFailureException If no user was found associated with the indicated {@link UserBean#getId() user id} 
     * @throws IllegalStateException If the {@code user} has not been registered 
     * @throws IllegalArgumentException If the indicated {@code user} contains information that cannot be updated
     * @throws DataIntegrityViolationException If the updated {@link UserBean#getUsername()}, {@link UserBean#getPreferredChannel()} or {@link UserBean#getSecondaryChannel()} exists for other user
     */
    public boolean updateUser(@NotNull @Valid UserBean user);
}
