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
package cat.albirar.users.repos;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.validation.annotation.Validated;

import cat.albirar.users.models.communications.CommunicationChannel;
import cat.albirar.users.models.users.UserBean;

/**
 * Repository of {@link UserBean}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@NoRepositoryBean
@Validated
public interface IUserRepo {
    /**
     * Returns the number of entities available.
     * @return the number of entities.
     */
    public long count();
    /**
     * Get all users from repo.
     */
    public List<UserBean> findAll();
    /**
     * Check if user with the indicated id exists.
     * @param id The id, required
     * @return true if exists and false if not
     */
    public boolean existsById(@NotBlank String id);
    /**
     * Find the user associated with the indicated {@code id}.
     * @param id The id
     * @return The {@link UserBean} or {@link Optional#empty()} if not found
     */
    public Optional<UserBean> findById(@NotBlank String id);
    /**
     * Check if user with the indicated name exists.
     * @param username The username, required
     * @return true if exists and false if not
     */
    public boolean existsByUsername(@NotBlank String username);
    /**
     * Gets user by username.
     * @param username The username, required
     * @return The {@link UserBean user} or {@link Optional#empty()} if user with {@code username} doesn't exist
     */
    public Optional<UserBean> findByUsername(@NotBlank String username);
    /**
     * Check if a {@link UserBean user} exists with the indicated {@code preferredChannel}.
     * @param preferredChannel The preferred channel sample to search for
     * @return true if exists and false if not
     */
    public boolean existsByPreferredChannel(@NotNull @Valid CommunicationChannel preferredChannel);
    /**
     * Check if a {@link UserBean user} exists with the indicated {@code secondaryChannel}.
     * @param secondaryChannel The secondary channel sample to search for
     * @return true if exists and false if not
     */
    public boolean existsBySecondaryChannel(@NotNull @Valid CommunicationChannel secondaryChannel);
    /**
     * Save (create or update) the indicated {@code user}.
     * <p>If {@link UserBean#getId()} is null or empty or blank or is informed but no users exists with this id, then its a <b>creating operation</b></p>
     * <p>Else its an <b>updating operation</b></p>
     * @param user The user
     * @return The saved user (if created, the {@link UserBean#getId()} is informed with the new id)
     * @throws DataIntegrityViolationException If the indicated {@link UserBean#getUsername()} or {@link UserBean#getPreferredChannel()} exists on registry associated with another account
     */
    public UserBean save(@NotNull @Valid UserBean user);
}
