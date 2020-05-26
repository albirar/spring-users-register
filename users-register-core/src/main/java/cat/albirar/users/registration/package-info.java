/*
 * This file is part of "albirar users-register".
 * 
 * "albirar users-register" is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * "albirar users-register" is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with "albirar users-register" source code. If not,
 * see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
/**
 * <h3>Overview</h3>
 * <p>The registration process can be configured in different ways in order to customize the process.</p>
 * <p>The process begins with the creation of {@link cat.albirar.users.test.models.users.UserBean} on register.</p>
 * <p>Depending on configuration, the process can be:
 * <ol>
 *    <li>Straight to approve ({@link cat.albirar.users.test.verification.EVerificationProcess#NONE})</li>
 *    <li>Verification of indicated channel owner to achieve the approbation ({@link cat.albirar.users.test.verification.EVerificationProcess#ONE_STEP})</li>
 *    <li>A combination of two steps ({@link cat.albirar.users.test.verification.EVerificationProcess#TWO_STEP})
 *       <ul>
 *          <li>Verification of channel owner</li>
 *          <li>Supervisor approbation</li>
 *       </ul>
 *    </li>
 * </ol>
 * <p>The <i>verification of channel owner</i> process send a message (email or phone message) to the indicated channel on registration. The message contains a special link
 * to verify that the registration owner is the owner of the indicated channel associated with the user.</p>
 * <p>The <i>supervisor approbation</i> is the last step in {@link cat.albirar.users.test.verification.EVerificationProcess#TWO_STEP 2 step registration verification process}.
 * This process send a message to the TODO Pending of define the supervisor selection
 * <h3>State of a user</h3>
 * <p>The states of a user in registry are:
 * <ul>
 * <li>Created</li>
 * <li>Verified</li>
 * <li>Registered</li>
 * <li>Enabled</li>
 * <li>Disabled</li>
 * <li>Expired</li>
 * </ul>
 */
package cat.albirar.users.registration;