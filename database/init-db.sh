#!/usr/bin/env sh

set -e

if [ -z "$POSTGRES_PASSWORD" ]; then
  echo "ERROR: Missing environment variables. Set value for '$POSTGRES_PASSWORD'."
  exit 1
fi

echo "Creating locrefdata database . . ."

psql -v ON_ERROR_STOP=1 --username postgres --dbname postgres <<-EOSQL
  CREATE ROLE locrefdata WITH PASSWORD 'locrefdata';
  CREATE DATABASE locrefdata ENCODING = 'UTF-8' CONNECTION LIMIT = -1;
  GRANT ALL PRIVILEGES ON DATABASE locrefdata TO locrefdata;
  ALTER ROLE locrefdata WITH LOGIN;
EOSQL

echo "Done creating database locrefdata."
