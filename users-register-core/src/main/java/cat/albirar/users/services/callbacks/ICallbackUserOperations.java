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

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import cat.albirar.users.models.account.AccountBean;
import cat.albirar.users.models.services.callbacks.OperationCheckResult;
import cat.albirar.users.models.users.UserBean;

/**
 * {@link UserBean user} callback operations contract for "plugins" system.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public interface ICallbackUserOperations {
    /**
     * Called before creating the {@code newUser}; it allows to disapprove the creation.
     * @param newUser The new user, without {@link UserBean#getId() id} informed yet
     * @return The check result, if {@link OperationCheckResult#isApproved()} is false, operation is aborted and no new user are created
     * @see OperationCheckResult#isApproved()
     */
    public OperationCheckResult preCreate(@NotNull UserBean newUser);
    /**
     * Called after the {@code newUser} is created; allow to creating specifics data structures.
     * @param newUser The new created user, with {@link UserBean#getId() id} assigned
     */
    public void postCreate(@NotNull UserBean newUser);
    /**
     * Called before update the {@code currentUser}; it allows to disapprove the modification.
     * @param currentUser The user to be modified, with the current data, without any modification
     * @param modifiedUser The user with modification data, before persist them
     * @return The resulting check, if {@link OperationCheckResult#isApproved()} is true, operation is performed; if false, operation is aborted
     * @see OperationCheckResult#isApproved()
     */
    public OperationCheckResult preUpdate(@NotNull UserBean currentUser, @NotNull UserBean modifiedUser);
    /**
     * Called after the {@code user} was modified.
     * @param user The modified and persisted user
     */
    public void postUpdate(@NotNull UserBean user);
    /**
     * Called before associate the {@code user} whit the {@code account}.
     * <strong>WARNING!!</strong> this call is an atomic part of {@link ICallbackAccountOperations#preAssociate(AccountBean, UserBean...)} and allow to check, user by user, if association is possible.
     * @param account The account that {@code user} will be associated
     * @param user The user to associate to
     * @return The resulting check, if {@link OperationCheckResult#isApproved()} is true, operation is performed; if false, operation is aborted
     * @see OperationCheckResult#isApproved()
     */
    public OperationCheckResult preAssociate(@NotNull AccountBean account, @NotNull UserBean user);
    /**
     * Called after the {@code user} was associate with the {@code account}, allow to create or update specific data structures.
     * <strong>WARNING!!</strong> this call is made BEFORE calling {@link ICallbackAccountOperations#postAssociate(AccountBean, UserBean...)}.
     * @param account The account that {@code user} was associated
     * @param user The user that is associate with the {@code account}
     */
    public void postAssociate(@NotNull AccountBean account, @NotNull UserBean user);
    /**
     * Called before dissociate the {@code user} from the {@code account}.
     * <strong>WARNING!!</strong> this call is an atomic part of {@link ICallbackAccountOperations#preDissociate(AccountBean, UserBean...)} and allow to check, user by user, if dissociation is possible.
     * @param account The account that {@code user} will be dissociated
     * @param user The user to dissociate to
     * @return The resulting check, if {@link OperationCheckResult#isApproved()} is true, operation is performed; if false, operation is aborted
     * @see OperationCheckResult#isApproved()
     */
    public OperationCheckResult preDissociate(@NotNull AccountBean account, @NotNull UserBean user);
    /**
     * Called after the {@code user} was dissociate from the {@code account}, allow to remove or update specific data structures.
     * <strong>WARNING!!</strong> this call is made BEFORE calling {@link ICallbackAccountOperations#postDissociate(AccountBean, UserBean...)}.
     * @param account The account that {@code user} was dissociated
     * @param user The user that is dissociate from the {@code account}
     */
    public void postDissociate(@NotNull AccountBean account, @NotNull UserBean user);
    /**
     * Called before remove a {@code user} from registry.
     * This operation fire the following actions:
     * <ul>
     *     <li>Call to this method, {@link #preRemove(List, UserBean)} and if return {@link OperationCheckResult#isApproved()} is true, then:
     *         <ul>
     *             <li>Call to {@link #preDissociate(AccountBean, UserBean)} for each account on {@code accounts} list. If all returns {@link OperationCheckResult#isApproved()} true, then:
     *                 <ul>
     *                     <li>Make effective dissociation of {@code user} from each account on {@code accounts} list.</li>
     *                     <li>Call to {@link #postDissociate(AccountBean, UserBean)} for each accounton {@code accounts} list.</li>
     *                     <li>Effective removing of {@code user} from registry</li>
     *                     <li>Call to {@link #postRemove(List, UserBean)}</li>
     *                 </ul>
     *             </li>
     *             <li>If any call to {@link #preDissociate(AccountBean, UserBean)} returns {@link OperationCheckResult#isApproved()} is false, then entirely operation is aborted</li>
     *         </ul>
     *     </li>
     *     <li>If {@link OperationCheckResult#isApproved()} is false, then operation is aborted</li>
     * </ul>
     * @param accounts The accounts list that {@code user} is associated
     * @param user The user to remove to
     * @return The resulting check, if {@link OperationCheckResult#isApproved()} is true, operation is performed; if false, operation is aborted
     * @see OperationCheckResult#isApproved()
     */
    public OperationCheckResult preRemove(@NotEmpty List<AccountBean> accounts, @NotNull UserBean user);
    /**
     * Call after the {@code user} was removed.
     * @param accounts The accounts list that {@code user} was associated
     * @param user The removed user
     */
    public void postRemove(@NotEmpty List<AccountBean> comptes, @NotNull  UserBean usuari);
}
