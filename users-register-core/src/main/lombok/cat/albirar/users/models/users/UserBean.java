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
package cat.albirar.users.models.users;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import cat.albirar.users.models.account.AccountBean;
import cat.albirar.users.models.auth.AuthorizationBean;
import cat.albirar.users.models.communications.CommunicationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A user in registry.
 * A user can be associated with none, one or more {@link AccountBean}.
 * 
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@SuperBuilder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
@Persistent
public class UserBean implements UserDetails {
    private static final long serialVersionUID = 788869657467918808L;
    
    /**
	 * Unique identifier of user, for internal and extensions use.
	 * @return L'id, or null (or empty or blank) if user are not persisted nor read yet 
	 * @param id L'id
	 */
	@Id
    @Setter(onParam_ = { @NotBlank })
	private String id;
	/**
	 * User name used, unique in register.
	 * Can be used for log-in or, alternatively {@link #getEmail()}
	 * @return The user name
	 * @param username The user name, required
	 */
    @Setter(onParam_ = { @NotBlank })
	@NotBlank
	private String username;
    /**
     * Preferred channel for communications, as 2 step verification or password recovery.
     * @return The preferred channel for communications
     * @param preferredChannel The preferred channel for communications, required
     */
    @Setter(onParam_ = { @NotNull })
    @NotNull
    @Valid
    private CommunicationChannel preferredChannel;
    /**
     * Secondary channel for communications, as 2 step verification or password recovery.
     * @return The secondary channel for communications
     * @param channel The secondary channel for communications
     */
    private CommunicationChannel secondaryChannel;
    /**
     * Password of user, in hex encrypted, cannot be read.
     * @return The encrypted password
     * @param password The encrypted password
     */
	private String password;
    /**
     * Timestamp of creation on register.
     */
    @Setter(onParam_ = { @NotNull })
    @CreatedDate
    private LocalDateTime created;
    /**
     * Timestamp of verification by {@link #getPreferredChannel() channel} owner.
     * Verified timestamp is null until the owner of {@link #getPreferredChannel()} verify identity with link
     */
    @Default
    private LocalDateTime verified = null;
    /**
     * Timestamp of registration.
     * Registered timestamp is null until the process is completed.
     */
    @Default
    private LocalDateTime registered = null;
	/**
	 * Date for user log-in expiration.
	 * @return The date of expiration, or null if no expiration was assigned
	 * @param expire The date of expiration, or null if no expiration was assigned
	 */
	@Getter(onMethod_ = { @JsonDeserialize(using = LocalDateDeserializer.class)})
    @Default
	private LocalDate expire = null;
	/**
	 * Locked date for avoid user log-in.
	 * @return A date before or equals to today for user log-in locked or a date in the future or null for unlocked.
	 * @param locked A date before or equals to today for user log-in locked or a date in the future or null for unlocked.
	 */
    @Getter(onMethod_ = { @JsonDeserialize(using = LocalDateDeserializer.class)})
    @Default
	private LocalDate locked = null;
    /**
     * Date for user credentials expiration.
     * If user credentials are expired, user cannot do log-in.
     * @return The date of expiration, or null if no expiration was assigned
     * @param expireCredentials The date of expiration, or null if no expiration was assigned
     */
    @Getter(onMethod_ = { @JsonDeserialize(using = LocalDateDeserializer.class)})
    @Default
	private LocalDate expireCredentials = null;
    /**
     * Indicate if user are enabled (true) or not (false); a disabled user cannot do log-in.
     */
    @Default
    private boolean enabled = true;
    /**
     * The authorities list for this user.
     */
    @Default
    private List<AuthorizationBean> authorities = new ArrayList<>();
    
	@JsonIgnore
	@Override
	public boolean isAccountNonExpired() {
		return (expire == null || expire.isAfter(LocalDate.now()));
	}

	@JsonIgnore
	@Override
	public boolean isAccountNonLocked() {
		return (locked == null || locked.isAfter(LocalDate.now()));
	}

	@JsonIgnore
	@Override
	public boolean isCredentialsNonExpired() {
		return (expireCredentials == null || expireCredentials.isAfter(LocalDate.now()));
	}
}
