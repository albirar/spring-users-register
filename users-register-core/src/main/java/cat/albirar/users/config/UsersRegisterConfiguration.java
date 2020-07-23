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
package cat.albirar.users.config;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import cat.albirar.communications.channels.models.ECommunicationChannelType;
import cat.albirar.communications.channels.models.LocalizableAttributesCommunicationChannelBean;
import cat.albirar.communications.channels.models.ContactBean;
import cat.albirar.users.registration.IRegistrationService;
import cat.albirar.users.services.SpringSecurityUserService;
import cat.albirar.users.services.TokenManager;
import cat.albirar.users.utils.LocaleUtils;
import cat.albirar.users.verification.IVerificationProcessService;
import cat.albirar.users.web.AuthApiController;

/**
 * Users registry abstract configuration.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Configuration
@PropertySource("classpath:/cat/albirar/users/register/users-register.properties")
@ComponentScan(basePackageClasses = {IRegistrationService.class, IVerificationProcessService.class, TokenManager.class, SpringSecurityUserService.class, AuthApiController.class})
@EnableWebMvc
public class UsersRegisterConfiguration {

    /**
     * Default sender.
     * @param senderName The display name
     * @param senderEmail The email
     * @param senderLocale The locale
     * @return
     */
    @Bean
    public ContactBean defaultSender(@Value("${" + PropertiesCore.SENDER_DISPLAY_NAME + "}") String senderName,
        @Value("${" + PropertiesCore.SENDER_LOCALE + "}") String senderEmail,
        @Value("${" + PropertiesCore.SENDER_LOCALE + "}") String senderLocale) {
        
        return ContactBean.builder()
                .displayName(senderName)
                .channelBean(LocalizableAttributesCommunicationChannelBean.builder()
                        .channelType(ECommunicationChannelType.EMAIL)
                        .channelId(senderEmail)
                        .locale(LocaleUtils.stringToLocale(senderLocale))
                        .build()
                        )
                .preferredLocale(LocaleUtils.stringToLocale(senderLocale))
                .build()
                ;
    }
    
    /**
     * Password encoder by default.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(BCryptVersion.$2Y, 12);
        return bCryptPasswordEncoder;
    }
    /**
     * The symmetric key to sign the jws token.
     */
    @Bean("jwsSecretKey")
    public SecretKey jwsSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator;
        
        keyGenerator = KeyGenerator.getInstance("HmacSHA256");
        keyGenerator.init(256, new SecureRandom());
        return keyGenerator.generateKey();
    }
    
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
    
    @Bean
    public MethodValidationPostProcessor validationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
