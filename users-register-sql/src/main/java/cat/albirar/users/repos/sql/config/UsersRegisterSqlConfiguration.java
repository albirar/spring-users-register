/*
 * This file is part of "albirar users-register-mysql".
 * 
 * "albirar users-register-mysql" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "albirar users-register-mysql" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "albirar users-register-mysql" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.repos.sql.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cat.albirar.users.config.UsersRegisterConfiguration;
import cat.albirar.users.repos.sql.AbstractSqlRepo;
import cat.albirar.users.repos.sql.mappings.AbstractRowMapper;

/**
 * Configuration for users register backed by mysql.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Configuration
@Import(UsersRegisterConfiguration.class)
@ComponentScan(basePackageClasses = {AbstractSqlRepo.class, AbstractRowMapper.class, UsersRegisterSqlDataSourceConfiguration.class})
@EnableTransactionManagement
@PropertySource("classpath:/cat/albirar/users/sql/users-sql.properties")
public class UsersRegisterSqlConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersRegisterSqlConfiguration.class);
    
    @Bean
    public DataSource dataSource(UsersRegisterSqlDataSourceConfiguration conf) {
        LOGGER.debug("Configuration: {}", conf);
        HikariConfig hConfig;
        
        hConfig = new HikariConfig();
        hConfig.setJdbcUrl(conf.getUrl());
        hConfig.setDriverClassName(conf.getDriver());
        hConfig.setUsername(conf.getUsername());
        hConfig.setPassword(conf.getPassword());
        hConfig.setAutoCommit(conf.isAutoCommit());
        return new HikariDataSource(hConfig);
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(JdbcTemplate jdbcTemplate) {
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }
    @Bean
    public PlatformTransactionManager transactionManager(DataSource datasource){
        return new DataSourceTransactionManager(datasource);
    }
}
