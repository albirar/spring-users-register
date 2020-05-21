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
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import cat.albirar.users.models.auth.AuthorizationBean;
import cat.albirar.users.models.communications.CommunicationChannel;
import cat.albirar.users.models.users.UserBean;
import cat.albirar.users.repos.IUserRepo;
import cat.albirar.users.repos.sql.mappings.AuthorizationRowMapper;
import cat.albirar.users.repos.sql.mappings.UserResultsetExtractor;
import cat.albirar.users.repos.sql.mappings.UserRowMapper;
import cat.albirar.users.repos.sql.mappings.UserRowMapper.UpdateSets;

/**
 * The sql backed {@link IUserRepo}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Repository
@Transactional
public class UserSqlRepo extends AbstractSqlRepo implements IUserRepo {
    private static String PART_SELECT;
    private static String SQL_FIND_ALL;
    private static String SQL_FIND_BY_ID;
    private static String SQL_EXIST_ID;
    private static String SQL_EXIST_USERNAME;
    private static String SQL_EXIST_PREF_CHANNEL;
    private static String SQL_EXIST_SEC_CHANNEL;
    private static String SQL_FIND_BY_USERNAME;
    private static String SQL_COUNT;
    private static String TEMPLATE_SQL_UPDATE_USER;
    private static String SQL_CREATE_USER;
    private static String SQL_CREATE_USER_W_ID;
    private static String SQL_REMOVE_USER_AUTHORITIES;
    private static String SQL_CREATE_USER_AUTHORITIES;
    
    @Autowired
    private UserResultsetExtractor userResultsetExtractor;

    @Autowired
    private UserRowMapper userRowMapper;
    
    /**
     * Setup sentences to use configured prefix.
     */
    @PostConstruct
    public void setupSentences() {
        PART_SELECT = "SELECT "
                + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_ID + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_ID + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_USERNAME + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_USERNAME + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_PREFERREDCHANNEL_TYPE + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_PREFERREDCHANNEL_TYPE + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_PREFERREDCHANNEL_VALUE + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_PREFERREDCHANNEL_VALUE + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_SECONDARYCHANNEL_TYPE + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_SECONDARYCHANNEL_TYPE + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_SECONDARYCHANNEL_VALUE + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_SECONDARYCHANNEL_VALUE + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_PASSWORD + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_PASSWORD + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_CREATED + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_CREATED + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_VERIFIED + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_VERIFIED + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_REGISTERED + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_REGISTERED + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_EXPIRE + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_EXPIRE + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_LOCKED + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_LOCKED + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_EXPIRECREDENTIALS + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_EXPIRECREDENTIALS + "\""
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_ENABLED + " AS \"" + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_ENABLED + "\""
                + ", " + UserResultsetExtractor.USER_AUTH_PREFIX + "." + AuthorizationRowMapper.COL_ID_USER_AUTH + " AS \"" + UserResultsetExtractor.USER_AUTH_PREFIX + "." + AuthorizationRowMapper.COL_ID_USER_AUTH + "\""
                + ", " + UserResultsetExtractor.USER_AUTH_PREFIX + "." + AuthorizationRowMapper.COL_AUTHORITY + " AS \"" + UserResultsetExtractor.USER_AUTH_PREFIX + "." + AuthorizationRowMapper.COL_AUTHORITY + "\""
                + " FROM " + UserResultsetExtractor.BASIC_JOIN
                ;
        SQL_FIND_ALL = PART_SELECT
                + " ORDER BY "
                + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_USERNAME
                + ", " + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_CREATED
                ;
        SQL_FIND_BY_ID = PART_SELECT
                + " WHERE "
                + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_ID + "=:" + UserRowMapper.COL_ID
                ;
        SQL_EXIST_ID = "SELECT COUNT(*) FROM "
                + UserRowMapper.TABLENAME()
                + " WHERE "
                + UserRowMapper.COL_ID + "=:" + UserRowMapper.COL_ID
                ;
        SQL_EXIST_USERNAME = "SELECT COUNT(*) FROM "
                + UserRowMapper.TABLENAME()
                + " WHERE "
                + UserRowMapper.COL_USERNAME + "=:" + UserRowMapper.COL_USERNAME
                ;
        SQL_EXIST_PREF_CHANNEL = "SELECT COUNT(*) FROM "
                + UserRowMapper.TABLENAME()
                + " WHERE "
                + UserRowMapper.COL_PREFERREDCHANNEL_TYPE + "=:" + UserRowMapper.COL_PREFERREDCHANNEL_TYPE
                + " AND "
                + UserRowMapper.COL_PREFERREDCHANNEL_VALUE + "=:" + UserRowMapper.COL_PREFERREDCHANNEL_VALUE
                ;
        SQL_EXIST_SEC_CHANNEL = "SELECT COUNT(*) FROM "
                + UserRowMapper.TABLENAME()
                + " WHERE "
                + UserRowMapper.COL_SECONDARYCHANNEL_TYPE + "=:" + UserRowMapper.COL_SECONDARYCHANNEL_TYPE
                + " AND "
                + UserRowMapper.COL_SECONDARYCHANNEL_VALUE + "=:" + UserRowMapper.COL_SECONDARYCHANNEL_VALUE
                ;
        SQL_FIND_BY_USERNAME = PART_SELECT
                + " WHERE "
                + UserResultsetExtractor.USER_PREFIX + "." + UserRowMapper.COL_USERNAME + "=:" + UserRowMapper.COL_USERNAME
                ;
        SQL_COUNT = "SELECT COUNT(*) FROM "
                + UserRowMapper.TABLENAME()
                ;
        TEMPLATE_SQL_UPDATE_USER = "UPDATE "
                + UserRowMapper.TABLENAME()
                + " SET "
                + " %s " // Put COL=:COL for each updated value
                + " WHERE "
                + UserRowMapper.COL_ID + "=:" + UserRowMapper.COL_ID
                ;
        SQL_CREATE_USER = "INSERT INTO "
                + UserRowMapper.TABLENAME()
                + " ("
                + String.join(",", UserRowMapper.NON_KEY_COLUMNS)
                + ") VALUES (:"
                + String.join(",:", UserRowMapper.NON_KEY_COLUMNS)
                + ")"
                ;
        SQL_CREATE_USER_W_ID = "INSERT INTO "
                + UserRowMapper.TABLENAME()
                + " ("
                + UserRowMapper.COL_ID
                + ", " + String.join(",", UserRowMapper.NON_KEY_COLUMNS)
                + ") VALUES (:"
                + UserRowMapper.COL_ID
                + ",:" + String.join(",:", UserRowMapper.NON_KEY_COLUMNS)
                + ")"
                ;
                
        SQL_REMOVE_USER_AUTHORITIES = "DELETE FROM "
                + AuthorizationRowMapper.TABLENAME_AUTHORITIES()
                + " WHERE "
                + AuthorizationRowMapper.COL_ID_USER_AUTH + "=:" + AuthorizationRowMapper.COL_ID_USER_AUTH
                ;
        SQL_CREATE_USER_AUTHORITIES = "INSERT INTO "
                + AuthorizationRowMapper.TABLENAME_AUTHORITIES()
                + " ("
                + AuthorizationRowMapper.COL_ID_USER_AUTH
                + ", " + AuthorizationRowMapper.COL_AUTHORITY
                + ") VALUES ("
                + ":" + AuthorizationRowMapper.COL_ID_USER_AUTH
                + ", :" + AuthorizationRowMapper.COL_AUTHORITY
                + ")"
                ;        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserBean> findById(String id) {
        List<UserBean> l;
        
        l = namedParameterJdbcTemplate.query(SQL_FIND_BY_ID, new MapSqlParameterSource(UserRowMapper.COL_ID, Long.parseLong(id)), userResultsetExtractor);
        if(l.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(l.get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserBean> findAll() {
        return jdbcTemplate.query(SQL_FIND_ALL, userResultsetExtractor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByUsername(String username) {
        return namedParameterJdbcTemplate.queryForObject(SQL_EXIST_USERNAME
                , new MapSqlParameterSource(UserRowMapper.COL_USERNAME, username)
                , Number.class).longValue() == 1L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByPreferredChannel(CommunicationChannel preferredChannel) {
        return namedParameterJdbcTemplate.queryForObject(SQL_EXIST_PREF_CHANNEL
                , new MapSqlParameterSource(UserRowMapper.COL_PREFERREDCHANNEL_TYPE, preferredChannel.getChannelType().name())
                    .addValue(UserRowMapper.COL_PREFERREDCHANNEL_VALUE, preferredChannel.getChannelId())
                , Number.class).longValue() == 1L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsBySecondaryChannel(CommunicationChannel secondaryChannel) {
        return namedParameterJdbcTemplate.queryForObject(SQL_EXIST_SEC_CHANNEL
                , new MapSqlParameterSource(UserRowMapper.COL_SECONDARYCHANNEL_TYPE, secondaryChannel.getChannelType().name())
                    .addValue(UserRowMapper.COL_SECONDARYCHANNEL_VALUE, secondaryChannel.getChannelId())
                , Number.class).longValue() == 1L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserBean> findByUsername(String username) {
        List<UserBean> l;
        
        l = namedParameterJdbcTemplate.query(SQL_FIND_BY_USERNAME, new MapSqlParameterSource(UserRowMapper.COL_USERNAME, username), userResultsetExtractor);
        if(l.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(l.get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserBean save(UserBean user) {
        MapSqlParameterSource parms;
        UserBean saved;
        List<AuthorizationBean> a1, a2;
        
        if(StringUtils.hasText(user.getId())
                    && existsById(user.getId())) {
            
            UpdateSets sets;
            UserBean u;
            
            u = findById(user.getId()).get();
            sets = userRowMapper.composeSets(u, user);
            if(StringUtils.hasText(sets.getSetsString())) {
                // UPDATE
                // The user bean
                sets.addValue(UserRowMapper.COL_ID, Long.parseLong(user.getId()));
                namedParameterJdbcTemplate.update(String.format(TEMPLATE_SQL_UPDATE_USER, sets.getSetsString()), sets.getParms());
            }
            a1 = u.getAuthorities().stream().sorted((au1, au2) -> au1.getAuthority().compareTo(au2.getAuthority())).collect(Collectors.toList());
            a2 = user.getAuthorities().stream().sorted((au1, au2) -> au1.getAuthority().compareTo(au2.getAuthority())).collect(Collectors.toList());
            if(!ObjectUtils.nullSafeEquals(a1, a2)) {
                // The authorities
                parms = new MapSqlParameterSource(AuthorizationRowMapper.COL_ID_USER_AUTH, Long.parseLong(user.getId()));
                namedParameterJdbcTemplate.update(SQL_REMOVE_USER_AUTHORITIES, parms);
                for(AuthorizationBean a : user.getAuthorities()) {
                    parms.addValue(AuthorizationRowMapper.COL_AUTHORITY, a.getAuthority());
                    namedParameterJdbcTemplate.update(SQL_CREATE_USER_AUTHORITIES, parms);
                }
            }
            saved = findById(user.getId()).get();
        } else {
            KeyHolder keyHolder;
            Number id;

            // CREATE
            parms = userRowMapper.mapValuesForCreation(user);
            if(StringUtils.hasText(user.getId())) {
                id = Long.parseLong(user.getId());
                parms.addValue(UserRowMapper.COL_ID, id);
                namedParameterJdbcTemplate.update(SQL_CREATE_USER_W_ID, parms);
            } else {
                keyHolder = new GeneratedKeyHolder();
                namedParameterJdbcTemplate.update(SQL_CREATE_USER, parms, keyHolder);
                id = (Number)keyHolder.getKeys().get(UserRowMapper.COL_ID);
                if(id == null) {
                    id = keyHolder.getKey();
                }
            }
            // The authorities
            parms = new MapSqlParameterSource(AuthorizationRowMapper.COL_ID_USER_AUTH, id);
            for(AuthorizationBean a : user.getAuthorities()) {
                parms.addValue(AuthorizationRowMapper.COL_AUTHORITY, a.getAuthority());
                namedParameterJdbcTemplate.update(SQL_CREATE_USER_AUTHORITIES, parms);
            }
            saved = findById(id.toString()).get();
        }
        return saved;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        return jdbcTemplate.queryForObject(SQL_COUNT, Number.class).longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsById(@NotBlank String id) {
        return namedParameterJdbcTemplate.queryForObject(SQL_EXIST_ID
                , new MapSqlParameterSource(UserRowMapper.COL_ID, Long.parseLong(id))
                , Number.class).longValue() == 1L;
    }
}
