FROM ubuntu:16.04

MAINTAINER Eugeny Morozov

RUN apt-get -y update

# Установка postgre

ENV PGVER 9.5

RUN apt-get install -y postgresql-$PGVER

# Run the rest of the commands as the ``postgres`` user created by the ``postgres-$PGVER`` package when it was ``apt-get installed``
USER postgres

# Create a PostgreSQL role named ``docker`` with ``docker`` as the password and
# then create a database `docker` owned by the ``docker`` role.
RUN /etc/init.d/postgresql start &&\
    psql --command "CREATE USER docker WITH SUPERUSER PASSWORD 'docker';" &&\
    createdb -E UTF8 -T template0 -O docker docker &&\
    /etc/init.d/postgresql stop

# Adjust PostgreSQL configuration so that remote connections to the
# database are possible.
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/$PGVER/main/pg_hba.conf

# And add ``listen_addresses`` to ``/etc/postgresql/$PGVER/main/postgresql.conf``
RUN echo "listen_addresses='*'" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "synchronous_commit = off" >> /etc/postgresql/$PGVER/main/postgresql.conf

RUN echo "shared_buffers = 256MB" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "effective_cache_size = 768MB" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "work_mem = 26MB" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "maintenance_work_mem = 64MB" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "wal_buffers = 8MB" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "default_statistics_target = 100" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "random_page_cost = 1.1" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "effective_io_concurrency = 200" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "min_wal_size = 1GB" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "max_wal_size = 2GB" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "checkpoint_completion_target = 0.7" >> /etc/postgresql/$PGVER/main/postgresql.conf
# Expose the PostgreSQL port
EXPOSE 5432

# Add VOLUMEs to allow backup of config, logs and databases
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

# Back to the root user
USER root

# Установка JDK
RUN apt-get install -y openjdk-9-jdk-headless
RUN apt-get install -y maven


# Копируем исходный код в Docker-контейнер
ENV WORK /opt/
ADD / $WORK/


# Собираем и устанавливаем пакет
WORKDIR $WORK
RUN mvn package


# Объявлем порт сервера
EXPOSE 5000

#
# Запускаем PostgreSQL и сервер
#
CMD service postgresql start && java -Xmx300M -Xmx300M -jar $WORK/target/eugenm.jar
