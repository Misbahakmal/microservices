CREATE SCHEMA customer_schema;
CREATE SCHEMA order_schema;
CREATE SCHEMA notification_schema;


CREATE USER customer_app WITH PASSWORD 'customer123';
CREATE USER order_app WITH PASSWORD 'order123';
CREATE USER notification_app WITH PASSWORD 'notification123';


GRANT ALL PRIVILEGES ON SCHEMA customer_schema TO customer_app;
GRANT ALL PRIVILEGES ON SCHEMA order_schema TO order_app;
GRANT ALL PRIVILEGES ON SCHEMA notification_schema TO notification_app;


ALTER SCHEMA customer_schema OWNER TO customer_app;
ALTER SCHEMA order_schema OWNER TO order_app;
ALTER SCHEMA notification_schema OWNER TO notification_app;