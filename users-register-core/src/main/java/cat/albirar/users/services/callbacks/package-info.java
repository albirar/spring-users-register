/*
 * This file is part of "core-auth".
 * 
 * "core-auth" is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * "core-auth" is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with "albirar users-register" source code. If not, see
 * <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
/**
 * <p>This callback system allow to link specific code on users and accounts operations</p>
 * <p>Operations are divide in two phases: 
 * <ol>
 * <li><code>PRE</code>: allow to verify information and, eventually, avoid to do the operation</li>
 * <li><code>POST</code>: When the operation is done, allow to manage specific data associated with user or account</li>
 * </ol>
 * </p>
 * <p>The <code>PRE</code> callback phase allow to verify validation rules that can indicate that operation should to be aborted. No other operation but verification should to be made in this phase.</p>
 * <p>The <code>POST</code> callback phase is the right place to work with specific data creation, modification or deletion, linked with user or account.</p>
 * <p>If you need to add specific information associated with users, you should to use the {@link cat.albirar.users.test.models.users.UserBean#getId()} in <code>POST</code> phase to establish a link between user and your information.<p>
 * <p>In order to add specific information associated with accounts, you should to use the {@link cat.albirar.users.test.models.account.AccountBean#getId()} in <code>POST</code> phase to establish a link between account and your information.<p>
 * 
 */
package cat.albirar.users.services.callbacks;

