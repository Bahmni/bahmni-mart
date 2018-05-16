#!/usr/bin/env bash

init_db() {
    echo "init postgresql database"
    service postgresql-${POSTGRESQL_VERSION} initdb  -D /var/lib/pgsql/data/postgresql.conf
}

update_postgresql_conf() {
    echo "update postgresql.conf"
    sed -i "/#listen_addresses = 'localhost'/c\listen_addresses = '*'" /var/lib/pgsql/${POSTGRESQL_VERSION}/data/postgresql.conf
    sed -i "/#port = ${POSTGRESQL_PORT}/c\port = ${POSTGRESQL_PORT}" /var/lib/pgsql/${POSTGRESQL_VERSION}/data/postgresql.conf
}

update_pg_hba_conf() {
    echo "update pg_hba.conf"
    sed -i "s|local   all             all                                     peer|local   all             all                                     trust|g" /var/lib/pgsql/${POSTGRESQL_VERSION}/data/pg_hba.conf
    sed -i "s|host    all             all             127.0.0.1/32            ident|host    all             all             127.0.0.1/32            trust|g" /var/lib/pgsql/${POSTGRESQL_VERSION}/data/pg_hba.conf
    sed -i "s|host    all             all             ::1/128                 ident|host    all             all             ::1/128                 trust|g" /var/lib/pgsql/${POSTGRESQL_VERSION}/data/pg_hba.conf
}

updating_firewall_rules_to_allow_postgres_port() {
    echo "allowing postgres port in firewall rules"
    sudo iptables -A INPUT -p tcp --dport  ${POSTGRESQL_PORT} -j ACCEPT -m comment --comment "POSTGRES"
    sudo service iptables save
}

restart_postgres_service() {
    echo "restarting postresql service"
    sudo chkconfig postgresql-${POSTGRESQL_VERSION} on
    sudo service postgresql-${POSTGRESQL_VERSION} start
}

create_postgres_users_and_db() {
   RESULT_USER=`psql -U${ANALYTICS_DB_USER_POSTGRES} -h${ANALYTICS_DB_SERVER} -tAc "select count(*) from pg_roles where rolname='${ANALYTICS_DB_USER}'"`
   RESULT_DB=`psql -U${ANALYTICS_DB_USER_POSTGRES} -h${ANALYTICS_DB_SERVER} -tAc "select count(*) from pg_catalog.pg_database where datname='${POSTGRES_DB_NAME}'"`
   if [ "$RESULT_USER" == "0" ]; then
            echo "creating postgres user - ${ANALYTICS_DB_USER} with roles CREATEDB,NOCREATEROLE,SUPERUSER,REPLICATION"
            createuser -U${ANALYTICS_DB_USER_POSTGRES}  -h${ANALYTICS_DB_SERVER} -d -R -s --replication ${ANALYTICS_DB_USER} -P;
   fi
   if [ "$RESULT_DB" == "0" ]; then
            echo "creating db - ${POSTGRES_DB_NAME} "
            createdb -U${ANALYTICS_DB_USER} -h${ANALYTICS_DB_SERVER} ${POSTGRES_DB_NAME};
            psql -U${ANALYTICS_DB_USER_POSTGRES} -h${ANALYTICS_DB_SERVER} -tAc "revoke all privileges on  database ${POSTGRES_DB_NAME} from public";

            echo "Creating the bahmni_mart_scdf schema"
            psql -U${ANALYTICS_DB_USER_POSTGRES} -h${ANALYTICS_DB_SERVER} -tAc "CREATE SCHEMA bahmni_mart_scdf AUTHORIZATION ${ANALYTICS_DB_USER};" ${POSTGRES_DB_NAME}
   fi
}

init_db
update_postgresql_conf
update_pg_hba_conf
updating_firewall_rules_to_allow_postgres_port
restart_postgres_service
create_postgres_users_and_db