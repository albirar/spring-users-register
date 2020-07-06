/*
 * This file is part of "albirar users-register-core".
 * 
 * "albirar users-register-core" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "albirar users-register-core" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "albirar users-register-core" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.models.tokens;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * The abstract root for all token classes.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@SuperBuilder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public abstract class AbstractTokenBean implements Serializable {
    private static final long serialVersionUID = 2450832153463835060L;
    
    @NotBlank
    @Setter(onParam_ = {@NotBlank})
    private String tokenId;
    @NotNull
    @Setter(onParam_ = {@NotNull})
    private LocalDateTime issued;
    @NotNull
    @Setter(onParam_ = {@NotNull})
    private LocalDateTime expire;

    @NotBlank
    @Setter(onParam_ = {@NotBlank})
    private String idUser;
    @NotBlank
    @Setter(onParam_ = {@NotBlank})
    private String username;
    @NotNull
    @Setter(onParam_ = {@NotNull})
    private Locale locale;
    
    /**
     * The class name for this kind of token.
     * Enable to discover how to decode this token
     * @return The token class name
     */
    public abstract ETokenClass getTokenClass();
}
