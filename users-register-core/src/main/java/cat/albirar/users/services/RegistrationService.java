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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cat.albirar.communications.models.CommunicationChannelBean;
import cat.albirar.users.models.auth.AuthorizationBean;
import cat.albirar.users.models.auth.ERole;
import cat.albirar.users.models.registration.RegistrationProcessResultBean;
import cat.albirar.users.models.tokens.ApprobationTokenBean;
import cat.albirar.users.models.tokens.RecoverPasswordTokenBean;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.models.verification.ProcessBean;
import cat.albirar.users.registration.IRegistrationService;
import cat.albirar.users.repos.IUserRepo;
import cat.albirar.users.verification.EVerificationProcess;
import cat.albirar.users.verification.ITokenManager;
import cat.albirar.users.verification.IVerificationProcessService;

/**
 * The registration service implementation.
 * Allow to register users and to update or delete users.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Service("registrationService")
@Validated
public class RegistrationService implements IRegistrationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);
    
    @Value("${" + VERIFICATION_MODE_PROPERTY_NAME + ":NONE}")
    private EVerificationProcess verification;
    
    @Autowired
    private IUserRepo userRepo;
    
    @Autowired
    private IVerificationProcessService verificationProcessService;
    
    @Autowired
    private ITokenManager tokenManager;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    /**
     * {@inheritDoc}
     */
    @Override
    public RegistrationProcessResultBean registerUser(String username, CommunicationChannelBean preferredChannel, String password) {
        UserBean ub;
        UserBean nUser;
        RegistrationProcessResultBean result;
        ProcessBean vBean;
        String idVerification;
        String token;
        LocalDateTime ldt;
        
        // First, check duplicates
        if(userRepo.existsByUsername(username)) {
            throw new DuplicateKeyException(String.format("The username '%s' is in use", username));
        }
        if(userRepo.existsByPreferredChannel(preferredChannel)) {
            throw new DuplicateKeyException(String.format("The %s '%s' is in use", preferredChannel.getChannelType().name().toLowerCase(), preferredChannel.getChannelId()));
        }
        
        ldt = LocalDateTime.now();
        ub = UserBean.builder()
                .username(username)
                .preferredChannel(preferredChannel)
                .authorities(Arrays.asList(new AuthorizationBean [] {AuthorizationBean.builder().authority(ERole.User.name()).build()}))
                .enabled(verification == EVerificationProcess.NONE) // Only NONE is enabled by default
                .created(ldt)
                .verified(verification == EVerificationProcess.NONE ? ldt : null)
                .registered(verification == EVerificationProcess.NONE ? ldt : null)
                .password(password)
                .build()
                ;
        nUser = userRepo.save(ub);
        idVerification = null;
        token = null;
        if(verification != EVerificationProcess.NONE) {
            // Start the verification process
            token = tokenManager.encodeToken(tokenManager.generateVerificationTokenBean(nUser, verification).get());
            vBean = ProcessBean.builder()
                    .channel(preferredChannel)
                    .token(token)
                    .build()
                    ;
            idVerification = verificationProcessService.startVerifyProcess(vBean);
        }
        result = RegistrationProcessResultBean.builder()
                .id(idVerification)
                .verificationProcess(verification)
                .user(nUser)
                .token(Optional.ofNullable(token))
                .build()
                ;
        return result;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Boolean> verifyUser(String token) {
        UserBean usr;
        Optional<UserBean> oUsr;
        Optional<VerificationTokenBean> oTkBean;
        VerificationTokenBean tkBean;
        
        LOGGER.debug("Verifying string token {}", token);
        oTkBean = tokenManager.decodeToken(VerificationTokenBean.class, token);
        
        if(oTkBean.isPresent()) {
            tkBean = oTkBean.get();
            LOGGER.debug("Verifying token {}", tkBean);
            oUsr = userRepo.findById(tkBean.getIdUser());
            if(!oUsr.isPresent()) {
                LOGGER.warn("The user at token {} is not found, cannot be verified!", token);
                return Optional.empty();
            }
            usr = oUsr.get();
            LOGGER.debug("Token {} contains user {}", tkBean, usr);
            if(usr.getVerified() == null) {
                usr.setVerified(LocalDateTime.now());
                if(tkBean.getProcess() != EVerificationProcess.TWO_STEP) {
                    usr.setRegistered(usr.getVerified());
                }
                usr.setEnabled(usr.getRegistered() != null);
                userRepo.save(usr);
                LOGGER.debug("User {} IS verified!", usr);
                return Optional.of(true);
            }
        } else {
            LOGGER.warn("The string token {} cannot be decoded, no verification can be made!", token);
        }
        return Optional.of(false);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Boolean> approveUser(String token) {
        UserBean usr;
        Optional<UserBean> oUsr;
        Optional<ApprobationTokenBean> oTkBean;
        ApprobationTokenBean tkBean;
        
        LOGGER.debug("Approving string token {}", token);
        oTkBean = tokenManager.decodeToken(ApprobationTokenBean.class, token);
        
        if(oTkBean.isPresent()) {
            tkBean = oTkBean.get();
            LOGGER.debug("Approving token {}", tkBean);
            oUsr = userRepo.findById(tkBean.getIdUser());
            if(!oUsr.isPresent()) {
                LOGGER.warn("The user at token {} is not found, cannot be approved!", token);
                return Optional.empty();
            }
            usr = oUsr.get();
            LOGGER.debug("Token {} contains user {}", tkBean, usr);
            if(usr.getVerified() != null) {
                if(usr.getRegistered() == null) {
                    usr.setRegistered(LocalDateTime.now());
                    usr.setEnabled(true);
                    userRepo.save(usr);
                    LOGGER.debug("User {} IS approved!", usr);
                    return Optional.of(true);
                } else {
                    LOGGER.warn("The user was approbed before, cannot process!", tkBean);
                }
            } else {
                LOGGER.warn("The user was not verifyied before, cannot process!", tkBean);
            }
        } else {
            LOGGER.warn("The string token {} cannot be decoded, no approbation can be made!", token);
        }
        return Optional.of(false);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Boolean> recoverPassword(String token, String newPassword) {
        Optional<RecoverPasswordTokenBean> oTk;
        RecoverPasswordTokenBean tk;
        Optional<UserBean> oUsr;
        
        LOGGER.debug("Recover password string token {}", token);
        
        oTk = tokenManager.decodeToken(RecoverPasswordTokenBean.class, token);
        
        if(oTk.isPresent()) {
            tk = oTk.get();
            LOGGER.debug("Recover password token {}", tk);
            oUsr = userRepo.findById(tk.getIdUser());
            
            if(oUsr.isPresent()) {
                if(oUsr.get().getRegistered() != null) {
                    oUsr.get().setPassword(passwordEncoder.encode(newPassword));
                    userRepo.save(oUsr.get());
                    LOGGER.info("Password changed successfully for user {} with id {}", oUsr.get().getUsername(), tk.getIdUser());
                    return Optional.of(true);
                } else {
                    LOGGER.debug("Cannot change password for unregistered user {} with id {}", oUsr.get().getUsername(), oUsr.get().getId());
                    return Optional.of(false);
                }
            }
            LOGGER.warn("The user with id {} at token {} is not found, cannot recover password!", tk.getIdUser(), token);
            return Optional.empty();
        }
        return Optional.of(false);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserBean> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserBean> getUserById(String id) {
        return userRepo.findById(id);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserBean> getUserByToken(String token) {
        Optional<UserBean> r;
        Optional<String> oUid;
        
        oUid = tokenManager.decodeUserId(token);
        if(oUid.isPresent()) {
            try {
                r = userRepo.findById(oUid.get());
            } catch (ValidationException e) {
                r = Optional.empty();
            }
        } else {
            r = Optional.empty();
        }
        return r;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateUser(UserBean user) {
        Optional<UserBean> oUsr;
        UserBean aUser;
        
        oUsr = userRepo.findById(user.getId());
        if(oUsr.isPresent()) {
            // Check for equals, test password with passwordEncoder and sort authorities
            if(!equalsUsers(oUsr.get(), user)) {
                aUser = oUsr.get();
                // Only updates on registered users...
                if(aUser.getRegistered() != null) {
                    // No changes can be made on created, verified and registered timestamps
                    if(aUser.getRegistered().equals(user.getRegistered())
                            && aUser.getVerified().equals(user.getVerified())
                            && aUser.getCreated().equals(user.getCreated())) {
                        // Update
                        aUser = userRepo.save(user.toBuilder().password(passwordEncoder.encode(user.getPassword())).build());
                        return true;
                    }
                    throw new IllegalArgumentException(String.format("No changes can be made to registered date or verified date or created date for user id %s!", user.getId()));
                }
                throw new IllegalStateException(String.format("The user with id %s is not yet validated, cannot be updated!",user.getId()));
            }
            return false;
        }
        throw new DataRetrievalFailureException(String.format("No user with %s id was found", user.getId()));
    }
    
    private boolean equalsUsers(UserBean saved, UserBean update) {
        if(!passwordEncoder.matches(update.getPassword(), saved.getPassword())) {
            return false;
        }
        return saved.toBuilder()
                    .password("XXX")
                    .authorities(saved.getAuthorities().stream().sorted().collect(Collectors.toList()))
                    .build()
                .equals(update.toBuilder()
                        .password("XXX")
                        .authorities(update.getAuthorities().stream().sorted().collect(Collectors.toList()))
                        .build()
                );
    }
}
