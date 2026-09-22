-- Runs on first initialization; also safe to run manually on an existing cluster.
SELECT 'CREATE DATABASE kelarus_account'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'kelarus_account')\gexec
