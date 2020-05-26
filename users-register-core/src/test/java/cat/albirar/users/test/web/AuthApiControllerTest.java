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
package cat.albirar.users.test.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cat.albirar.users.models.tokens.ApprobationTokenBean;
import cat.albirar.users.models.tokens.RecoverPasswordTokenBean;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.web.ChangePasswordBean;
import cat.albirar.users.test.UsersRegisterAbstractDataTest;
import cat.albirar.users.test.UsersRegisterTests;
import cat.albirar.users.verification.EVerificationProcess;
import cat.albirar.users.web.AuthApiController;

/**
 * Test for {@link AuthApiController}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class AuthApiControllerTest extends UsersRegisterTests {

    @Autowired
    private WebApplicationContext wac;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }
    /**
     * Build a verification token bean for {@link UsersRegisterAbstractDataTest#SAMPLE_CREATED_USER} and {@link EVerificationProcess#ONE_STEP}
     * @return The verification token bean
     */
    private VerificationTokenBean buildVerificationTokenBean() {
        return tokenManager.generateVerificationTokenBean(SAMPLE_CREATED_USER, EVerificationProcess.ONE_STEP).get();
    }
    /**
     * Build a verification token bean for {@link EVerificationProcess#TWO_STEP}
     * @return The verification token bean
     * @see #buildVerificationTokenBean()
     */
    private VerificationTokenBean buildVerificationTokenBeanTwoStep() {
        return buildVerificationTokenBean().toBuilder().process(EVerificationProcess.TWO_STEP).build();
    }
    /**
     * Build a verification token bean for {@link UsersRegisterAbstractDataTest#SAMPLE_VERIFIED_USER} and {@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER} as approver.
     * @return The verification token bean
     */
    private ApprobationTokenBean buildApprobationTokenBean() {
        return tokenManager.generateApprobationTokenBean(SAMPLE_VERIFIED_USER, SAMPLE_REGISTERED_USER).get();
    }
    
    private RecoverPasswordTokenBean buildRecoverPasswordTokenBean() {
        return tokenManager.generateRecoverPasswordTokenBean(SAMPLE_REGISTERED_USER, true).get();
    }
    
    private String buildAsJsonString(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }
    @Test
    public void testVerifyInvalidToken() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, DUMMY_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }
    
    @Test
    public void testVerifyExpiredToken() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildVerificationTokenBean().toBuilder()
                    .expire(LocalDateTime.now().minusDays(1)).build()))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }
    
    /**
     * Test {@link AuthApiController#URL_TEMPLATE_VERIFICATION} for
     * <ul>
     * <li>User not found {@link UsersRegisterAbstractDataTest#DUMMY_ID}</li>
     * </ul>
     */
    @Test
    public void testVerifyUserNotFound() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildVerificationTokenBean().toBuilder()
                .idUser(DUMMY_ID)
                .username(DUMMY_USERNAME)
                .build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }
    
    /**
     * Test {@link AuthApiController#URL_TEMPLATE_VERIFICATION} for one step and:
     * <ul>
     * <li>User verified {@link UsersRegisterAbstractDataTest#SAMPLE_VERIFIED_USER}</li>
     * </ul>
     */
    @Test
    public void testVerifyUserRegisteredOneStep() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildVerificationTokenBean().toBuilder()
                .idUser(SAMPLE_REGISTERED_USER.getId())
                .username(SAMPLE_REGISTERED_USER.getUsername())
                .build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.result").value(false))
                .andExpect(jsonPath("$.lastStep").value(true));
    }

    /**
     * Test {@link AuthApiController#URL_TEMPLATE_VERIFICATION} for two step and:
     * <ul>
     * <li>User verified {@link UsersRegisterAbstractDataTest#SAMPLE_VERIFIED_USER}</li>
     * </ul>
     */
    @Test
    public void testVerifyUserRegisteredTwoStep() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildVerificationTokenBeanTwoStep().toBuilder()
                .idUser(SAMPLE_REGISTERED_USER.getId())
                .username(SAMPLE_REGISTERED_USER.getUsername())
                .build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.result").value(false))
                .andExpect(jsonPath("$.lastStep").value(false));
    }

    /**
     * Test {@link AuthApiController#URL_TEMPLATE_VERIFICATION} for one step and:
     * <ul>
     * <li>User verified {@link UsersRegisterAbstractDataTest#SAMPLE_CREATED_USER}</li>
     * </ul>
     */
    @Test
    public void testVerifyUserOneStepOk() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildVerificationTokenBean()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.result").value(true))
                .andExpect(jsonPath("$.lastStep").value(true));;
    }

    /**
     * Test {@link AuthApiController#URL_TEMPLATE_VERIFICATION} for one step and:
     * <ul>
     * <li>User verified {@link UsersRegisterAbstractDataTest#SAMPLE_CREATED_USER}</li>
     * </ul>
     */
    @Test
    public void testVerifyUserTwoStepOk() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildVerificationTokenBeanTwoStep()))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(jsonPath("$.result").value(true))
            .andExpect(jsonPath("$.lastStep").value(false));;
    }

    /**
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for invalid token.
     */
    @Test
    public void testApproveInvalidToken() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, DUMMY_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }
    /**
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for verification token.
     */
    @Test
    public void testApproveInvalidClassToken1() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildVerificationTokenBean()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }
    /**
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for verification token.
     */
    @Test
    public void testApproveInvalidClassToken2() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildVerificationTokenBean()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }
    /**
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for expired token.
     */
    @Test
    public void testApproveExpiredToken() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildApprobationTokenBean().toBuilder()
                    .expire(LocalDateTime.now().minusDays(5)).build()))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for two step and:
     * <ul>
     * <li>User verified {@link UsersRegisterAbstractDataTest#DUMMY_ID}</li>
     * </ul>
     */
    @Test
    public void testApproveUserNotFound() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildApprobationTokenBean().toBuilder()
                .idUser(DUMMY_ID)
                .username(DUMMY_USERNAME)
                .build()))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for two step and:
     * <ul>
     * <li>User verified {@link UsersRegisterAbstractDataTest#SAMPLE_CREATED_USER}</li>
     * </ul>
     */
    @Test
    public void testApproveUserNotVerified() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildApprobationTokenBean().toBuilder()
                .idUser(SAMPLE_CREATED_USER.getId())
                .username(SAMPLE_CREATED_USER.getUsername())
                .build()))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(jsonPath("$.result").value(false))
            .andExpect(jsonPath("$.lastStep").value(true));
    }

    /**
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for two step and:
     * <ul>
     * <li>User verified {@link UsersRegisterAbstractDataTest#SAMPLE_VERIFIED_USER}</li>
     * </ul>
     */
    @Test
    public void testApproveUserVerified() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildApprobationTokenBean()))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(jsonPath("$.result").value(true))
            .andExpect(jsonPath("$.lastStep").value(true));
    }

    @Test
    public void testChangePasswordEmptyContent () throws Exception {
        mockMvc.perform(put(AuthApiController.URL_CHANGE_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }
    
    @Test
    public void testChangePasswordValidationExceptionValues () throws Exception {
        mockMvc.perform(put(AuthApiController.URL_CHANGE_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildAsJsonString(ChangePasswordBean.builder().token(null).password(null).build()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        mockMvc.perform(put(AuthApiController.URL_CHANGE_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildAsJsonString(ChangePasswordBean.builder().token("").password("").build()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        mockMvc.perform(put(AuthApiController.URL_CHANGE_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildAsJsonString(ChangePasswordBean.builder().token(" ").password("  ").build()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void testChangePasswordInvalidToken () throws Exception {
        mockMvc.perform(put(AuthApiController.URL_CHANGE_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildAsJsonString(ChangePasswordBean.builder().token(tokenManager.encodeToken(buildApprobationTokenBean()))
                        .password(PASSWORDS[2]).build()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        mockMvc.perform(put(AuthApiController.URL_CHANGE_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildAsJsonString(ChangePasswordBean.builder().token(tokenManager.encodeToken(buildVerificationTokenBean()))
                        .password(PASSWORDS[2]).build()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void testChangePasswordUserNotFound () throws Exception {
        mockMvc.perform(put(AuthApiController.URL_CHANGE_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildAsJsonString(ChangePasswordBean.builder().token(tokenManager.encodeToken(buildRecoverPasswordTokenBean()
                        .toBuilder().idUser(DUMMY_ID).username(DUMMY_USERNAME).build()))
                        .password(PASSWORDS[2]).build()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void testChangePasswordUserNotRegisteredYet () throws Exception {
        mockMvc.perform(put(AuthApiController.URL_CHANGE_PASSWORD)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildAsJsonString(ChangePasswordBean.builder().token(tokenManager.encodeToken(buildRecoverPasswordTokenBean()
                            .toBuilder().idUser(SAMPLE_VERIFIED_USER.getId()).username(SAMPLE_VERIFIED_USER.getUsername()).build()))
                            .password(PASSWORDS[2]).build()))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.result").value(false));
    }

    @Test
    public void testChangePasswordChangedOK () throws Exception {
        mockMvc.perform(put(AuthApiController.URL_CHANGE_PASSWORD)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildAsJsonString(ChangePasswordBean.builder().token(tokenManager.encodeToken(buildRecoverPasswordTokenBean()))
                            .password(PASSWORDS[2]).build()))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.result").value(true));
    }
}
