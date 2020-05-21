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
package cat.albirar.users.repos.sql.mappings;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import cat.albirar.users.models.auth.AuthorizationBean;

/**
 * The {@link AuthorizationBean} row mapper.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Component
public class AuthorizationRowMapper extends AbstractRowMapper implements IPrefixedColsRowMapper<AuthorizationBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationRowMapper.class);
    
    public static final String COL_ID_USER_AUTH = "user_id";
    public static final String COL_AUTHORITY = "authority";

    private static final String TABLE_NAME = "user_authorities";
    private static String USER_AUTHORITIES_TABLE = TABLE_NAME;
    
    public static final String TABLENAME_AUTHORITIES() {
        return USER_AUTHORITIES_TABLE;
    }
    @PostConstruct
    public final void init() {
        if(StringUtils.hasText(mappingConf.getPrefix()) && mappingConf.getPrefix().endsWith("_")) {
            USER_AUTHORITIES_TABLE = mappingConf.getPrefix() + TABLE_NAME;
            LOGGER.debug("Prefix for tables is {}, user authorities table name is {}", mappingConf.getPrefix(), USER_AUTHORITIES_TABLE);
        } else {
            LOGGER.debug("NO prefix is indicated for tables, user authorities table name is {}", USER_AUTHORITIES_TABLE);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationBean mapRow(String colPrefix, ResultSet rs, int rowNum) throws SQLException {
        return AuthorizationBean.builder()
                .authority(rs.getString(prefixCol(colPrefix, COL_AUTHORITY)))
                .build()
                ;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapRow(null, rs, rowNum);
    }

}
