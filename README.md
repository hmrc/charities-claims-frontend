
# Charities Claims Frontend

Charities Claims frontend microservice allows organisations or agents to claim tax repayments for: 
- Gift Aid
- Other income eg. bank interest
- Top-up payments under the Gift Aid Small Donations Scheme (GASDS)

It can be used by:

- Charities
- Community Amateur Sports Clubs (CASC)
- Nominee (an individual or an organisation who you authorise to submit Gift Aid or other tax repayment claims on your behalf)
- Collection Agency (A collection agency receives donations to your charity and Gift Aid declarations via their website. The agency will claim Gift Aid that it receives for your charity on eligible donations on your behalf.)
- Authorised Agent (someone who acts on your behalf, eg, your accountant, tax agent or adviser)

## Charities Claims Validation
Charities Claims frontend uses the [charities-claims-validation](https://github.com/hmrc/charities-claims-validation) service for 
- **Transformation:** Converts spreadsheet data into JSON format.
- **Validation:** Validates data against specific business rules (Gift Aid, Other Income, etc.).
- **Storage:** Tracks upload lifecycle and persists validation results.

### You can refer to the [documentation](https://confluence.tools.tax.service.gov.uk/display/RBD/4.+Charities)

## Persistence
This service uses mongodb to persist user answers.

## Requirements
This service is written in Scala using the Play framework, so needs at least a JRE to run.

JRE/JDK 11 is recommended.

The service also depends on mongodb.

## Running the service
Using Service Manager, **sm2** uses the **DASS_CHARITIES_ALL** profile to start all services with the latest tagged releases.

```bash
sm2 --start DASS_CHARITIES_ALL
```
Run ```sm2 -s ``` to check what services are running

## Launching the service locally

Run the **sm2** command below to start all the services required for the Charities Claims frontend service.

```bash
sm2 -start DASS_CHARITIES_ALL
```
Run the **sm2** command below to stop Charities Claims frontend service.

```bash
sm2 -stop CHARITIES_CLAIMS_FRONTEND
```
Run the **sm2** command below to start Charities Claims frontend service locally.
> Note: this service runs on port 8030 by default

```bash
sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes
```
***

### Running the test suite

```bash
SBT_OPTS="-Xmx4g -XX:+UseParallelGC -Xss8m" sbt clean coverage test it/test coverageReport
```
This will also generate a coverage report

### Sorting messages files

```
./scripts/sortMessages.sc --save
```
### Scalafmt

To prevent formatting failures in a GitHub pull request, run the command ```sbt scalafmtAll``` before pushing to the remote repository.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
