docker-compose up -d

docker exec -it <postgres_container_id> psql -U docker -W exampledb


# Creata table

CREATE TABLE customers (
  id integer,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  PRIMARY KEY(id)
);

ALTER TABLE public.customers REPLICA IDENTITY FULL;

INSERT INTO customers (id, first_name, last_name, email) VALUES (1, 'John', 'doe', 'jdTest@yopmail.com');


GET http://localhost:8083/connectors

POST http://localhost:8083/connectors
{
    "name": "postgres-source-connector",
    "config": {
        "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
        "plugin.name": "pgoutput",
        "database.hostname": "postgres",
        "database.port": "5432",
        "database.user": "docker",
        "database.password": "docker",
        "database.dbname": "exampledb",
        "database.server.name": "postgres",
        "key.converter.schemas.enable": "false",
        "value.converter.schemas.enable": "false",
        "transforms": "unwrap",
        "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState",
        "value.converter": "org.apache.kafka.connect.json.JsonConverter",
        "key.converter": "org.apache.kafka.connect.json.JsonConverter",
        "table.include.list": "public.customers",
        "slot.name" : "dbz_sales_transaction_slot"

    }
}



docker exec -it <kafka_container_id>  bash

kafka-topics --list --zookeeper zookeeper:2181

kafka-console-consumer --bootstrap-server localhost:9092 --topic postgres.public.customers --from-beginning