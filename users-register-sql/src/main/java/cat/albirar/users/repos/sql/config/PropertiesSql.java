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

import javax.sql.DataSource;

import cat.albirar.users.config.PropertiesCore;

/**
 * Properties for SQL back-end configuration.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public interface PropertiesSql extends PropertiesCore {
    /**
     * Root of all SQL configuration properties.
     */
    public static final String ROOT_SQL = ROOT_USERS_PROPERTIES + ".sql";
    /**
     * Root of all SQL {@link DataSource} configuration properties.
     */
    public static final String SQL_ROOT_DATASOURCE = ROOT_SQL + ".datasource";
    /**
     * Configuration property for SQL {@link DataSource} driver class name. 
     */
    public static final String SQL_DATASOURCE_DRIVER = SQL_ROOT_DATASOURCE + ".driver";
    /**
     * Configuration property for SQL {@link DataSource} jdbc url. 
     */
    public static final String SQL_DATASOURCE_URL = SQL_ROOT_DATASOURCE + ".url";
    /**
     * Configuration property for SQL {@link DataSource} connection username authentication. 
     */
    public static final String SQL_DATASOURCE_USERNAME = SQL_ROOT_DATASOURCE + ".username";
    /**
     * Configuration property for SQL {@link DataSource} connection password authentication. 
     */
    public static final String SQL_DATASOURCE_PASSWORD = SQL_ROOT_DATASOURCE + ".password";
    /**
     * Configuration property for SQL table prefix. 
     */
    public static final String SQL_PREFIX_TABLES = ROOT_SQL + ".prefix";
}
