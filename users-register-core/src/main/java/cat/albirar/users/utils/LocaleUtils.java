/*
 * This file is part of "spring-users-register-core".
 * 
 * "spring-users-register-core" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "spring-users-register-core" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "spring-users-register-core" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.utils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

/**
 * Some utilities for locale management.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public abstract class LocaleUtils {
    
    private static final Pattern ptLocale = Pattern.compile("([a-z]{0,2})_?([A-Z]{2})?.*");
    /**
     * Utility to convert from {@link Locale} to string.
     * @param locale The locale to convert
     * @return The string representation
     */
    public static final String localeToString(Locale locale) {
        return locale.toString();
    }
    /**
     * Utility to convert from a {@code strLocale} string to a Locale.
     * <p>Only get account of {@link Locale#getLanguage()} and {@link Locale#getCountry()}.</p>
     * <p>If no language are in {@code strLocale} the {@link Locale#getDefault() default} language is assumed</p>
     * <p>If no Country are in {@code strLocale} the {@link Locale#getDefault() default} country is assumed</p>
     * @param strLocale The text representation of locale in the form of {@code LL_CC.*}
     * @return The converted {@link Locale}
     */
    public static final Locale stringToLocale(String strLocale) {
        Matcher mt;
        String l, c;
        
        mt = ptLocale.matcher(strLocale);
        if(mt.matches()) {
            if(StringUtils.hasText(mt.group(1))) {
                l = mt.group(1);
            } else {
                l = Locale.getDefault().getLanguage();
            }
            
            if(StringUtils.hasText(mt.group(2))) {
                c = mt.group(2);
            } else {
                c = Locale.getDefault().getCountry();
            }
            return new Locale(l, c);
        } else {
            return Locale.getDefault();
        }
    }

}
