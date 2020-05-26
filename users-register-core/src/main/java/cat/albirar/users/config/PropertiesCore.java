/*
 * This file is part of "albirar users-register-core".
 * 
 * "albirar users-register-core" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "albirar users-register-core" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "albirar users-register-core" source code.  If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * Copyright (C) 2020 Octavi Fornés
 */
package cat.albirar.users.config;

import cat.albirar.users.verification.EVerificationProcess;

/**
 * The properties of core module.
 * @author Octavi Forn&eacute;s &lt;<a href="mailto:ofornes@albirar.cat">ofornes@albirar.cat</a>&gt;
 * @since 1.0.0
 */
public interface PropertiesCore {
    /**
     * The root for all users-register properties.
     */
    public static final String ROOT_USERS_PROPERTIES = "albirar.auth.register";
    /**
     * Property for establish the verification process of registration.
     * @see EVerificationProcess
     */
    public static final String VERIFICATION_MODE_PROPERTY_NAME = ROOT_USERS_PROPERTIES + ".verification";
    /**
     * The root for all 'token' property configuration
     */
    public static final String ROOT_TOKENS = ROOT_USERS_PROPERTIES + ".token";
    /**
     * Property name for jwt token issuer.
     * @see <a href="https://tools.ietf.org/html/rfc7519#section-4.1.1">https://tools.ietf.org/html/rfc7519#section-4.1.1</a>
     */
    public static final String TOKEN_PROP_ISSUER = ROOT_TOKENS + ".issuer";

    /**
     * Property name for establish the expiration days for tokens.
     * @see <a href="https://tools.ietf.org/html/rfc7519#section-4.1.4">https://tools.ietf.org/html/rfc7519#section-4.1.4</a>
     */
    public static final String TOKEN_PROP_EXPIRE = ROOT_TOKENS + ".expire";

}
