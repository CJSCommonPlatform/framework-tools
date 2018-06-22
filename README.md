#  Framework Tools

[![Build Status](https://travis-ci.org/CJSCommonPlatform/framework-tools.svg?branch=master)](https://travis-ci.org/CJSCommonPlatform/framework-tools) [![Coverage Status](https://coveralls.io/repos/github/CJSCommonPlatform/framework-tools/badge.svg?branch=master)](https://coveralls.io/github/CJSCommonPlatform/framework-tools?branch=master)

Tools for working with services using the microservice framework.

# Replay Tool
For instruction on how to use the replay tool, please see the [replay tool page](https://github.com/CJSCommonPlatform/framework-tools/tree/master/framework-tools-replay)

## Integration Tests
The Integraion Tests expect a running [Postgres](https://www.postgresql.org/) database, 
with the correct users and databases configured. The integration tests can be skipped if no local Postgres database is installed

To run with the Integration Tests then your postgress should be configured so, with the folowing user:

| Parameter | Vaule         |
|-----------|---------------|
| host      | **localhost** |
| port      | **5432**      |
| username  | **framework** |
| password  | **framework** |

You will then need to create three databases owned by the new **framework** user:

* __frameworkeventstore__
* __frameworkviewstore__
* __frameworkfilestore__

### Skipping Integration Tests
If you do not have access to a local posgres database then the Integration Tests can be skipped.
To skip, set the following property when running Maven:

``` 
mvn clean install -Drun.it=false
```

