# USERS-REGISTER

This is a project to brings a fully featured module for register users and accounts.

There should be as easy as include some annotations and configuration properties to get a fully fledged subsystem to manage all features of users and accounts.

## Features 

* Integrated with spring-boot, spring-mvc and spring-security
* Persistence, one of these
  * PostgreSQL
  * Mysql
  * MongoDb
* Security
  * Three standard roles:
    * User (regular user)
    * Account administrator (for account administration)
    * System administrator (for entire system administration)
  * Expandable to new specific roles
  * JWT Token based operations for verification, approbation and recover password
* Registration
  * Offers three modes of registration:
    * NONE (the user is registered without verification nor approbation)
    * ONE_STEP (in order to be registered, the user should to verify their identity with a link to their email or a SMS code to their phone)
    * TWO_STEP (ONE_STEP plus approbation by system administrator)
* User management
  * The user can be enabled or disabled
  * Model has:
    * An immutable ID, assigned on creation and useful to associate specific data
    * A mutable username, for login purposes
    * An immutable creation date (timestamp of creation)
    * An immutable verification date (timestamp of verification by user)
    * An immutable registration date (timestamp of approbation by system administrator)
    * Preferred communication channel (as email or phone)
    * Optional second communication channel (as email or phone)
    * An encrypted password
    * A expire date of user, optional
    * A expire date of credentials, optional
  * A process for recover password
  * A profile page to update their data
* Account management
  * The account can be enabled or disabled
  * Model has:
    * An immutable ID, assigned on creation and useful to associate specific data
    * A mutable name for showing purposes
  * A profile page to update data
* Communications
  * Allow to send email messages
  * Allow to send phone messages in form of:
    * (Current) SMS
    * (Future) Telegram, whatsapp, messenger, etc
* Customization
  * Callbacks before and after some operations on **users**:
    * Creation
    * Update
    * Association with account
    * Dissociation with account
    * Delete
  * Callbacks before and after some operations on **accounts**:
    * Creation
    * Update
    * Association with account
    * Dissociation with account
    * Delete
  * Thymeleaf fragments:
    * Show information in different ways
    * Get information
    * Form updating
  * Thymeleaf pages and web routes:
    * User registration
    * User login
    * User logout
    * User profile
    * User recover password
    * User change password from recover
    * Account creation
    * Account management

## Getting started

- Include dependencies

   - optionally users-register-mvc if use in mvc spring-boot application

   - users-register-sql (for mysql or postgresql back-end)

   - OR users-register-mongodb (for mongodb back-end)

   - If SQL back-end should to add jdbc driver:

     - For mysql: 

        ```xml
        <dependency>
          <groupId>mysql</groupId>
          <artifactId>mysql-connector-java</artifactId>
        </dependency>
        ```

     - For postgresql:

        ```xml
        <dependency>
          <groupId>org.postgresql</groupId>
          <artifactId>postgresql</artifactId>
        </dependency>
        ```

- Set properties:

  - General:
    - `albirar.auth.register.verification` one of `NONE`, `ONE_STEP` or `TWO_STEP`, by default is `NONE`
    - `albirar.auth.register.token.issuer` with issuer value, commonly the name of the application or company
    - `albirar.auth.register.token.expire` the number of days before a token is expired, by default is 10 days
  - For SQL back-end:
    - `albirar.auth.register.sql.prefix` with the prefix for tables on database, by default is `albirar_`. Note that the prefix should to end with a `_`, if not, no prefix is used and tables are named as is (user, user_authorities and account)
    - `albirar.auth.register.sql.datasource.url` Datasource URL, by default is `jdbc:mysql://localhost:3306/auth_users`
    - `albirar.auth.register.sql.datasource.driver` Datasource driver, by default is `com.mysql.jdbc.Driver`
    - `albirar.auth.register.sql.datasource.username` Datasource connection credentials username, by default is `auth_users`
    - `albirar.auth.register.sql.datasource.password` Datasource connection credentials password, by default is `auth_users`
  - For Mongodb back-end:
    - `albirar.auth.register.mongodb.host` The mongodb server host, by default is `localhost`
    - `albirar.auth.register.mongodb.port` The mongodb server port, by default is `27017`
    - `albirar.auth.register.mongodb.database` The mongodb server database, by default is `usersauth`

- Add required annotations

  - If `Spring autoconfigure` is enabled, the modules are configured automatically, no further annotations should to be used
  - Else, the following annotations should to be used in configuration class:
    - `EnableUsersRegisterMvc` if MVC is used
    - `EnableUsersRegisterMongodb` if mongodb back-end is used
    - `EnableUsersRegisterSql` if SQL back-end is used

- Further configuration on SQL backend

  - Selection of database on SQL back-end
    - The selection of database are made automatically based on jdbc url.
    - If the url contains `mysql` the MYSQL back-end is used
    - Else, the POSTGRESQL back-end is used
  - Schema creation
    - Should use the `schema.sql` file located on `users-register-sql` jar module. Customize with the configured table prefix and apply to database server

## Use in application

The main service is `cat.albirar.users.registration.IRegistrationService` that allow to get, update and remove users or accounts.

Should to implement the two callbacks in order to insert your functionalities associated with users and accounts in response to operations.

By example, if you want to implement a calendar associate to users, on callbacks should to get the generated id (on after create) and use it to generate the needed data.

