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

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cat.albirar.users.models.auth.AuthorizationBean;
import cat.albirar.users.models.auth.ERole;
import cat.albirar.users.models.communications.CommunicationChannel;
import cat.albirar.users.models.registration.RegistrationProcessResultBean;
import cat.albirar.users.models.tokens.AbstractTokenBean;
import cat.albirar.users.models.tokens.ApprobationTokenBean;
import cat.albirar.users.models.tokens.ETokenClass;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.models.verification.VerificationProcessBean;
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

    @Value("${albirar.auth.registration.template}")
    private Resource registrationTemplate;
    /**
     * {@inheritDoc}
     */
    @Override
    public RegistrationProcessResultBean registerUser(String username, CommunicationChannel preferredChannel, String password) {
        UserBean ub;
        UserBean nUser;
        RegistrationProcessResultBean result;
        VerificationProcessBean vBean;
        long idVerification;
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
        idVerification = 0L;
        token = null;
        if(verification != EVerificationProcess.NONE) {
            // Start the verification process
            token = tokenManager.encodeToken(tokenManager.generateVerificationTokenBean(nUser, verification).get());
            vBean = VerificationProcessBean.builder()
                    .channel(preferredChannel)
                    .token(token)
                    .template(registrationTemplate)
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
    public Optional<Boolean> userVerified(String token) {
        UserBean usr;
        Optional<UserBean> oUsr;
        Optional<AbstractTokenBean> oTkBean;
        AbstractTokenBean aTkBean;
        VerificationTokenBean tkBean;
        
        LOGGER.debug("Verifying string token {}", token);
        oTkBean = tokenManager.decodeToken(token);
        
        if(oTkBean.isPresent()) {
            aTkBean = oTkBean.get();
            LOGGER.debug("Verifying token {}", aTkBean);
            if(aTkBean.getTokenClass() == ETokenClass.VERIFICATION) {
                tkBean = (VerificationTokenBean)aTkBean;
                if(tkBean.getProcess() == EVerificationProcess.ONE_STEP
                        || tkBean.getProcess() == EVerificationProcess.TWO_STEP) {
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
                    LOGGER.warn("The process of the token {} is not verifiable, cannot process!", tkBean);
                }
            } else {
                LOGGER.error("A {} token is used as a {} token!", aTkBean.getTokenClass().name(), ETokenClass.VERIFICATION.name());
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
    public Optional<Boolean> userApproved(String token) {
        UserBean usr;
        Optional<UserBean> oUsr;
        Optional<AbstractTokenBean> oTkBean;
        AbstractTokenBean aTkBean;
        ApprobationTokenBean tkBean;
        
        LOGGER.debug("Approving string token {}", token);
        oTkBean = tokenManager.decodeToken(token);
        
        if(oTkBean.isPresent()) {
            aTkBean = oTkBean.get();
            LOGGER.debug("Verifying token {}", aTkBean);
            if(aTkBean.getTokenClass() == ETokenClass.APPROBATION) {
                tkBean = (ApprobationTokenBean) aTkBean;
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
                LOGGER.error("A {} token is used as a {} token!", aTkBean.getTokenClass().name(), ETokenClass.VERIFICATION.name());
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
    public Optional<UserBean> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserBean> getUserById(@NotBlank String id) {
        return userRepo.findById(id);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserBean> getUserByToken(String registrationToken) {
        Optional<UserBean> r;
        Optional<AbstractTokenBean> oTk;
        
        oTk = tokenManager.decodeToken(registrationToken);
        if(oTk.isPresent()) {
            r = userRepo.findById(oTk.get().getIdUser());
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
            if(!oUsr.get().equals(user)) {
                aUser = oUsr.get();
                if(aUser.getRegistered() != null) {
                    // No changes can be made on created, verified and registered timestamps
                    if(aUser.getRegistered().equals(user.getRegistered())
                            && aUser.getVerified().equals(user.getVerified())
                            && aUser.getCreated().equals(user.getCreated())) {
                        // Update
                        userRepo.save(user);
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String recoverPassword(@NotNull @Valid UserBean user) {
        // TODO Auto-generated method stub
        return null;
    }
}
