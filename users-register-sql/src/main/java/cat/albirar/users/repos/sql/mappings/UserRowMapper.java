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

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import cat.albirar.users.models.communications.CommunicationChannel;
import cat.albirar.users.models.communications.ECommunicationChannelType;
import cat.albirar.users.models.users.UserBean;

/**
 * {@link RowMapper} for {@link UserBean}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Component
public class UserRowMapper extends AbstractRowMapper implements IPrefixedColsRowMapper<UserBean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRowMapper.class);

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public static final String COL_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PREFERREDCHANNEL_TYPE = "pc_type";
    public static final String COL_PREFERREDCHANNEL_VALUE = "pc_value";
    public static final String COL_SECONDARYCHANNEL_TYPE = "sc_type";
    public static final String COL_SECONDARYCHANNEL_VALUE = "sc_value";
    public static final String COL_PASSWORD = "password";
    public static final String COL_CREATED = "created";
    public static final String COL_VERIFIED = "verified";
    public static final String COL_REGISTERED = "registered";
    public static final String COL_EXPIRE = "expire";
    public static final String COL_LOCKED = "locked";
    public static final String COL_EXPIRECREDENTIALS = "expire_credentials";
    public static final String COL_ENABLED = "enabled";

    public static final String [] NON_KEY_COLUMNS = {
        COL_USERNAME, COL_PREFERREDCHANNEL_TYPE, COL_PREFERREDCHANNEL_VALUE
        , COL_SECONDARYCHANNEL_TYPE, COL_SECONDARYCHANNEL_VALUE
        , COL_PASSWORD, COL_CREATED, COL_VERIFIED, COL_REGISTERED
        , COL_EXPIRE, COL_LOCKED, COL_EXPIRECREDENTIALS, COL_ENABLED
    };

    private static final String TABLE_NAME = "user";
    private static String USER_TABLE = TABLE_NAME;
    
    public static final String TABLENAME() {
        return USER_TABLE;
    };
    
    @PostConstruct
    public final void init() {
        if(StringUtils.hasText(mappingConf.getPrefix()) && mappingConf.getPrefix().endsWith("_")) {
            USER_TABLE = mappingConf.getPrefix() + TABLE_NAME;
            LOGGER.debug("Prefix for tables is {}, User table name is {}", mappingConf.getPrefix(), USER_TABLE);
        } else {
            LOGGER.debug("NO prefix is indicated for tables, User table name is {}", USER_TABLE);
        }
    }

    public String mapId(String colPrefix, ResultSet rs, int rowNum) throws SQLException {
        return formatId(rs.getLong(prefixCol(colPrefix, COL_ID)));
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public UserBean mapRow(String colPrefix, ResultSet rs, int rowNum) throws SQLException {
        String t, v;
        LocalDate e, l, ec;
        Date d;
        Timestamp tv, tr;
        
        e = (d = rs.getDate(prefixCol(colPrefix, COL_EXPIRE))) == null ? null : d.toLocalDate(); 
        l = (d = rs.getDate(prefixCol(colPrefix, COL_LOCKED))) == null ? null : d.toLocalDate(); 
        ec = (d = rs.getDate(prefixCol(colPrefix, COL_EXPIRECREDENTIALS))) == null ? null : d.toLocalDate(); 
        t = rs.getString(prefixCol(colPrefix, COL_SECONDARYCHANNEL_TYPE));
        v = rs.getString(prefixCol(colPrefix, COL_SECONDARYCHANNEL_VALUE));
        tv = rs.getTimestamp(prefixCol(colPrefix, COL_VERIFIED));
        tr = rs.getTimestamp(prefixCol(colPrefix, COL_REGISTERED));
        return UserBean.builder()
                .id(mapId(colPrefix, rs, rowNum))
                .username(rs.getString(prefixCol(colPrefix, COL_USERNAME)))
                .preferredChannel(CommunicationChannel.builder()
                        .channelType(ECommunicationChannelType.valueOf(rs.getString(prefixCol(colPrefix, COL_PREFERREDCHANNEL_TYPE))))
                        .channelId(rs.getString(prefixCol(colPrefix, COL_PREFERREDCHANNEL_VALUE))).build())
                .secondaryChannel(t == null ? null : CommunicationChannel.builder().channelType(ECommunicationChannelType.valueOf(t)).channelId(v).build())
                .password(rs.getString(prefixCol(colPrefix, COL_PASSWORD)))
                .created(LocalDateTime.ofInstant(rs.getTimestamp(prefixCol(colPrefix, COL_CREATED)).toInstant(), ZoneId.systemDefault()))
                .verified(tv == null ? null : LocalDateTime.ofInstant(tv.toInstant(), ZoneId.systemDefault()))
                .registered(tr == null ? null : LocalDateTime.ofInstant(tr.toInstant(), ZoneId.systemDefault()))
                .expire(e)
                .locked(l)
                .enabled(rs.getBoolean(prefixCol(colPrefix, COL_ENABLED)))
                .expireCredentials(ec)
                .build();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public UserBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapRow(null, rs, rowNum);
    }

    /**
     * Add all values for creation.
     * @param user The user with the new values
     * @param parms The parameter values holder
     */
    public MapSqlParameterSource mapValuesForCreation(UserBean user) {
        MapSqlParameterSource parms;
        
        parms = new MapSqlParameterSource();
        parms.addValue(COL_USERNAME, user.getUsername());
        parms.addValue(COL_PREFERREDCHANNEL_TYPE, user.getPreferredChannel().getChannelType().name());
        parms.addValue(COL_PREFERREDCHANNEL_VALUE, user.getPreferredChannel().getChannelId());
        if(user.getSecondaryChannel() != null) {
            parms.addValue(COL_SECONDARYCHANNEL_TYPE, user.getSecondaryChannel().getChannelType().name());
            parms.addValue(COL_SECONDARYCHANNEL_VALUE, user.getSecondaryChannel().getChannelId());
        } else {
            parms.addValue(COL_SECONDARYCHANNEL_TYPE, null);
            parms.addValue(COL_SECONDARYCHANNEL_VALUE, null);
        }
        parms.addValue(COL_PASSWORD, user.getPassword());
        if(user.getCreated() != null) {
            parms.addValue(COL_CREATED, Timestamp.valueOf(user.getCreated()));
        } else {
            parms.addValue(COL_CREATED, Timestamp.valueOf(LocalDateTime.now()));
        }
        if(user.getVerified() != null) {
            parms.addValue(COL_VERIFIED, Date.from(user.getVerified().atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            parms.addValue(COL_VERIFIED, null);
        }
        if(user.getRegistered() != null) {
            parms.addValue(COL_REGISTERED, Date.from(user.getRegistered().atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            parms.addValue(COL_REGISTERED, null);
        }
        
        if(user.getExpire() != null) {
            parms.addValue(COL_EXPIRE, Date.valueOf(user.getExpire()));
        } else {
            parms.addValue(COL_EXPIRE, null);
        }
        if(user.getLocked() != null) {
            parms.addValue(COL_LOCKED, Date.valueOf(user.getLocked()));
        } else {
            parms.addValue(COL_LOCKED, null);
        }
        if(user.getExpireCredentials() != null) {
            parms.addValue(COL_EXPIRECREDENTIALS, Date.valueOf(user.getExpireCredentials()));
        } else {
            parms.addValue(COL_EXPIRECREDENTIALS, null);
        }
        parms.addValue(COL_ENABLED, Boolean.valueOf(user.isEnabled()));
        return parms;
    }

    /**
     * Compose the {@code SET ...} part of update sentence for update a user with only the changed values.
     * @param parms The parameter values holder (in and out), the values for sets will be put here
     * @param original The original (persisted) user
     * @param updated The new values for the user
     * @return A string with a composition of {@code COL=:COL...} for every changed value of user
     */
    public UpdateSets composeSets(UserBean original, UserBean updated) {
        List<String> sets;
        UpdateSets r;
        
        r = new UpdateSets();
        sets = new ArrayList<>();
        if(!original.getUsername().equals(updated.getUsername())) {
            sets.add(COL_USERNAME.concat("=:").concat(COL_USERNAME));
            r.addValue(COL_USERNAME, updated.getUsername());
        }
        if(!original.getPreferredChannel().equals(updated.getPreferredChannel())) {
            sets.add(COL_PREFERREDCHANNEL_TYPE.concat("=:").concat(COL_PREFERREDCHANNEL_TYPE));
            r.addValue(COL_PREFERREDCHANNEL_TYPE, updated.getPreferredChannel().getChannelType().name());
            sets.add(COL_PREFERREDCHANNEL_VALUE.concat("=:").concat(COL_PREFERREDCHANNEL_VALUE));
            r.addValue(COL_PREFERREDCHANNEL_VALUE, updated.getPreferredChannel().getChannelId());
        }
        if(!ObjectUtils.nullSafeEquals(original.getSecondaryChannel(), updated.getSecondaryChannel())) {
            if(updated.getSecondaryChannel() != null) {
                sets.add(COL_SECONDARYCHANNEL_TYPE.concat("=:").concat(COL_SECONDARYCHANNEL_TYPE));
                r.addValue(COL_SECONDARYCHANNEL_TYPE, updated.getSecondaryChannel().getChannelType().name());
                sets.add(COL_SECONDARYCHANNEL_VALUE.concat("=:").concat(COL_SECONDARYCHANNEL_VALUE));
                r.addValue(COL_SECONDARYCHANNEL_VALUE, updated.getSecondaryChannel().getChannelId());
            } else {
                sets.add(COL_SECONDARYCHANNEL_TYPE.concat("=NULL"));
                sets.add(COL_SECONDARYCHANNEL_VALUE.concat("=NULL"));
            }
        }
        if(StringUtils.hasText(updated.getPassword()) && !passwordEncoder.matches(updated.getPassword(), original.getPassword())) {
            sets.add(COL_PASSWORD.concat("=:").concat(COL_PASSWORD));
            r.addValue(COL_PASSWORD, updated.getPassword());
        }
        if(!ObjectUtils.nullSafeEquals(original.getVerified(), updated.getVerified())) {
            if(updated.getVerified() != null) {
                sets.add(COL_VERIFIED.concat("=:").concat(COL_VERIFIED));
                r.addValue(COL_VERIFIED, Date.from(updated.getVerified().atZone(ZoneId.systemDefault()).toInstant()));
            } else {
                sets.add(COL_VERIFIED.concat("=NULL"));
            }
        }
        if(!ObjectUtils.nullSafeEquals(original.getRegistered(), updated.getRegistered())) {
            if(updated.getRegistered() != null) {
                sets.add(COL_REGISTERED.concat("=:").concat(COL_REGISTERED));
                r.addValue(COL_REGISTERED, Date.from(updated.getRegistered().atZone(ZoneId.systemDefault()).toInstant()));
            } else {
                sets.add(COL_REGISTERED.concat("=NULL"));
            }
        }
        if(!ObjectUtils.nullSafeEquals(original.getExpire(), updated.getExpire())) {
            if(updated.getExpire() != null) {
                sets.add(COL_EXPIRE.concat("=:").concat(COL_EXPIRE));
                r.addValue(COL_EXPIRE, Date.valueOf(updated.getExpire()));
            } else {
                sets.add(COL_EXPIRE.concat("=NULL"));
            }
        }
        if(!ObjectUtils.nullSafeEquals(original.getLocked(), updated.getLocked())) {
            if(updated.getLocked() != null) {
                sets.add(COL_LOCKED.concat("=:").concat(COL_LOCKED));
                r.addValue(COL_LOCKED, Date.valueOf(updated.getLocked()));
            } else {
                sets.add(COL_LOCKED.concat("=NULL"));
            }
        }
        if(!ObjectUtils.nullSafeEquals(original.getExpireCredentials(), updated.getExpireCredentials())) {
            if(updated.getExpireCredentials() != null) {
                sets.add(COL_EXPIRECREDENTIALS.concat("=:").concat(COL_EXPIRECREDENTIALS));
                r.addValue(COL_EXPIRECREDENTIALS, Date.valueOf(updated.getExpireCredentials()));
            } else {
                sets.add(COL_EXPIRECREDENTIALS.concat("=NULL"));
            }
        }
        if(original.isEnabled() != updated.isEnabled()) {
            sets.add(COL_ENABLED.concat("=:").concat(COL_ENABLED));
            r.addValue(COL_ENABLED, Boolean.valueOf(updated.isEnabled()));
        }
        r.setSetsString(String.join(", ", sets));
        return r;
    }

    public static class UpdateSets {
        private MapSqlParameterSource parms;
        private String setsString;
        public UpdateSets() {
            parms = new MapSqlParameterSource();
        }
        public MapSqlParameterSource getParms() {
            return parms;
        }
        public void addValue(String name, Object value) {
            parms.addValue(name, value);
        }
        public void setSetsString(String setsString) {
            this.setsString = setsString;
        }
        public String getSetsString() {
            return setsString;
        }
    }
}
