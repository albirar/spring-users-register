/*
 * This file is part of "albirar users-register-mongodb".
 * 
 * "albirar users-register-mongodb" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "albirar users-register-mongodb" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "albirar users-register-mongodb" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.repos.mongodb;

import org.springframework.data.repository.RepositoryDefinition;

import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.repos.IUserRepo;

/**
 * The repository for {@link UserBean} backed by mongodb.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@RepositoryDefinition(domainClass = UserBean.class, idClass = String.class)
public interface IUserMongoRepo extends IUserRepo {
    public void deleteAll();
}
