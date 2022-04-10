# SpringBoot-Cloud-Kafka-SASL_SSL

### create certs command

    rm -rf secrets
    mkdir secrets
    echo "datahub" > secrets/cert_creds

    keytool -genkey -noprompt \
         -alias broker \
         -dname "CN=localhost, OU=test, O=datahub, L=paris, C=fr" \
         -keystore secrets/broker.keystore.jks \
         -keyalg RSA \
         -storepass datahub \
         -storetype JKS \
         -keypass datahub  >/dev/null 2>&1

    keytool -keystore secrets/broker.keystore.jks -alias broker -certreq -file secrets/broker.csr -storepass datahub -keypass datahub >/dev/null 2>&1

    openssl req -new -x509 -keyout secrets/datahub-ca.key -out secrets/datahub-ca.crt -days 365 -subj '/CN=localhost/OU=test/O=datahub/L=paris/C=fr' -passin pass:datahub -passout pass:datahub >/dev/null 2>&1

    openssl x509 -req -CA secrets/datahub-ca.crt -CAkey secrets/datahub-ca.key -in secrets/broker.csr -out secrets/broker-ca-signed.crt -days 365 -CAcreateserial -passin pass:datahub  >/dev/null 2>&1

    keytool -keystore secrets/broker.keystore.jks -alias CARoot -import -noprompt -file secrets/datahub-ca.crt -storepass datahub -keypass datahub >/dev/null 2>&1

    keytool -keystore secrets/broker.keystore.jks -alias broker -import -file secrets/broker-ca-signed.crt -storepass datahub -keypass datahub >/dev/null 2>&1

    keytool -keystore secrets/broker.truststore.jks -storetype JKS -alias CARoot -import -noprompt -file secrets/datahub-ca.crt -storepass datahub -keypass datahub >/dev/null 2>&1


### docker-compose-sasl-ssl-plain.yaml

```yaml

version: '3'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.1.0
    container_name: zookeeper
    hostname: zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_SERVER_ID: 1
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka-ssl:
    image: confluentinc/cp-kafka:7.1.0
    container_name: kafka-ssl
    hostname: kafka-ssl
    ports:
      - "9093:9093"
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      ZOOKEEPER_SASL_ENABLED: FALSE
      KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://localhost:9092,SASL_SSL://localhost:9093'
      KAFKA_SASL_ENABLED_MECHANISMS: PLAIN
      KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL: PLAIN
      KAFKA_SECURITY_INTER_BROKER_PROTOCOL: PLAINTEXT
      KAFKA_SSL_KEYSTORE_FILENAME: broker.keystore.jks
      KAFKA_SSL_KEYSTORE_CREDENTIALS: cert_creds
      KAFKA_SSL_KEY_CREDENTIALS: cert_creds
#      KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM:
      KAFKA_OPTS: -Djava.security.auth.login.config=/etc/kafka/configs/kafka_jaas.conf
    volumes:
      - ./config:/etc/kafka/configs
      - ./config:/etc/kafka/secrets

```


### application-sasl-ssl-plain.yaml

```yaml

spring:
  cloud:
    stream:
      binders:
        kafka-plain:
          type: kafka
          environment.spring.cloud.stream.kafka.binder:
            brokers: localhost:9093
            autoAddPartitions: true
            autoCreateTopics: true
            configuration:
              security.protocol: SASL_SSL
              ssl.enabled.protocols: TLSv1.2
              ssl.truststore.type: JKS
              ssl.truststore.location: /Users/pratya.yeekhaday/Desktop/Workshop/SpringBoot-Cloud-Kafka-SASL_SSL/config/broker.truststore.jks
              ssl.truststore.password: datahub
              sasl.mechanism: PLAIN
              sasl.jaas.config: "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"test\" password=\"testtest\";"
      bindings:
        customerConsumer:
          binder: kafka-plain
          group: Customer.Group
          destination: topic.customer
        customerProducer:
          binder: kafka-plain
          destination: topic.customer

```