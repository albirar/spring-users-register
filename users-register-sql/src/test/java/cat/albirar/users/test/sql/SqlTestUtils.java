/*
 * This file is part of "albirar spring-users-register-sql".
 * 
 * "albirar spring-users-register-sql" is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * "albirar spring-users-register-sql" is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with "albirar spring-users-register-sql"
 * source code. If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.test.sql;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cat.albirar.users.models.account.AccountBean;
import cat.albirar.users.models.auth.AuthorizationBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.repos.sql.config.PropertiesSql;
import cat.albirar.users.repos.sql.mappings.AccountRowMapper;
import cat.albirar.users.repos.sql.mappings.AuthorizationRowMapper;
import cat.albirar.users.repos.sql.mappings.UserRowMapper;
import cat.albirar.users.test.UsersRegisterAbstractDataTest;

/**
 * Sql test utils.
 * 
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Component
public class SqlTestUtils extends UsersRegisterAbstractDataTest {

    @Autowired
    protected NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    protected UserRowMapper userRowMapper;

    @Autowired
    protected AccountRowMapper accountRowMapper;

    @Autowired
    private Environment env;

    public void setupData() {
        MapSqlParameterSource parm;
        String insertAccount;
        String insertUser, insertAuth;
    
        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + AccountRowMapper.TABLENAME());

        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + UserRowMapper.TABLENAME());
        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + AuthorizationRowMapper.TABLENAME_AUTHORITIES());
        
        insertAccount = "INSERT INTO "
                + AccountRowMapper.TABLENAME()
                + " ("
                + AccountRowMapper.COL_ID
                + ", " + String.join(",", AccountRowMapper.NON_KEY_COLUMNS)
                + " ) VALUES ("
                + ":" + AccountRowMapper.COL_ID
                + ", :" + String.join(",:", AccountRowMapper.NON_KEY_COLUMNS)
                + ")"
                ;
        for(AccountBean ac : UsersRegisterAbstractDataTest.ACCOUNTS) {
            parm = accountRowMapper.mapForSave(ac);
            namedJdbcTemplate.update(insertAccount, parm);
        }
        // Set start of auto-account on postgresql
        if(env.getProperty(PropertiesSql.SQL_DATASOURCE_URL).contains("postgres")) {
            namedJdbcTemplate.update(String.format("ALTER SEQUENCE %s_id_seq RESTART WITH 10000", AccountRowMapper.TABLENAME()), new MapSqlParameterSource());
        }
        
        insertUser = "INSERT INTO "
                + UserRowMapper.TABLENAME()
                + " ("
                + UserRowMapper.COL_ID
                + ", " + String.join(",", UserRowMapper.NON_KEY_COLUMNS)
                + " ) VALUES ("
                + ":" + UserRowMapper.COL_ID
                + ", :" + String.join(",:", UserRowMapper.NON_KEY_COLUMNS)
                + ")"
                ;
        insertAuth = "INSERT INTO "
                + AuthorizationRowMapper.TABLENAME_AUTHORITIES()
                + " ("
                + AuthorizationRowMapper.COL_ID_USER_AUTH
                + ", " + AuthorizationRowMapper.COL_AUTHORITY
                + ") VALUES ("
                + ":" + AuthorizationRowMapper.COL_ID_USER_AUTH
                + ", :" + AuthorizationRowMapper.COL_AUTHORITY
                + ")"
                ;
        for(UserBean u : UsersRegisterAbstractDataTest.USERS) {
            parm = userRowMapper.mapValuesForCreation(u);
            parm.addValue(UserRowMapper.COL_CREATED, Timestamp.valueOf(u.getCreated()));
            parm.addValue(UserRowMapper.COL_ID, Long.parseLong(u.getId()));
            namedJdbcTemplate.update(insertUser, parm);
            // The authorities
            parm = new MapSqlParameterSource(AuthorizationRowMapper.COL_ID_USER_AUTH, Long.parseLong(u.getId()));
            for(AuthorizationBean a : u.getAuthorities()) {
                parm.addValue(AuthorizationRowMapper.COL_AUTHORITY, a.getAuthority());
                namedJdbcTemplate.update(insertAuth, parm);
            }
        }
        // Set start of auto-account on postgresql
        if(env.getProperty(PropertiesSql.SQL_DATASOURCE_URL).contains("postgres")) {
            namedJdbcTemplate.update(String.format("ALTER SEQUENCE %s_id_seq RESTART WITH 10000", UserRowMapper.TABLENAME()), new MapSqlParameterSource());
        }        
    }
    public void teardownData() {
        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + AccountRowMapper.TABLENAME());
        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + UserRowMapper.TABLENAME());
        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + AuthorizationRowMapper.TABLENAME_AUTHORITIES());
    }
}
