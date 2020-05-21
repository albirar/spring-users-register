/*
 * This file is part of "albirar users-register-sql".
 * 
 * "albirar users-register-sql" is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * "albirar users-register-sql" is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with "albirar users-register-sql" source
 * code. If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.repos.sql.mappings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import cat.albirar.users.repos.sql.config.UsersRegisterSqlMappginConfiguration;

/**
 * Abstract root class for {@link RowMapper rowMappers}
 * 
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class AbstractRowMapper {
    @Autowired
    protected UsersRegisterSqlMappginConfiguration mappingConf;

    protected static final String prefixCol(String prefix, String col) {
        return (StringUtils.hasText(prefix) ? prefix.concat(".").concat(col) : col);
    }

    public static String formatId(long id) {
        // wide 24 with 0 padded
        return String.format("%024d", id);
    }
}
