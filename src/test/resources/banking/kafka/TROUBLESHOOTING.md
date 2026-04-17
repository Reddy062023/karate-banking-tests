\# Section 2.11 - Kafka Troubleshooting Guide



\## Common Issues, Root Causes and Fixes



\---



\### Issue 1: Consumer Lag Grows Continuously

Symptom:  Messages piling up, consumers falling behind

Cause:    Consumers too slow, DB writes blocking

Fix:      Add more consumers (max = partition count)

&#x20;         Optimize DB writes using async processing

Diagnostic:

&#x20; kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-group



We experienced this today:

&#x20; Our KafkaTestHelper used unique group.id each time

&#x20; This meant always reading from offset 0 (earliest)

&#x20; In production: monitor consumer lag with Grafana



\---



\### Issue 2: Messages Lost Silently

Symptom:  Producer thinks message sent but consumer never gets it

Cause:    acks=0 or acks=1 with broker restart

Fix:      acks=all, min.insync.replicas=2, retries=3

Diagnostic:

&#x20; Check producer logs for TimeoutException



We fixed this in our OrderEventProducer.java:

&#x20; p.put(ProducerConfig.ACKS\_CONFIG, "all")

&#x20; p.put(ProducerConfig.RETRIES\_CONFIG, 3)

&#x20; p.put(ProducerConfig.ENABLE\_IDEMPOTENCE\_CONFIG, true)



\---



\### Issue 3: Duplicate Messages Processed

Symptom:  Same order processed twice, double charge

Cause:    Consumer crashed before offset commit

Fix:      enable.idempotence=true on producer

&#x20;         Make consumer logic idempotent (upsert not insert)

Diagnostic:

&#x20; Check for duplicate orderId in DB



We tested this in Section 2.10 Scenario 3:

&#x20; enable.idempotence=true prevents duplicate Kafka messages

&#x20; In Instore: prevents double-charging customer



\---



\### Issue 4: Deserialization Error on Consumer

Symptom:  Consumer throws SerializationException

Cause:    Producer schema changed without consumer update

Fix:      Use Schema Registry + BACKWARD\_TRANSITIVE compatibility

Diagnostic:

&#x20; Check for SerializationException in consumer logs

&#x20; curl http://localhost:8081/subjects



We experienced this today:

&#x20; Schema Registry connection refused when not running

&#x20; Fixed by starting schema-registry container

&#x20; Tested backward compatibility with curl command



\---



\### Issue 5: SSL Handshake Failure

Symptom:  Producer/consumer cannot connect to broker

Cause:    Wrong truststore/keystore path or expired cert

Fix:      Verify ssl.truststore.location and ssl.truststore.password

Diagnostic:

&#x20; kafka-broker-api-versions --bootstrap-server localhost:9092



In Instore:

&#x20; Production Kafka uses SSL + SASL authentication

&#x20; DevOps team manages certificates

&#x20; QA uses non-SSL local environment for testing



\---



\### Issue 6: Consumer Rebalancing Loop

Symptom:  Consumer logs show Rebalancing continuously

Cause:    max.poll.interval.ms too low for processing time

Fix:      Increase to 300000ms (5 minutes)

&#x20;         Reduce max.poll.records to process faster

Diagnostic:

&#x20; Consumer logs show "Rebalancing..."



We saw this today:

&#x20; Consumer logs showed rebalancing messages

&#x20; Fixed with SESSION\_TIMEOUT\_MS\_CONFIG = 30000

&#x20; MAX\_POLL\_INTERVAL\_MS\_CONFIG = 300000



\---



\### Issue 7: Topic Not Found Error

Symptom:  LEADER\_NOT\_AVAILABLE or UnknownTopicException

Cause:    auto.create.topics.enable=false or topic not pre-created

Fix:      Create topic manually before consumers start

&#x20;         Or set KAFKA\_AUTO\_CREATE\_TOPICS\_ENABLE=true

Diagnostic:

&#x20; kafka-topics --bootstrap-server localhost:9092 --list



We experienced this today:

&#x20; LEADER\_NOT\_AVAILABLE warning in CI logs

&#x20; Fixed by adding KAFKA\_AUTO\_CREATE\_TOPICS\_ENABLE=true

&#x20; and sleep 5 in workflow to wait for Kafka ready



\---



\### Issue 8: Offset Commit Fails

Symptom:  CommitFailedException in consumer logs

Cause:    Session timeout exceeded during processing

Fix:      session.timeout.ms=45000

&#x20;         heartbeat.interval.ms=10000

&#x20;         Process messages faster

Diagnostic:

&#x20; CommitFailedException in consumer logs



We set this in OrderEventConsumer.java:

&#x20; p.put(ConsumerConfig.SESSION\_TIMEOUT\_MS\_CONFIG, 30000)

&#x20; p.put(ConsumerConfig.MAX\_POLL\_INTERVAL\_MS\_CONFIG, 300000)



\---



\### Issue 9: High Producer Latency

Symptom:  Messages taking too long to send

Cause:    linger.ms=0 sends each message individually

Fix:      linger.ms=5-20ms for batching

&#x20;         Increase batch.size

&#x20;         Enable compression (snappy)

Diagnostic:

&#x20; Check producer metrics for record-send-rate



We optimized this in OrderEventProducer.java:

&#x20; p.put(ProducerConfig.LINGER\_MS\_CONFIG, 5)

&#x20; p.put(ProducerConfig.BATCH\_SIZE\_CONFIG, 16384)

&#x20; p.put(ProducerConfig.COMPRESSION\_TYPE\_CONFIG, "snappy")



\---



\### Issue 10: Broker Unreachable

Symptom:  Connection refused or NetworkException

Cause:    Wrong bootstrap.servers host:port or firewall

Fix:      Check hostname and port

&#x20;         Check Docker network configuration

Diagnostic:

&#x20; nc -z localhost 9092

&#x20; kafka-broker-api-versions --bootstrap-server localhost:9092



We experienced this today in CI:

&#x20; Kafka container was running but port not mapped

&#x20; Fixed by adding ports: 9092:9092 to docker-compose

&#x20; and KAFKA\_ADVERTISED\_LISTENERS: PLAINTEXT://kafka:29092



\---



\## Key Diagnostic Commands



Test Kafka connectivity:

&#x20; kafka-broker-api-versions --bootstrap-server localhost:9092



Check consumer group lag:

&#x20; kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-group



List all topics:

&#x20; kafka-topics --bootstrap-server localhost:9092 --list



Describe specific topic:

&#x20; kafka-topics --bootstrap-server localhost:9092 --describe --topic order-events



Check Kafka logs for errors:

&#x20; docker logs kafka 2>\&1 | grep -iE "error|exception" | tail -50



Check Schema Registry subjects:

&#x20; curl http://localhost:8081/subjects



Check Schema Registry latest schema:

&#x20; curl http://localhost:8081/subjects/order-events-avro-value/versions/latest



Reset consumer offset to earliest:

&#x20; kafka-consumer-groups --bootstrap-server localhost:9092 \\

&#x20;   --group my-group --topic order-events --reset-offsets \\

&#x20;   --to-earliest --execute



\---



\## Issues We Fixed During This Project



Issue                        How We Fixed It

\-------------------------------------------------------------

CI Kafka unreachable         Added port mapping to workflow

Schema Registry not found    Fixed KAFKA\_ADVERTISED\_LISTENERS

Consumer closes immediately  Added sleep + increased timeout

GraalVM method not found     Changed to static Java methods

Allure showing 0 tests       Changed allure\_results path

karate-config ci-api URL     Changed to localhost:8090



\---



\## Interview Tips



Q: What do you do when a Kafka consumer stops processing?

A: First check consumer group lag using kafka-consumer-groups command.

&#x20;  Then check consumer logs for exceptions.

&#x20;  Common causes: processing timeout, DB connection issue, rebalancing loop.



Q: How do you prevent message loss in Kafka?

A: Set acks=all on producer, min.insync.replicas=2 on broker,

&#x20;  retries=3 on producer, enable.idempotence=true.



Q: What is the difference between at-least-once and exactly-once?

A: At-least-once: message delivered at least once, possible duplicates.

&#x20;  Exactly-once: enable.idempotence=true + transactions, no duplicates.

&#x20;  In Instore: we use at-least-once with idempotent consumer logic.



Q: How do you debug a consumer rebalancing loop?

A: Check max.poll.interval.ms vs actual processing time.

&#x20;  If processing takes longer than max.poll.interval.ms,

&#x20;  consumer is kicked out of group and rebalancing starts.

&#x20;  Fix: increase max.poll.interval.ms or reduce batch size.

