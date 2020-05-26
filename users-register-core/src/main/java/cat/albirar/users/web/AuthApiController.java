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
package cat.albirar.users.web;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cat.albirar.users.models.tokens.ApprobationTokenBean;
import cat.albirar.users.models.tokens.RecoverPasswordTokenBean;
import cat.albirar.users.models.tokens.VerificationTokenBean;
import cat.albirar.users.models.web.ChangePasswordBean;
import cat.albirar.users.models.web.ProcessResultBean;
import cat.albirar.users.models.web.ResultBean;
import cat.albirar.users.registration.IRegistrationService;
import cat.albirar.users.verification.EVerificationProcess;
import cat.albirar.users.verification.ITokenManager;

/**
 * The controller to dispatch requests for verification, approbation, recover password, etc.
 * The token should to be informed at HEADER {@value AuthApiController#HEADER_PARAM_TOKEN}
 * 
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@RestController
@Validated
public class AuthApiController {
    public static final String URL_ROOT = "/api/auth/1.0";
    public static final String PATH_VAR_TOKEN = "token";
    /**
     * URL for verification.
     */
    public static final String URL_VERIFICATION = URL_ROOT + "/verification";
    /**
     * URL template for verification.
     */
    public static final String URL_TEMPLATE_VERIFICATION = URL_VERIFICATION + "/{" + PATH_VAR_TOKEN + "}";
    /**
     * URL for approbation.
     */
    public static final String URL_APPROBATION = URL_ROOT + "/approbation";
    /**
     * URL template for approbation.
     */
    public static final String URL_TEMPLATE_APPROBATION = URL_APPROBATION + "/{" + PATH_VAR_TOKEN + "}";
    /**
     * URL for change password.
     */
    public static final String URL_CHANGE_PASSWORD = URL_ROOT + "/password";

    @Autowired
    private ITokenManager tokenManager;
    
    @Autowired
    private IRegistrationService registrationService;
    /**
     * The verification end-point.
     * @param strToken The token to verify
     * @return The result of verification
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if token is invalid or expired
     * @throws ResponseStatusException with {@link HttpStatus#NOT_FOUND} if user in token is not found
     */
    @GetMapping(path = AuthApiController.URL_TEMPLATE_VERIFICATION, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ProcessResultBean verifyToken(@PathVariable(PATH_VAR_TOKEN) @NotBlank String strToken) {
        Optional<Boolean> result;
        VerificationTokenBean vtk;
        Optional<VerificationTokenBean> oVtk;

        oVtk = tokenManager.decodeToken(VerificationTokenBean.class, strToken);
        if(oVtk.isPresent()) {
            vtk = oVtk.get();
            result = registrationService.verifyUser(strToken);
            if(result.isPresent()) {
                return ProcessResultBean.builder()
                        .tokenId(vtk.getTokenId())
                        .date(LocalDateTime.now())
                        .result(result.get().booleanValue())
                        .lastStep(vtk.getProcess() == EVerificationProcess.ONE_STEP)
                        .build();
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User from token not found!");
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token!");
    }
    /**
     * The approbation end-point.
     * @param strToken The token to verify
     * @return The result of approbation
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if token is invalid or expired
     * @throws ResponseStatusException with {@link HttpStatus#NOT_FOUND} if user in token is not found
     */
    @GetMapping(path = AuthApiController.URL_TEMPLATE_APPROBATION, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ProcessResultBean approveToken(@PathVariable(PATH_VAR_TOKEN) @NotBlank String strToken) {
        Optional<Boolean> result;
        ApprobationTokenBean atk;
        Optional<ApprobationTokenBean> oAtk;
        
        oAtk = tokenManager.decodeToken(ApprobationTokenBean.class, strToken);
        if(oAtk.isPresent()) {
            atk = oAtk.get();
            result = registrationService.approveUser(strToken);
            if(result.isPresent()) {
                return ProcessResultBean.builder()
                        .tokenId(atk.getTokenId())
                        .date(LocalDateTime.now())
                        .result(result.get().booleanValue())
                        .lastStep(true) // in 2 steps process this is the last step 
                        .build();
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User from token not found!");
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token!");
    }
    /**
     * The change password end-point.
     * @param bean The bean with the information to change password.
     * @return The result, with {@link ResultBean#isResult()} true if the users exists and state is verified or registered and password was changed, false if state of user is created and password was not changed 
     * @throws ResponseStatusException with {@link HttpStatus#BAD_REQUEST} if token is invalid or expired
     * @throws ResponseStatusException with {@link HttpStatus#NOT_FOUND} if user in token is not found
     */
    @PutMapping(path = AuthApiController.URL_CHANGE_PASSWORD, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResultBean changePasswordToken(@RequestBody @NotNull @Valid ChangePasswordBean bean) {
        Optional<RecoverPasswordTokenBean> otk;
        Optional<Boolean> resp;
        
        otk = tokenManager.decodeToken(RecoverPasswordTokenBean.class, bean.getToken());
        if(otk.isPresent()) {
            resp = registrationService.recoverPassword(bean.getToken(), bean.getPassword());
            if(resp.isPresent()) {
                return ResultBean.builder()
                        .tokenId(otk.get().getTokenId())
                        .date(LocalDateTime.now())
                        .result(resp.get())
                        .build()
                        ;
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User from token not found!");
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token!");
    }
}
