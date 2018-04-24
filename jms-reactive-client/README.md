# JMS Reactive Client

## Configuration

The following properties determine the operation of the JMS Reactive Client
if the configuration is being loaded from properties (the client can also be
configured programmatically).  It's also worth noting that these values can
be set using environment variables.  Replace the periods with underscores and
upper-case the string to determine the effective environment variable name
(e.g. the property name ``broker.url`` would be equivalent to the
``BROKER_URL`` environment variable).

| Property Name          | Description                   | Required | Default |
| ---------------------- | ----------------------------- | -------- |-------- |
| broker.url             |                               | True     |         |
| queue.name             |                               | True     |         |
| broker.username        |                               | True     |         |
| broker.password        |                               | True     |         |
| broker.retry.threshold |                               | True     | 3       |
| error.transport.name   |                               | False    |         |
| error.transport.type   |                               | False    | QUEUE   |

## Logging

The JMS Reactive Client specifies only ``slf4j-api`` as a dependency.  The
application built using this library should add an implementation if output
logging is desired.  Test logging is configured so that executing tests can
output to the console (Note that this is not encouraged by the JUnit 5 API).
