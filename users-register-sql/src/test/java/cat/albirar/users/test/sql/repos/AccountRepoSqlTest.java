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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import cat.albirar.users.models.account.AccountBean;
import cat.albirar.users.repos.IAccountRepo;
import cat.albirar.users.repos.sql.config.PropertiesSql;
import cat.albirar.users.repos.sql.mappings.AccountRowMapper;
import cat.albirar.users.test.UsersRegisterAbstractDataTest;
import cat.albirar.users.test.repos.AccountRepoTest;
import cat.albirar.users.test.sql.UsersRegisterSqlTestConfig;
import cat.albirar.users.test.sql.testcontainer.SqlTestContainterExtension;

/**
 * The sql backed test {@link AccountRepoTest}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@ContextConfiguration(classes = UsersRegisterSqlTestConfig.class)
@ExtendWith(SqlTestContainterExtension.class)
@Sql(scripts = {"/schema.sql"})
public class AccountRepoSqlTest extends AccountRepoTest {

    @Autowired
    protected IAccountRepo accountRepo;
    @Autowired
    protected NamedParameterJdbcTemplate namedJdbcTemplate;
    @Autowired
    protected AccountRowMapper accountRowMapper;

    @Autowired
    private Environment env;
    
    @BeforeEach
    public void setupTestData() {
        MapSqlParameterSource parm;
        String insertAccount;
    
        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + AccountRowMapper.TABLENAME());
        
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
    }
    
    @AfterEach
    public void teardownData() {
        namedJdbcTemplate.getJdbcTemplate().update("DELETE FROM " + AccountRowMapper.TABLENAME());
    }
}
