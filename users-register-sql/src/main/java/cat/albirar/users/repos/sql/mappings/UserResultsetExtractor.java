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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import cat.albirar.users.models.auth.AuthorizationBean;
import cat.albirar.users.models.users.UserBean;

/**
 * {@link UserBean} result set extractor to work with joins for {@link AuthorizationBean authorities} and {@link ProfileBean profiles}.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
@Component
public class UserResultsetExtractor implements ResultSetExtractor<List<UserBean>> {
    public static final String USER_PREFIX = "U";
    public static final String USER_AUTH_PREFIX = "A";
    public static String BASIC_JOIN;
    @Autowired
    private UserRowMapper userRowMapper;
    @Autowired
    private AuthorizationRowMapper authRowMapper;
    
    @PostConstruct
    public void setupSentences() {
        BASIC_JOIN = UserRowMapper.TABLENAME() + " " + USER_PREFIX
                + " LEFT JOIN " + AuthorizationRowMapper.TABLENAME_AUTHORITIES() + " " + USER_AUTH_PREFIX
                + " ON " + USER_AUTH_PREFIX + "." + AuthorizationRowMapper.COL_ID_USER_AUTH + "=" + USER_PREFIX + "." + UserRowMapper.COL_ID
                ;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserBean> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<UserBean> result;
        UserBean u, u1;
        int rowNum;
        AuthorizationBean a;
        
        rowNum = 0;
        result = new ArrayList<>();
        if(rs.next()) {
            u = userRowMapper.mapRow(USER_PREFIX, rs, rowNum);
            a = authRowMapper.mapRow(USER_AUTH_PREFIX, rs, rowNum);
            if(a.getAuthority() != null) {
                u.getAuthorities().add(a);
            }
            result.add(u);
            while(rs.next()) {
                u1 = userRowMapper.mapRow(USER_PREFIX, rs, rowNum);
                if(!u1.getId().equals(u.getId())) {
                    result.add(u1);
                    u = u1;
                }
                a = authRowMapper.mapRow(USER_AUTH_PREFIX, rs, rowNum);
                if(a.getAuthority() != null) {
                    u.getAuthorities().add(a);
                }
            }
        }
        return result;
    }
}
