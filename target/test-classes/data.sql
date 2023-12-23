-- ############# Users Initialization ############# --
-- [ADMIN USER]
INSERT INTO users (id, username, password, role, name, last_name, savings, created_on, last_updated_on)
VALUES (
    '24930b8e-ff6e-476a-8758-4bd3ff5ebd6b',
    'admin',
    'test',
    'ADMIN',
    'Admin Name',
    'Admin Lastname',
    35.50,
    PARSEDATETIME('2020-06-14 00:00:00Z', 'yyyy-MM-dd HH:mm:ssX'),
    PARSEDATETIME('2021-11-27 16:41:32Z', 'yyyy-MM-dd HH:mm:ssX')
);

-- [NORMAL USER]
INSERT INTO users (id, username, password, role, name, created_on, last_updated_on)
VALUES (
    '26ad7565-ba11-4914-bf91-84557b8b8764',
    'user',
    'test',
    'USER',
    'User Name',
    now(),
    now()
);