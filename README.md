# jms-tools

## DELAY ALL MESSAGES

To delay all messages, you can turn on a feature and configure the retry_wait amount of time

```
  SHOULDDELAY_FEATURE: true
  SHOULDDELAY_RETRYTHRESHOLD_INCREASEAMOUNT: 1
  SHOULDDELAY_RETRY_WAIT: 300000
```

## CHANGE IN LOGGING SETUP

Removing the logback.xml from implementation project will allow the LogConfiguration class to take over the configuration of the logger

Log Level Settings
```
CONSOLE_ROOT_LOG_LEVEL - LOG Level for all (ignoring edu.psu) packages
CONSOLE_LOG_LEVEL - LOG Level for edu.psu packages
```