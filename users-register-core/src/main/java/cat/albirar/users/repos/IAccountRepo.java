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
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.validation.annotation.Validated;

import cat.albirar.users.models.account.AccountBean;

/**
 * {@link AccountBean} repository.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@NoRepositoryBean
@Validated
public interface IAccountRepo {
    /**
     * Find all registered users.
     */
    public List<AccountBean> findAll();
    /**
     * Find account by {@code id}.
     * @param id The required id
     * @return The account or {@link Optional#empty()} if no account with the indicated {@code id} was found
     */
    public Optional<AccountBean> findById(@NotBlank String id);
    /**
     * Find account by {@code name}.
     * @param name the name, required
     * @return The account or {@link Optional#empty()} if no account with the indicated {@code name} was found
     */
    public Optional<AccountBean> findByName(@NotBlank String name);
	/**
	 * Create or update the indicated {@code account}.
	 * Operation discrimination:
	 * <ul>
     * <li>If {@link AccountBean#getId()} is null or empty or blank, is a <b>creation operation</b></li>
     * <li>Else, is a <b>update operation</b></li>
	 * </ul>
	 * @return The saved account
	 * @throws DataIntegrityViolationException If the indicated {@link AccountBean#getName()} exists on registry associated with another account
	 * @throws DataRetrievalFailureException If is an update operation and no object with the indicated id is found
	 */
	public AccountBean save(@NotNull @Valid AccountBean account);
	/**
	 * Count all {@link AccountBean} on register.
	 * @return The total count
	 */
	public long count();
}
