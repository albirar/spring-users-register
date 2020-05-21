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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.repos.IUserRepo;

/**
 * The user service to provide search for users.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Component
public class SpringSecurityUserService implements UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringSecurityUserService.class);
    
    @Autowired
    private IUserRepo userRepo;
    /**
     * {@inheritDoc}
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserBean> oUsr;
        
        if(StringUtils.hasText(username)) {
            oUsr = userRepo.findByUsername(username);
            if(!oUsr.isPresent()) {
                // User not found
                LOGGER.error("User with username '{}' not found!", username);
                throw new UsernameNotFoundException(String.format("User with username '%s' not found!", username));
            }
            if(CollectionUtils.isEmpty(oUsr.get().getAuthorities())) {
                LOGGER.error("User with username '{}' has no granted authorities!", username);
                throw new UsernameNotFoundException(String.format("User with username '%s' has no granted authorities!", username));
            }
            LOGGER.info("User with username '{}' exists with id '{}' and authorities %s", username, oUsr.get().getId(), oUsr.get().getAuthorities());
            return oUsr.get();
        }
        throw new UsernameNotFoundException("Username cannot be null or blank string");
    }
}
