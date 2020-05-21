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

import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.test.UsersRegisterAbstractDataTest;
import cat.albirar.users.test.UsersRegisterTests;
import cat.albirar.users.verification.EVerificationProcess;
import cat.albirar.users.verification.ITokenManager;
import cat.albirar.users.web.AuthApiController;

/**
 * Test for {@link AuthApiController}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class AuthApiControllerTest extends UsersRegisterTests {

    @Autowired
    protected ITokenManager tokenManager;
    
    @Autowired
    private WebApplicationContext wac;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }
    /**
     * Build a default token bean as:
     * <ul>
     * <li>{@link VerificationTokenBean#getIdUser()} with {@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER}</li>
     * <li>{@link VerificationTokenBean#getUsername()} with {@link UsersRegisterAbstractDataTest#SAMPLE_REGISTERED_USER}</li>
     * <li>{@link VerificationTokenBean#getIssued()} with 5 days ago</li>
     * <li>{@link VerificationTokenBean#getExpire()} with today plus 5 days</li>
     * <li>{@link VerificationTokenBean#getProcess()} with {@link EVerificationProcess#NONE}</li>
     * </ul>
     * @return
     */
    private VerificationTokenBean buildToken() {
        LocalDateTime ldt;
        
        ldt = LocalDateTime.now();
        return VerificationTokenBean.builder()
                .tokenId(SAMPLE_ID)
                .idUser(SAMPLE_REGISTERED_USER.getId())
                .username(SAMPLE_REGISTERED_USER.getUsername())
                .issued(ldt.minusDays(5))
                .expire(LocalDateTime.now().plusDays(5))
                .process(EVerificationProcess.NONE)
                .build();
    }
    private VerificationTokenBean buildTokenOneStep() {
        return buildToken().toBuilder().process(EVerificationProcess.ONE_STEP).build();
    }
    private VerificationTokenBean buildTokenTwoStep() {
        return buildToken().toBuilder().process(EVerificationProcess.TWO_STEP).build();
    }

    @Test
    public void testVerifyInvalidToken() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, DUMMY_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }
    
    @Test
    public void testVerifyExpiredToken() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildToken().toBuilder()
                .expire(LocalDateTime.now().minusDays(1)).build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.PRECONDITION_FAILED.value()));
    }
    
    @Test
    public void testVerifyNoneStepVerification() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildToken()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.PRECONDITION_REQUIRED.value()));
    }
    
    /**
     * Test {@link AuthApiController#URL_TEMPLATE_VERIFICATION} for
     * <ul>
     * <li>User not found {@link UsersRegisterAbstractDataTest#DUMMY_ID}</li>
     * </ul>
     */
    @Test
    public void testVerifyUserNotFound() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildTokenOneStep().toBuilder()
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
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildTokenOneStep().toBuilder()
                .idUser(SAMPLE_VERIFIED_USER.getId())
                .username(SAMPLE_VERIFIED_USER.getUsername())
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
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildTokenTwoStep().toBuilder()
                .idUser(SAMPLE_VERIFIED_USER.getId())
                .username(SAMPLE_VERIFIED_USER.getUsername())
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
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildTokenOneStep().toBuilder()
                .idUser(SAMPLE_CREATED_USER.getId())
                .username(SAMPLE_CREATED_USER.getUsername())
                .build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.OK.value()))
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
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_VERIFICATION, tokenManager.encodeToken(buildTokenTwoStep().toBuilder()
                .idUser(SAMPLE_CREATED_USER.getId())
                .username(SAMPLE_CREATED_USER.getUsername())
                .build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.OK.value()))
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
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for expired token.
     */
    @Test
    public void testApproveExpiredToken() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildTokenTwoStep().toBuilder()
                .idUser(SAMPLE_VERIFIED_USER.getId())
                .username(SAMPLE_VERIFIED_USER.getUsername())
                .expire(LocalDateTime.now().minusDays(1))
                .build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.PRECONDITION_FAILED.value()));
    }

    /**
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for none step.
     */
    @Test
    public void testApproveVerifiedNoneStep() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildTokenTwoStep().toBuilder()
                .idUser(SAMPLE_VERIFIED_USER.getId())
                .username(SAMPLE_VERIFIED_USER.getUsername())
                .process(EVerificationProcess.NONE)
                .build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.PRECONDITION_REQUIRED.value()));
    }

    /**
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for one step and.
     */
    @Test
    public void testApproveVerifiedOneStep() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildTokenTwoStep().toBuilder()
                .idUser(SAMPLE_VERIFIED_USER.getId())
                .username(SAMPLE_VERIFIED_USER.getUsername())
                .process(EVerificationProcess.ONE_STEP)
                .build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.PRECONDITION_REQUIRED.value()));
    }

    /**
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for two step and:
     * <ul>
     * <li>User verified {@link UsersRegisterAbstractDataTest#DUMMY_ID}</li>
     * </ul>
     */
    @Test
    public void testApproveUserNotFound() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildTokenTwoStep().toBuilder()
                .idUser(DUMMY_ID)
                .username(DUMMY_USERNAME)
                .build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Test {@link AuthApiController#URL_TEMPLATE_APPROBATION} for two step and:
     * <ul>
     * <li>User verified {@link UsersRegisterAbstractDataTest#SAMPLE_CREATED_USER}</li>
     * </ul>
     */
    @Test
    public void testApproveUserNotVerified() throws Exception {
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildTokenTwoStep().toBuilder()
                .idUser(SAMPLE_CREATED_USER.getId())
                .username(SAMPLE_CREATED_USER.getUsername())
                .build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.OK.value()))
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
        mockMvc.perform(get(AuthApiController.URL_TEMPLATE_APPROBATION, tokenManager.encodeToken(buildTokenTwoStep().toBuilder()
                .idUser(SAMPLE_VERIFIED_USER.getId())
                .username(SAMPLE_VERIFIED_USER.getUsername())
                .build()))
                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.result").value(true))
                .andExpect(jsonPath("$.lastStep").value(true));
    }
}
