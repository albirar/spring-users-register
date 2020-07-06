DROP TABLE IF EXISTS albirar_account;
CREATE TABLE albirar_account
(
	id SERIAL NOT NULL PRIMARY KEY
	, name VARCHAR(255) NOT NULL UNIQUE
	, enabled BOOLEAN NOT NULL DEFAULT TRUE
);

DROP TABLE IF EXISTS albirar_user;
CREATE TABLE albirar_user
(
	id SERIAL NOT NULL PRIMARY KEY
	,username VARCHAR(50) NOT NULL UNIQUE
	,pc_type VARCHAR(50) NOT NULL
	,pc_value VARCHAR(255) NOT NULL
	,sc_type VARCHAR(50)
	,sc_value VARCHAR(255) 
	,password VARCHAR(255) NOT NULL
	,locale VARCHAR(6) NOT NULL
	,created TIMESTAMP NOT NULL DEFAULT NOW()
	,verified TIMESTAMP NULL
	,registered TIMESTAMP NULL
	,expire DATE NULL
	,locked DATE NULL
	,expire_credentials DATE NULL
	,enabled BOOLEAN NOT NULL DEFAULT FALSE
	, CONSTRAINT pc_unq UNIQUE (pc_type, pc_value)
	, CONSTRAINT sc_idx UNIQUE (sc_type, sc_value)
);

DROP TABLE IF EXISTS albirar_user_authorities;
CREATE TABLE albirar_user_authorities
(
	user_id BIGINT NOT NULL
	, authority VARCHAR(255) NOT NULL
	, PRIMARY KEY (user_id, authority)
);
CREATE INDEX albirar_user_authorities_idx1 ON albirar_user_authorities (user_id);