# item-group-status-updater
Kafka consumer that reads from item-group-processed topic to update item group status.

## Build Requirements

In order to build `item-group-status-updater` locally you will need the following:

- Java 11
- Maven
- Git

## Environment Variables

| Name                            | Description                                                                                                                  | Mandatory | Default | Example                        |
|---------------------------------|------------------------------------------------------------------------------------------------------------------------------|-----------|---------|--------------------------------|
| `API_URL`                       | The URL used by this app to connect to the CHS API it uses.                                                                  | √         | N/A     | `http://api.chs.local:4001`    |
| `BACKOFF_DELAY`                 | The delay in milliseconds between message republish attempts.                                                                | √         | N/A     | `100`                          |
| `BOOTSTRAP_SERVER_URL`          | The URLs of the Kafka brokers that the consumers will connect to.                                                            | √         | N/A     | `kafka:9092`                   |
| `CHS_API_KEY`                   | The API Access Key for CHS.                                                                                                  | √         | N/A     | <CHS_API_KEY>                  |
| `CONCURRENT_LISTENER_INSTANCES` | The number of consumers that should participate in the consumer group. Must be equal to the number of main topic partitions. | √         | N/A     | `1`                            |
| `DOCUMENT_API_LOCAL_URL`        | The Document API URL that must be configured for this app.                                                                   | √         | N/A     | `NOT-USED`                     |
| `GROUP_ID`                      | The group ID of the main consume.                                                                                            | √         | N/A     | `item-group-status-updater`    |
| `INVALID_MESSAGE_TOPIC`         | The topic to which consumers will republish messages if any unchecked exception other than `RetryableException` is thrown.   | √         | N/A     | `item-group-processed-invalid` |
| `MAX_ATTEMPTS`                  | The maximum number of times messages will be processed before they are sent to the dead letter topic.                        | √         | N/A     | `4`                            |
| `PAYMENTS_API_URL`              | The Payments API URL that must be configured for this app.                                                                   | √         | N/A     | `NOT-USED`                     |
| `SERVER_PORT`                   | Port this application runs on when deployed.                                                                                 | √         | N/A     | `18631`                        |
| `TOPIC`                         | The topic from which the main consumer will consume messages.                                                                | √         | N/A     | `item-group-processed`         |


## Endpoints

| Path                                       | Method | Description                                                         |
|--------------------------------------------|--------|---------------------------------------------------------------------|
| *`/item-group-status-updater/healthcheck`* | GET    | Returns HTTP OK (`200`) to indicate a healthy application instance. |

