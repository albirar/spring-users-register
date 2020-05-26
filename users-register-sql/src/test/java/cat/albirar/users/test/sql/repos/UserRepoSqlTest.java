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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import cat.albirar.users.test.repos.UserRepoTest;
import cat.albirar.users.test.sql.SqlTestContainterExtension;
import cat.albirar.users.test.sql.SqlTestUtils;
import cat.albirar.users.test.sql.UsersRegisterSqlTestConfig;

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
    private SqlTestUtils sqlTestUtils;
    
    @BeforeEach
    public void setupTestData() {
        sqlTestUtils.setupData();
    }
    
    @AfterEach
    public void teardownData() {
        sqlTestUtils.teardownData();
    }
}
