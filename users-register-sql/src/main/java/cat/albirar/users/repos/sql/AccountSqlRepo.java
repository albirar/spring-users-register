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
package cat.albirar.users.repos.sql;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import cat.albirar.users.models.account.AccountBean;
import cat.albirar.users.repos.IAccountRepo;
import cat.albirar.users.repos.sql.mappings.AccountRowMapper;

/**
 * The sql backed {@link IAccountRepo}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Repository
@Transactional
public class AccountSqlRepo extends AbstractSqlRepo implements IAccountRepo {
    
    @Autowired
    private AccountRowMapper rowMapper;

    private static String SQL_COUNT;
    private static String SQL_SELECT_ALL;
    private static String SQL_SELECT_BY_ID;
    private static String SQL_SELECT_BY_NAME;
    private static String SQL_UPDATE;
    private static String SQL_CREATE;

    @PostConstruct
    public void setupSentences() {
        SQL_COUNT = "SELECT COUNT(*) FROM "
                + AccountRowMapper.TABLENAME()
                ;
        SQL_SELECT_ALL = "SELECT * FROM "
                + AccountRowMapper.TABLENAME()
                ;
        SQL_SELECT_BY_ID = SQL_SELECT_ALL
                + " WHERE "
                + AccountRowMapper.COL_ID + "=:" + AccountRowMapper.COL_ID
                ;
        SQL_SELECT_BY_NAME = SQL_SELECT_ALL
                + " WHERE "
                + AccountRowMapper.COL_NAME + "=:" + AccountRowMapper.COL_NAME
                ;
        SQL_UPDATE = "UPDATE "
                + AccountRowMapper.TABLENAME()
                + " SET "
                + AccountRowMapper.COL_NAME + "=:" + AccountRowMapper.COL_NAME
                + ", " + AccountRowMapper.COL_ENABLED + "=:" + AccountRowMapper.COL_ENABLED
                + " WHERE "
                + AccountRowMapper.COL_ID + "=:" + AccountRowMapper.COL_ID
                ;
        SQL_CREATE = "INSERT INTO "
                + AccountRowMapper.TABLENAME()
                + "("
                + AccountRowMapper.COL_NAME
                + ", " + AccountRowMapper.COL_ENABLED
                + ") VALUES ("
                + ":" + AccountRowMapper.COL_NAME
                + ", :" + AccountRowMapper.COL_ENABLED
                + ")"
                ;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable
    public List<AccountBean> findAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL, rowMapper);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable
    public Optional<AccountBean> findByName(String name) {
        try {
            return Optional.of(namedParameterJdbcTemplate.queryForObject(SQL_SELECT_BY_NAME, new MapSqlParameterSource(AccountRowMapper.COL_NAME, name), rowMapper));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountBean save(AccountBean account) {
        SqlParameterSource params;
        Number id;
        KeyHolder keyHolder;
        
        params = rowMapper.mapForSave(account);
        
        if(params.hasValue(AccountRowMapper.COL_ID)) {
            // UPDATE
            namedParameterJdbcTemplate.update(SQL_UPDATE, params);
            return account.toBuilder().build();
        }
        // CREATE!
        keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(SQL_CREATE, params, keyHolder);
        id = (Number)keyHolder.getKeys().get(AccountRowMapper.COL_ID);
        if(id == null) {
            id = keyHolder.getKey();
        }
        return account.toBuilder().id(Long.toString(id.longValue())).build();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AccountBean> findById(@NotBlank String id) {
        try {
            return Optional.of(namedParameterJdbcTemplate.queryForObject(SQL_SELECT_BY_ID, new MapSqlParameterSource(AccountRowMapper.COL_ID, Long.parseLong(id)), rowMapper));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        return jdbcTemplate.queryForObject(SQL_COUNT, Number.class).longValue();
    }
}
