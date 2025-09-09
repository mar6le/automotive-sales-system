-- Create additional database for Keycloak (automotive_sales already exists via POSTGRES_DB)
CREATE DATABASE keycloak;

-- Create users
CREATE USER keycloak_user WITH PASSWORD 'keycloak_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak_user;
GRANT ALL PRIVILEGES ON DATABASE automotive_sales TO automotive_user;

-- Connect to keycloak database and grant schema privileges
\c keycloak;
GRANT ALL ON SCHEMA public TO keycloak_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO keycloak_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO keycloak_user;

-- Connect to automotive_sales database and grant schema privileges
\c automotive_sales;
GRANT ALL ON SCHEMA public TO automotive_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO automotive_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO automotive_user;
