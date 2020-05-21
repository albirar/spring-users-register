/*
 * This file is part of "core-auth".
 * 
 * "core-auth" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "core-auth" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "albirar users-register" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.services.callbacks;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import cat.albirar.users.models.account.AccountBean;
import cat.albirar.users.models.services.callbacks.OperationCheckResult;
import cat.albirar.users.models.users.UserBean;

/**
 * Account operations callback contract.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public interface ICallbackAccountOperations {
    /**
     * Called before creating the {@code newAccount}; it allows to disapprove the creation.
     * @param newAccount The new account, before persist them
     * @return The resulting check, if {@link OperationCheckResult#isApproved()} is true, operation is performed; if false, operation is aborted
     * @see OperationCheckResult#isApproved()
     */
    public OperationCheckResult preCreate(@NotNull AccountBean newAccount);
    /**
     * Called after the {@code newAccount} is created; allow to creating specifics data structures.
     * @param newAccount The new created account, with {@link AccountBean#getId() id} assigned
     */
    public void postCreate(@NotNull AccountBean newAccount);
    /**
     * Called before update the {@code currentAccount}; it allows to disapprove the modification.
     * @param currentAccount The account to be modified, with the current data, without any modification
     * @param modifiedAccount The account with modification data, before persist them
     * @return The resulting check, if {@link OperationCheckResult#isApproved()} is true, operation is performed; if false, operation is aborted
     * @see OperationCheckResult#isApproved()
     */
    public OperationCheckResult preUpdate(@NotNull AccountBean currentAccount, @NotNull AccountBean modifiedAccount);
    /**
     * Called after the {@code account} was modified.
     * @param account The modified and persisted account
     */
    public void postUpdate(@NotNull AccountBean account);
    /**
     * Called before associate one or more {@code users} with an {@code account}.
     * <p><strong>WARNING!!</strong> a call to {@link ICallbackUserOperations#preAssociate(AccountBean, UserBean)} is made for every user in the {@code users} list after this call</p>
     * <p>This operation is made as follows:
     * <ol>
     *     <li>Call to {@link #preAssociate(AccountBean, UserBean...)}
     *         <ul>
     *             <li>If returns {@link OperationCheckResult#isApproved()} is true:
     *                 <ul>
     *                     <li>A call to {@link ICallbackUserOperations#preAssociate(AccountBean, UserBean)} is made for every {@link UserBean user} in {@code users} list</li>
     *                     <li>If all calls was returned {@link OperationCheckResult#isApproved()} true, then:
     *                         <ul>
     *                             <li>Every {@link UserBean user} of the {@code users} list are effectively associated with {@code account}</li>
     *                         </ul>
     *                     </li>
     *                     <li>If any of the calls to {@link ICallbackUserOperations#preAssociate(AccountBean, UserBean)} returns {@link OperationCheckResult#isApproved()} false, operation is aborted.</li>
     *                 </ul>
     *             </li>
     *             <li>If returns {@link OperationCheckResult#isApproved()} is false, then operation is aborted</li>
     *         </ul>
     *     </li>
     *     <li>If operation was not aborted, then:
     *         <ul>
     *             <li>Call to {@link ICallbackUserOperations#postAssociate(AccountBean, UserBean)} for every {@link UserBean user} of the {@code users} list are made</li>
     *             <li>A call to {@link #postAssociate(AccountBean, UserBean...)} is made</li>
     *         </ul>
     *     </li>
     * </ol>
     * @param account The account to associate with {@code users}
     * @param users The users to associate to, already created
     * @return The resulting check, if {@link OperationCheckResult#isApproved()} is true, operation is performed; if false, operation is aborted
     * @see OperationCheckResult#isApproved()
     */
    public OperationCheckResult preAssociate(@NotNull AccountBean account, @NotEmpty UserBean ...users);
    /**
     * Called after {@code users} are associated with the {@code account}.
     * <strong>WARNING!!</strong> a call to {@link ICallbackUserOperations#postAssociate(AccountBean, UserBean)} was made to every user in the {@code users} list before this call
     * @param account The account with associated {@code users}
     * @param users The users, now associated with the {@code account}
     */
    public void postAssociate(@NotNull AccountBean account, @NotEmpty UserBean ...users);
    /**
     * Called before dissociate one or more {@code users} from an {@code account}.
     * <strong>WARNING!!</strong> a call to {@link ICallbackUserOperations#preDissociate(AccountBean, UserBean)} is made for every user in the {@code users} list after this call
     * <p>This operation is made as follows:
     * <ol>
     *     <li>Call to {@link #preDissociate(AccountBean, UserBean...)}
     *         <ul>
     *             <li>If returns {@link OperationCheckResult#isApproved()} is true:
     *                 <ul>
     *                     <li>A call to {@link ICallbackUserOperations#preDissociate(AccountBean, UserBean)} is made for every {@link UserBean user} in {@code users} list</li>
     *                     <li>If all calls was returned {@link OperationCheckResult#isApproved()} true, then:
     *                         <ul>
     *                             <li>Every {@link UserBean user} of the {@code users} list are effectively dissociated from {@code account}</li>
     *                         </ul>
     *                     </li>
     *                     <li>If any of the calls to {@link ICallbackUserOperations#preDissociate(AccountBean, UserBean)} returns {@link OperationCheckResult#isApproved()} false, operation is aborted.</li>
     *                 </ul>
     *             </li>
     *             <li>If returns {@link OperationCheckResult#isApproved()} is false, then operation is aborted</li>
     *         </ul>
     *     </li>
     *     <li>If operation was not aborted, then:
     *         <ul>
     *             <li>Call to {@link ICallbackUserOperations#postDissociate(AccountBean, UserBean)} for every {@link UserBean user} of the {@code users} list are made</li>
     *             <li>A call to {@link #postDissociate(AccountBean, UserBean...)} is made</li>
     *         </ul>
     *     </li>
     * </ol>
     * @param account The account where the {@code users} are associated
     * @param users The users to dissociate
     * @return The resulting check, if {@link OperationCheckResult#isApproved()} is true, operation is performed; if false, operation is aborted
     * @see OperationCheckResult#isApproved()
     */
    public OperationCheckResult preDissociate(@NotNull AccountBean account, @NotEmpty UserBean ...users);
    /**
     * Called after {@code users} are dissociated from the {@code account}.
     * <strong>WARNING!!</strong> a call to {@link ICallbackUserOperations#postDissociate(AccountBean, UserBean)} is made to every user in the {@code users} list before this call
     * @param account The account from where the {@code users} are dissociated
     * @param users The dissociated users
     */
    public void postDissociate(@NotNull AccountBean account, @NotEmpty UserBean ...users);
    /**
     * Called before remove an {@code account} from system; also a dissociation for all the users associated from are made (see {@link ICallbackUserOperations#preDissociate(AccountBean, UserBean)}).
     * The procedure of removing fire the following operations:
     * <ol>
     *     <li>Call to {@link #preRemove(AccountBean)} is made and if {@link OperationCheckResult#isApproved()} is true, then:
     *         <ol>
     *             <li>Call to {@link #preDissociate(AccountBean, UserBean...)} (that fire a specific process to dissociate users) and if return {@link OperationCheckResult#isApproved()} true then: 
     *                 <ul>
     *                     <li>Make the effective removing of the {@code account}</li>
     *                     <li>Call to {@link #postRemove(AccountBean)}</li>
     *                 </ul>
     *             </li>
     *             <li>If any of {@link #preDissociate(AccountBean, UserBean...)} returns {@link OperationCheckResult#isApproved()} false, then operation is aborted entirely</li>
     *         </ol>
     *     </li>
     *     <li>If {@link #preRemove(AccountBean)} returns {@link OperationCheckResult#isApproved()} false, operation is aborted and no account removing nor dissociate users is made.</li>
     * </ol>
     * @param account The account to remove to
     * @return The resulting check, if {@link OperationCheckResult#isApproved()} is true, operation is performed; if false, operation is aborted
     * @see OperationCheckResult#isApproved()
     * @see ICallbackUserOperations#preDissociate(AccountBean, UserBean)
     * @see ICallbackUserOperations#postDissociate(AccountBean, UserBean)
     */
    public OperationCheckResult preRemove(@NotNull AccountBean account);
    /**
     * Called after the {@code account} was removed.
     * <p>This is the last step in remove operation. You can delete specific information of your system that was linked with this account</p>
     * @param account The removed account
     * @see AccountBean#getId()
     */
    public void postRemove(@NotNull AccountBean account);

}
