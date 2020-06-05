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
package cat.albirar.users.verification;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import cat.albirar.users.models.verification.ProcessBean;

/**
 * The service contract to start a verifying process for registration.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public interface IVerificationProcessService {
    /**
     * Start a verification process.
     * @param verificationProcess The verification process information
     * @return The unique id to process the response from verification
     */
    public String startVerifyProcess(@NotNull @Valid ProcessBean verificationProcess);
    /**
     * Start a approve process.
     * @param approbationProcess The approbation process information
     * @return The unique id to process the response from approbation
     */
    public String startApproveProcess(@NotNull @Valid ProcessBean approbationProcess);
}
