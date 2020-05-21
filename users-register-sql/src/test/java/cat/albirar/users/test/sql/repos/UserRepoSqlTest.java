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
package cat.albirar.users.test.sql.repos;

import java.sql.Timestamp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import cat.albirar.users.models.auth.AuthorizationBean;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.repos.sql.config.PropertiesSql;
import cat.albirar.users.repos.sql.mappings.AuthorizationRowMapper;
import cat.albirar.users.repos.sql.mappings.UserRowMapper;
import cat.albirar.users.test.UsersRegisterAbstractDataTest;
import cat.albirar.users.test.repos.UserRepoTest;
import cat.albirar.users.test.sql.UsersRegisterSqlTestConfig;
import cat.albirar.users.test.sql.testcontainer.SqlTestContainterExtension;

/**
 * The SQL backed {@link UserRepoTest}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@ContextConfiguration(classes = UsersRegisterSqlTestConfig.class)
@ExtendWith(SqlTestContainterExtension.class)
@Sql(scripts = {"/schema.sql"})
public class UserRepoSqlTest extends UserRepoTest {
    @Autowired
    protected NamedParameterJdbcTemplate namedJdbcTemplate;
    @Autowired
    protected UserRowMapper userRowMapper;
    
    @Autowired
    private Environment env;
    
    @BeforeEach
    public void setupTestData() {
        MapSqlParameterSource parm;
        String insertUser, insertAuth;
    
        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + UserRowMapper.TABLENAME());
        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + AuthorizationRowMapper.TABLENAME_AUTHORITIES());
        
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
            parm = userRowMapper.mapValuesForCreation(u.toBuilder().password(PASSWORDS[0]).build());
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
    
    @AfterEach
    public void teardownData() {
        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + UserRowMapper.TABLENAME());
        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + AuthorizationRowMapper.TABLENAME_AUTHORITIES());
    }
}
