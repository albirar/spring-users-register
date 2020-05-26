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
package cat.albirar.users.models.auth;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Authorization register for application.
 * Can be derived for specific roles.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class AuthorizationBean implements GrantedAuthority, Serializable, Comparable<AuthorizationBean> {
    private static final long serialVersionUID = 1726612662095845083L;
    
    /**
     * The authority name for this authorization.
     * @param authority the name
     * @return The name
     */
    @Setter(onParam_ = { @NotBlank })
    @Default
    private String authority = ERole.User.name();
    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(AuthorizationBean o) {
        if(authority == null && o.getAuthority() == null) {
            return 0;
        }
        if(authority == null) {
            return -1;
        }
        if(o.getAuthority() == null) {
            return 1;
        }
        return authority.compareTo(o.getAuthority());
    }
}
