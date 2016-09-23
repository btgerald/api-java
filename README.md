# api-php

iMeet® Central API Sample Code for Java

Fill in config/client_config.yml with your credentials.   To grab credentials, please see the API tab on the "Company Admin" sections on your account.

This project's dependencies are managed by Maven. Maven can also be used to run the API samples.

```mvn clean compile```

Get an access token
```mvn exec:java```

Get the results of a single API endpoint
```mvn exec:java -Dexec.args="/v1/users"```

Get the results of a multiple API endpoint
```mvn exec:java -Dexec.args="/v1/users /v1/workspaces"```