
# charities-claims-frontend

***

## Technical documentation

### Before running the app

Run the following command to start all the related services for this project:
```bash
sm2 -start DASS_CHARITIES_ALL
```
Included in the above command is `CHARITIES_CLAIMS_FRONTEND`, which is this repository's most recent release.
If you want to run your local version of this code instead, run:
```bash
sm2 -stop CHARITIES_CLAIMS_FRONTEND
```

then:
> Note: this service runs on port 8030 by default

```bash
sbt 'run'
```

***

### Running the test suite

```bash
SBT_OPTS="-Xmx4g -XX:+UseParallelGC -Xss8m" sbt clean coverage test it/test coverageReport
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").