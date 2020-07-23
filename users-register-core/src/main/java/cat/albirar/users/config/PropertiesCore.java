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

import cat.albirar.communications.channels.models.ECommunicationChannelType;
import cat.albirar.communications.channels.models.ContactBean;
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
     * The root for all 'sender' property configuration.
     */
    public static final String ROOT_SENDER = ROOT_USERS_PROPERTIES + ".sender";
    /**
     * Property name for {@link ContactBean#getDisplayName()} sender's value.
     */
    public static final String SENDER_DISPLAY_NAME = ROOT_SENDER + ".name";
    /**
     * Property name for {@link ContactBean#getChannelBean() email} sender's value.
     */
    public static final String SENDER_EMAIL = ROOT_SENDER + ".mail";
    /**
     * Property name for {@link ContactBean#getPreferredLocale()} sender's value.
     */
    public static final String SENDER_LOCALE = ROOT_SENDER + ".locale";
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
    /**
     * The root for all 'templates' property configuration.
     */
    public static final String ROOT_TEMPLATES = ROOT_USERS_PROPERTIES + ".templates";
    /**
     * In order to dispose global variables to templates, any variable with this prefix will be put on template rendering.
     * <p>Any global variable will be visible from templates without this prefix.</p>
     * <p>By example:</p>
     * <pre>
     * albirar.auth.register.templates.variables.logo.image=https://web.com/images/logo.png
     * </pre>
     * Will be passed to template as:
     * <pre>
     * logo.image=https://web.com/images/logo.png
     * </pre>
     */
    public static final String TEMPLATES_GLOBAL_VARIABLES_PREFIX = ROOT_TEMPLATES + ".variables";
    public static final String TEMPLATES_GLOBAL_VARIABLES_PREFIX_DOT = TEMPLATES_GLOBAL_VARIABLES_PREFIX + ".";
    /**
     * Property name for configure the "root" path (without extension) of template for verification.
     * This is the path until name (included) but without extension.
     * The extension is selected depending on channel type and can be {@code .html} or {@code .txt}.
     * Usually they are two files, with same name but with different extension (one for each type).
     * The verification process select the correct extension in function of {@link ECommunicationChannelType} of destination.
     */
    public static final String TEMPLATE_VERIFICATION_PROPERTY = ROOT_TEMPLATES + ".verification";
    
    /**
     * Property name for configure the "root" path (without extension) of template for approbation.
     * This is the path until name (included) but without extension.
     * The extension is selected depending on channel type and can be {@code .html} or {@code .txt}.
     * Usually they are two files, with same name but with different extension (one for each type).
     * The verification process select the correct extension in function of {@link ECommunicationChannelType} of destination.
     */
    public static final String TEMPLATE_APPROBATION_PROPERTY = ROOT_TEMPLATES + ".approbation";
    /**
     * Property name for configure the "root" path (without extension) of template for recover password.
     * This is the path until name (included) but without extension.
     * The extension is selected depending on channel type and can be {@code .html} or {@code .txt}.
     * Usually they are two files, with same name but with different extension (one for each type).
     * The verification process select the correct extension in function of {@link ECommunicationChannelType} of destination.
     */
    public static final String TEMPLATE_RECOVER_PROPERTY = ROOT_TEMPLATES + ".recover";
    /**
     * Property name for configure the "root" path (without extension) of resources messages for templates rendering.
     * This is the path until name (included) but without extension nor locale specification.
     * The template system select the correct message property file in function of locale of destination.
     */
    public static final String TEMPLATE_RESOURCES_PROPERTY = ROOT_TEMPLATES + ".resources";

}
