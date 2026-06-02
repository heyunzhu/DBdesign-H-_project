USE library;

-- Run this once only when upgrading an existing database that was created
-- before sys_user.password_hash existed. Fresh databases only need schema.sql.
ALTER TABLE sys_user
ADD COLUMN password_hash VARCHAR(64) NOT NULL
DEFAULT '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'
AFTER user_name;

-- Initial demo password for existing users: 123456.
UPDATE sys_user
SET password_hash = '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92';
