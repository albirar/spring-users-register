/*
 * This file is part of "albirar demo".
 * 
 * "albirar demo" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "albirar demo" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "albirar demo" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package com.example.demo;

import static cat.albirar.users.models.communications.ECommunicationChannelType.EMAIL;

import java.security.Principal;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import cat.albirar.users.models.communications.CommunicationChannel;
import cat.albirar.users.models.registration.RegistrationProcessResultBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.registration.IRegistrationService;

/**
 * Demo controller.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Controller
public class DemoController {
    
    @Autowired
    private IRegistrationService registrationService;
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = {"/", "", "/index.html"})
    public String index() {
        return "index";
    }

    @GetMapping(path = "/signin")
    public String signin() {
        return "signin";
    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = {"/signin-ok"})
    public String signinOk() {
        return "redirect:/";
    }

    @GetMapping(path = "/signup")
    public ModelAndView signup() {
        return new ModelAndView("signup");
    }
    @PostMapping(path = "/signup")
    public ModelAndView signupRegister(Model model) {
        ModelAndView mv;
        RegistrationProcessResultBean r;
        
        try {
            r = registrationService.registerUser((String)model.getAttribute("username"), CommunicationChannel.builder().channelType(EMAIL).channelId((String)model.getAttribute("email")).build(), (String)model.getAttribute("password"));
            model.addAttribute("user", r.getUser());
            mv = new ModelAndView("signup-ok", model.asMap());
        } catch(DuplicateKeyException e) {
            model.addAttribute("ERROR", "Username or email is duplicated!");
            mv = new ModelAndView("signup", model.asMap());
        }
        return mv;
    }
    @GetMapping(path = "/signup-ok")
    public ModelAndView signupOk(Model model) {
        model.addAttribute("message", "Registration successful!");
        return new ModelAndView("profile", model.asMap());
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = {"/logout-ok"})
    public String logoutOk() {
        return "redirect:/";
    }

    @GetMapping(path = "/profile")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView profile(Principal principal) {
        ModelAndView mv;
        Optional<UserBean> oUser;
        
        oUser = registrationService.getUserByUsername(principal.getName());
        if(oUser.isPresent()) {
            mv = new ModelAndView("profile");
            mv.addObject("user", oUser.get());
        } else {
            mv = new ModelAndView("index", HttpStatus.FORBIDDEN);
        }
        return mv;
    }

    @PostMapping(path = "/profile")
    public ModelAndView profileUpdate(@ModelAttribute("user") @NotNull @Validated UserBean user) {
        ModelAndView mv;

        mv = new ModelAndView();
        try {
            registrationService.updateUser(user);
            mv.setStatus(HttpStatus.OK);
            mv.setViewName("profile");
            mv.addObject("user", user);
        } catch(DataRetrievalFailureException e) {
//            ERROR If no user was found associated with the indicated user id
        } catch(IllegalStateException e) {
//            ERROR - If the user has not been registered
        } catch(IllegalArgumentException e) {
//            ERROR If the indicated user contains information that cannot be updated
        } catch(DuplicateKeyException e) {
//          ERROR If username, preferred or secondary channel are duplicated
        }
        return mv;
    }
}
