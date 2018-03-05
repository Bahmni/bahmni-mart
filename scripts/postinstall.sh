#!/bin/bash

. /opt/bahmni-mart/conf/bahmni-mart-postgres.conf

manage_user_and_group() {
    #create bahmni user and group if doesn't exist
    USERID=bahmni
    GROUPID=bahmni
    /bin/id -g $GROUPID 2>/dev/null
    [ $? -eq 1 ]
    groupadd bahmni

    /bin/id $USERID 2>/dev/null
    [ $? -eq 1 ]
    useradd -g bahmni bahmni
}

create_mart_directories() {
    if [ ! -d /opt/bahmni-mart/log/ ]; then
        mkdir -p /opt/bahmni-mart/log/
    fi
}

link_directories() {
    #create links
    ln -s /opt/bahmni-mart/bin/bahmni-mart /usr/bin/bahmni-mart
    ln -s /opt/bahmni-mart/log /var/log/bahmni-mart
}

manage_permissions() {
    # permissions
    chown -R bahmni:bahmni /usr/bin/bahmni-mart
    chown -R bahmni:bahmni /opt/bahmni-mart
    chown -R bahmni:bahmni /var/log/bahmni-mart
}
setup_cronjob() {
    # adding cron job for scheduling the job at 11:30PM everyday
    crontab -u bahmni -l | { cat; echo "30 23 * * * /usr/bin/bahmni-mart >/dev/null 2>&1"; } | crontab -u bahmni -
}

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
    sed -i "s|host    all             all             127.0.0.1/32            ident|host    all             all             0.0.0.0/0            trust|g" /var/lib/pgsql/${POSTGRESQL_VERSION}/data/pg_hba.conf
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
    sudo service postgresql-${POSTGRESQL_VERSION} stop
    sudo service postgresql-${POSTGRESQL_VERSION} start
}

create_postgres_users_and_db() {
   RESULT_USER=`psql -U${ANALYTICS_DB_USER_POSTGRES} -h${ANALYTICS_DB_SERVER} -tAc "select count(*) from pg_roles where rolname='analytics'"`
   RESULT_DB=`psql -U${ANALYTICS_DB_USER_POSTGRES} -h${ANALYTICS_DB_SERVER} -tAc "select count(*) from pg_catalog.pg_database where datname='analytics'"`
   if [ "$RESULT_USER" == "0" ]; then
            echo "creating postgres user - analytics with roles CREATEDB,NOCREATEROLE,SUPERUSER,REPLICATION"
            createuser -U${ANALYTICS_DB_USER_POSTGRES}  -h${ANALYTICS_DB_SERVER} -d -R -s --replication analytics -P;
   fi
   if [ "$RESULT_DB" == "0" ]; then
            echo "creating db - analytics "
            createdb -U${ANALYTICS_DB_USER} -h${ANALYTICS_DB_SERVER} analytics;
            psql -U${ANALYTICS_DB_USER_POSTGRES} -h${ANALYTICS_DB_SERVER} -tAc "revoke all privileges on  database analytics from public";
   fi
}


#manage_user_and_group
create_mart_directories
link_directories
#manage_permissions
#setup_cronjob
init_db
update_postgresql_conf
update_pg_hba_conf
#updating_firewall_rules_to_allow_postgres_port
restart_postgres_service
create_postgres_users_and_db