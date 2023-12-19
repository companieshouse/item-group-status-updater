# item-group-status-updater
Kafka consumer that reads from item-group-processed topic to update item group status.

## Build Requirements

In order to build `item-group-status-updater` locally you will need the following:

- Java 11
- Maven
- Git

## Environment Variables

| Name                     | Description                                                 | Mandatory | Default | Example                     |
|--------------------------|-------------------------------------------------------------|-----------|---------|-----------------------------|
| `API_URL`                | The URL used by this app to connect to the CHS API it uses. | √         | N/A     | `http://api.chs.local:4001` |
| `CHS_API_KEY`            | The API Access Key for CHS.                                 | √         | N/A     | <CHS_API_KEY>               |
| `DOCUMENT_API_LOCAL_URL` | The Document API URL that must be configured for this app.  | √         | N/A     | `NOT-USED`                  |
| `PAYMENTS_API_URL`       | The Payments API URL that must be configured for this app.  | √         | N/A     | `NOT-USED`                  |


## Endpoints

| Path                                       | Method | Description                                                         |
|--------------------------------------------|--------|---------------------------------------------------------------------|
| *`/item-group-status-updater/healthcheck`* | GET    | Returns HTTP OK (`200`) to indicate a healthy application instance. |


