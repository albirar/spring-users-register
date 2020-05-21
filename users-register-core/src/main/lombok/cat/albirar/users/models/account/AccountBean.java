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
package cat.albirar.users.models.account;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Account data.
 * @author Octavi Fornés &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@SuperBuilder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
@Persistent
public class AccountBean implements Serializable {
	private static final long serialVersionUID = 2501602060005841732L;
	/**
	 * Unique id for this account.
	 * @param id The unique id
	 * @return The unique id or null if this bean is not persisted or was not read
	 */
	@Id
    @Setter(onParam_ = { @NotBlank })
	private String id;
	/**
	 * Unique name of this account.
	 * @param name The name, required
	 * @return The name
	 */
    @Setter(onParam_ = { @NotBlank })
    @NotBlank
	private String name;
    /**
     * Flag to enable or disable this account.
     * If account is disabled, any operation on this account will be prevented.
     * @param enabled true for enable account and false for disable
     * @return true for enable account and false for disable
     */
    @Default
	private boolean enabled = true;
}
