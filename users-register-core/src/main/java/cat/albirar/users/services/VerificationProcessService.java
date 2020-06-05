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
package cat.albirar.users.services;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cat.albirar.users.models.verification.ProcessBean;
import cat.albirar.users.verification.IVerificationProcessService;

/**
 * The verification process service.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Service
@Validated
public class VerificationProcessService implements IVerificationProcessService {
    /**
     * {@inheritDoc}
     */
    @Override
    public String startVerifyProcess(ProcessBean verificationProcess) {
        // TODO Pending!
        /*
         * Planned to be:
         * 1. Prepare variables
         * 2. Process template with all info
         * 3. Push a message into communications queue with template processing as body
         * 4. Return the message id
         * 
         * The queue process the message and send the verification request to user through the channel
         * The link point to a 
         */
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String startApproveProcess(@NotNull @Valid ProcessBean approbationProcess) {
        // TODO Pending
        /*
         * Planned to be:
         * 1. Prepare variables
         * 2. Process template with all info
         * 3. Push a message into communications queue  with template processing as body
         * 4. Return the message id
         * 
         */
        return null;
    }
}
