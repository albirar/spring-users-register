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
package cat.albirar.users.repos.sql.mappings;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import cat.albirar.users.models.account.AccountBean;

/**
 * The {@link RowMapper} for {@link AccountBean}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Component
public class AccountRowMapper extends AbstractRowMapper implements IPrefixedColsRowMapper<AccountBean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRowMapper.class);

    private static final String TABLE_NAME = "account";
    private static String ACCOUNT_TABLE = TABLE_NAME;
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_ENABLED = "enabled";
    
    @PostConstruct
    public final void init() {
        if(StringUtils.hasText(mappingConf.getPrefix()) && mappingConf.getPrefix().endsWith("_")) {
            ACCOUNT_TABLE = mappingConf.getPrefix() + TABLE_NAME;
            LOGGER.debug("Prefix for tables is {}, Account table name is {}", mappingConf.getPrefix(), ACCOUNT_TABLE);
        } else {
            LOGGER.debug("NO prefix is indicated for Account, Profile table name is {}", ACCOUNT_TABLE);
        }
    }
    
    public static final String TABLENAME () {
        return ACCOUNT_TABLE;
    }

    public static final String [] NON_KEY_COLUMNS = {
        COL_NAME, COL_ENABLED
    };
    /**
     * {@inheritDoc}
     */
    @Override
    public AccountBean mapRow(String colPrefix, ResultSet rs, int rowNum) throws SQLException {
        long l;
        l = rs.getLong(prefixCol(colPrefix, COL_ID));
        if(rs.wasNull()) {
            return null;
        }
        
        return AccountBean.builder()
                .id(formatId(l))
                .name(rs.getString(prefixCol(colPrefix, COL_NAME)))
                .enabled(rs.getBoolean(prefixCol(colPrefix, COL_ENABLED)))
                .build()
                ;
        
    }
    /**
     * Map an {@link AccountBean} with values from {@link #ACCOUNT_TABLE}.
     * The mapping is:
     * <ul>
     * <li>{@link AccountBean#getId()} with {@value #COL_ID}</li>
     * <li>{@link AccountBean#getName()} with {@value #COL_NAME}</li>
     * <li>{@link AccountBean#isEnabled()} with {@value #COL_ENABLED}</li>
     * </ul>
     */
    @Override
    public AccountBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapRow(null, rs, rowNum);
    }
    /**
     * Map the indicated {@code account} to parameter source values for save.
     * <p>If {@link AccountBean#getId()} have {@link StringUtils#hasText(String) text}, then {@value #COL_ID} parameter is mapped with {@link AccountBean#getId()}.</p>
     * <p>If {@link AccountBean#getId()} is null or empty or blank, then no mapping is made for {@value #COL_ID} parameter (considering is create)</p>
     * @param account The account bean
     * @return The parameter source with values
     */
    public MapSqlParameterSource mapForSave(@NotNull @Valid AccountBean account) {
        MapSqlParameterSource param;
        
        param = new MapSqlParameterSource(COL_NAME, account.getName())
                .addValue(COL_ENABLED, account.isEnabled())
                ;
        if(StringUtils.hasText(account.getId())) {
            param.addValue(COL_ID, Long.parseLong(account.getId()));
        }
        return param;
    }
}
