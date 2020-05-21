/*
 * This file is part of "albirar users-register-sql".
 * 
 * "albirar users-register-sql" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "albirar users-register-sql" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "albirar users-register-sql" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.repos.sql.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A specific configuration for SQL connection for auth.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@Component
public class UsersRegisterSqlDataSourceConfiguration {
    
    @Value("${" + PropertiesSql.SQL_DATASOURCE_DRIVER + "}")
    private String driver;
    @Value("${" + PropertiesSql.SQL_DATASOURCE_URL + "}")
    private String url;
    @Value("${" + PropertiesSql.SQL_DATASOURCE_USERNAME + "}")
    private String username;
    @Value("${" + PropertiesSql.SQL_DATASOURCE_PASSWORD + "}")
    private String password;
    private boolean autoCommit = true;
    
}
