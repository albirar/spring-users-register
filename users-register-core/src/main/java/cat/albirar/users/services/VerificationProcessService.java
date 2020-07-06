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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cat.albirar.communications.messages.models.MessageBean;
import cat.albirar.communications.services.ICommunicationService;
import cat.albirar.template.engine.EContentType;
import cat.albirar.template.engine.models.TemplateDefinitionBean;
import cat.albirar.template.engine.models.TemplateInstanceBean;
import cat.albirar.template.engine.service.ITemplateEngine;
import cat.albirar.users.config.PropertiesCore;
import cat.albirar.users.models.tokens.ApprobationTokenBean;
import cat.albirar.users.models.tokens.RecoverPasswordTokenBean;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.verification.ProcessBean;
import cat.albirar.users.verification.ITokenManager;
import cat.albirar.users.verification.IVerificationProcessService;

/**
 * The verification process service.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Service
@Validated
public class VerificationProcessService implements IVerificationProcessService {
    
    @Value("${" + PropertiesCore.TEMPLATE_VERIFICATION_PROPERTY + "}")
    private String templateVerificationResourcePath;
    @Value("${" + PropertiesCore.TEMPLATE_APPROBATION_PROPERTY + "}")
    private String templateApprobationResourcePath;
    @Value("${" + PropertiesCore.TEMPLATE_RECOVER_PROPERTY + "}")
    private String templateRecoverResourcePath;
    
    @Value("${" + PropertiesCore.TEMPLATE_RESOURCES_PROPERTY + "}")
    private MessageSource resources;
    
    @Autowired
    private ITemplateEngine templateEngine;
    
    @Autowired
    private ICommunicationService communicationService;
    
    @Autowired
    private ITokenManager tokenManager;
    
    private TemplateDefinitionBean templateHtmlVerification;
    private TemplateDefinitionBean templateTextVerification;
    private TemplateDefinitionBean templateHtmlApprobation;
    private TemplateDefinitionBean templateTextApprobation;
    private TemplateDefinitionBean templateHtmlRecover;
    private TemplateDefinitionBean templateTextRecover;
    
    private Map<String, Object> globalVariables;
    
    /**
     * Initialize some properties.
     */
    @PostConstruct
    public void init(@Autowired ConfigurableEnvironment env) {
        List<EnumerablePropertySource<?>> propSrc;
        
        templateHtmlVerification = TemplateDefinitionBean.builder()
                .contentType(EContentType.HTML)
                .name("verification.html")
                .template(templateVerificationResourcePath + ".html")
                .build()
                ;
        templateTextVerification = TemplateDefinitionBean.builder()
                .contentType(EContentType.TEXT_PLAIN)
                .name("verification.txt")
                .template(templateVerificationResourcePath + ".txt")
                .build()
                ;
        templateHtmlApprobation = TemplateDefinitionBean.builder()
                .contentType(EContentType.HTML)
                .name("approbation.html")
                .template(templateApprobationResourcePath + ".html")
                .build()
                ;
        templateTextApprobation = TemplateDefinitionBean.builder()
                .contentType(EContentType.TEXT_PLAIN)
                .name("approbation.txt")
                .template(templateApprobationResourcePath + ".txt")
                .build()
                ;
        templateHtmlRecover = TemplateDefinitionBean.builder()
                .contentType(EContentType.HTML)
                .name("recover-password.html")
                .template(templateRecoverResourcePath + ".html")
                .build()
                ;
        templateTextRecover = TemplateDefinitionBean.builder()
                .contentType(EContentType.TEXT_PLAIN)
                .name("recover-password.txt")
                .template(templateRecoverResourcePath + ".txt")
                .build()
                ;
        globalVariables = new TreeMap<>();
        propSrc = env.getPropertySources().stream()
                .filter(ps -> EnumerablePropertySource.class.isAssignableFrom(ps.getClass()))
                .map(ps -> (EnumerablePropertySource<?>) ps)
                .collect(Collectors.toList())
                ;
        for(EnumerablePropertySource<?> eps : propSrc) {
            Arrays.stream(eps.getPropertyNames())
            .filter(name -> name.startsWith(PropertiesCore.TEMPLATES_GLOBAL_VARIABLES_PREFIX_DOT))
            .forEach(name -> globalVariables.put(name.substring(PropertiesCore.TEMPLATES_GLOBAL_VARIABLES_PREFIX_DOT.length()), env.getProperty(name)))
            ;
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public String startVerifyProcess(ProcessBean verificationProcess) {
        TemplateDefinitionBean td;
        VerificationTokenBean vtk;
        String rendered;
        EContentType ct;
        
        if(verificationProcess.getDestination().getChannelBean().getChannelType().isHtmlBodyChannel()) {
            td = templateHtmlVerification;
            ct = EContentType.HTML;
        } else {
            td = templateTextVerification;
            ct = EContentType.TEXT_PLAIN;
        }
        vtk = tokenManager.decodeToken(VerificationTokenBean.class, verificationProcess.getToken()).get();
        
        rendered = templateEngine.renderTemplate(TemplateInstanceBean.buildInstance(td)
                .locale(vtk.getLocale())
                .variables(globalVariables)
                .messages(resources)
                .build()
                );
        return communicationService.pushMessage(MessageBean.builder()
                .receiver(verificationProcess.getDestination().toBuilder().build())
                .sender(verificationProcess.getSender().toBuilder().build())
                .body(rendered)
                .bodyType(ct)
                .title(verificationProcess.getTitle())
                .build()
                );
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public String startApproveProcess(ProcessBean approbationProcess) {
        TemplateDefinitionBean td;
        ApprobationTokenBean atk;
        String rendered;
        EContentType ct;
        
        if(approbationProcess.getDestination().getChannelBean().getChannelType().isHtmlBodyChannel()) {
            td = templateHtmlApprobation;
            ct = EContentType.HTML;
        } else {
            td = templateTextApprobation;
            ct = EContentType.TEXT_PLAIN;
        }
        atk = tokenManager.decodeToken(ApprobationTokenBean.class, approbationProcess.getToken()).get();
        
        rendered = templateEngine.renderTemplate(TemplateInstanceBean.buildInstance(td)
                .locale(atk.getLocale())
                .variables(globalVariables)
                .messages(resources)
                .build()
                );
        return communicationService.pushMessage(MessageBean.builder()
                .receiver(approbationProcess.getDestination().toBuilder().build())
                .sender(approbationProcess.getSender().toBuilder().build())
                .body(rendered)
                .bodyType(ct)
                .title(approbationProcess.getTitle())
                .build()
                );
    }
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public String startRecoverPasswordProcess(ProcessBean recoverPasswordProcess) {
        TemplateDefinitionBean td;
        RecoverPasswordTokenBean rptk;
        String rendered;
        EContentType ct;
        
        if(recoverPasswordProcess.getDestination().getChannelBean().getChannelType().isHtmlBodyChannel()) {
            td = templateHtmlRecover;
            ct = EContentType.HTML;
        } else {
            td = templateTextRecover;
            ct = EContentType.TEXT_PLAIN;
        }
        rptk = tokenManager.decodeToken(RecoverPasswordTokenBean.class, recoverPasswordProcess.getToken()).get();
        
        rendered = templateEngine.renderTemplate(TemplateInstanceBean.buildInstance(td)
                .locale(rptk.getLocale())
                .variables(globalVariables)
                .messages(resources)
                .build()
                );
        return communicationService.pushMessage(MessageBean.builder()
                .receiver(recoverPasswordProcess.getDestination().toBuilder().build())
                .sender(recoverPasswordProcess.getSender().toBuilder().build())
                .body(rendered)
                .bodyType(ct)
                .title(recoverPasswordProcess.getTitle())
                .build()
                );
    }
}
