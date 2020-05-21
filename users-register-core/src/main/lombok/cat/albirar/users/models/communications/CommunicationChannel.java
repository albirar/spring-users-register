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
package cat.albirar.users.models.communications;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A communication channel, for verifying process or recover password, etc.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@SuperBuilder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class CommunicationChannel implements Serializable {
    private static final long serialVersionUID = 4988280421025258444L;
    /**
     * The channel type, required.
     * @param channeltype The channel type, required
     * @return The channel type
     */
    @Setter(onParam_ = { @NotNull })
    @NotNull
    private ECommunicationChannelType channelType;
    /**
     * The channel id, can be email or mobile.
     * @param channelId The channelId, can be an email or a mobile phone number, required
     * @return The channelId, can be an email or a mobile phone number
     */
    @Setter(onParam_ = { @NotBlank })
    @NotBlank
    private String channelId;
}
