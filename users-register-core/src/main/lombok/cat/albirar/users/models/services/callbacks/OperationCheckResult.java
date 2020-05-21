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
package cat.albirar.users.models.services.callbacks;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * A operation check result.
 * Approved result, disapproved description (why) and checker name (who).
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
public class OperationCheckResult {
    /**
     * Approved flag result
     * @param approved true for approved and false if not approved
     * @return true for approved and false if not approved
     */
    private boolean approved;
    /**
     * If {@link #isApproved()} is false, this is a description of why the involved operation is not approved.
     * @param vetoDescription The description of why the involved operation is disaproved
     * @return The description of why the involved operation is disaproved
     */
    private String vetoDescription;
    /**
     * If {@link #isApproved()} is false, this is the name of the checker that disapproved the involved operation.
     * @param checkerName The name of the checker that disapproved the involved operation (for exemple 'blog', 'inventary', etc.)
     * @return The name of the checker that disapproved the involved operation (for exemple 'blog', 'inventary', etc.)
     */
    private String nomVetador;
}
