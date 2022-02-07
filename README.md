# testcontainers

## Integration tests

Integreation test should follow the [F.I.R.S.T](https://medium.com/@tasdikrahman/f-i-r-s-t-principles-of-testing-1a497acda8d6):
- **F**: Fast, it should run in a matter of seconds (or less)
- **I**: Isolated, setup should be independent therefore its result isn't influenced by any factor (e.g. dependant on the running environment).
- **R**: Repeatable, its result shouldn't change based on external factors like the environment (deterministic).
- **S**: Self-validating, will chack automatically its result (pass or fail).
- **T**: Thorough, Cover all possible paths, edge cases and try to cover every use case scenario.

Test containers allow the management of Docker containers using Java code. The containers can be used to create:

- HTTP Servers
- Databases
- Message Queues (such as RabbitMQ)
- AWS Components
- Any application that can be run as a Docker container.

## Maven configuration 

```
  <dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
  </dependency>
```
## Generic Container

> Check the GenericContainerTest.java class

[Generic containers](https://www.testcontainers.org/features/creating_container/) offers the flexible option, creating any Docker images as temporary test dependencies:

```
public static GenericContainer webServer =
            new GenericContainer("alpine:3.2")
                    .withExposedPorts(8005)
                    .withCommand("/bin/sh", "-c", "while true; do echo "
                            + "\"HTTP/1.1 200 OK\n\nHello World!\" | nc -l -p 8005; done");
```
In this case the image name, exposed port and the command executed inside the container are specified.


```
    @Test
    public void test_webserver_container_get_request_then_return_response() throws Exception {
        String address = String.format("http://%s:%d", webServer.getContainerIpAddress(),
                webServer.getMappedPort(8005));

        String response = getRequest(new URL(address));

        assertEquals(response, "Hello World!");
    }
  ```
  
## Databases (Specialized containers - Databases)

[Databases](https://www.testcontainers.org/modules/databases/) contaienrs could be an alternative for H2 or local machine running databases:

Useful for data-access-layer integration tests, the specialized containers provide extended functionalities, e.g.: getJdbcUrl, getUserName, getPassword, etc.
In this case is necessary to add the specific dependency and the database driver:

```
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>postgresql</artifactId>
  <version>${testcontainers.version}</version>
  <scope>test</scope>
</dependency>
    
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

```
    @Rule
    public JdbcDatabaseContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:9.4")
            .withInitScript("import.sql");
```
DDL and DML statements can be executed using a script file and the withInitScript("some-file.sql") method.
An alternative could be the [@Sql](https://docs.spring.io/spring-framework/docs/5.2.0.RC1/spring-framework-reference/testing.html#spring-testing-annotation-sql) annotation.
  
> check the PostgreSQLContainerWithScriptTest.java class
  
```
    @Test
    public void when_select_query_executed_then_return_result() throws SQLException {

        ResultSet resultSet = executeQuery(postgreSQLContainer,
                "SELECT * FROM person where name LIKE '%Donato Di Betto Bardi%'");
        resultSet.next();
        int id = resultSet.getInt(1);
        assertEquals(4, id);
    }

    private ResultSet executeQuery(JdbcDatabaseContainer postgreSQLContainer, String query) throws SQLException {
        String jdbcUrl = postgreSQLContainer.getJdbcUrl();
        String username = postgreSQLContainer.getUsername();
        String password = postgreSQLContainer.getPassword();

        Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
        return conn.createStatement().executeQuery(query);
    }
```

### Database containers launched via JDBC URL scheme

An alternative for launching database containers is the [JDBC URL scheme](https://www.testcontainers.org/modules/databases/jdbc/), 
as easy as modifying the JDBC connection URL:
 
 ```
 spring.datasource.url=jdbc:tc:postgresql:11.7-alpine:///
 ```

 > Check the JDBCContainerLaunchedURLSchemeTest.java class

## The LocalStack module

A fully functional local AWS cloud stack, could be used to develop and test cloud and serverless applications 
without using the cloud.

The [LocalStack](https://localstack.cloud/) container provides an empty local AWS cloud, commands can be executed inside the container for 
creating the proper resources, initialization will happen as part of the JUnit Jupiter lifecycle (**@BeforeAll**)
preparing the environment before running any test. LocalStack container offers the [awslocal](https://github.com/localstack/awscli-local) 
executable (AWS CLI wrapper) used to create the resources:

```
localStackContainer.execInContainer("awslocal", "s3", "mb", "s3://mybucket");
```
This test is using the [JUnit Jupiter Testcontainers](https://www.testcontainers.org/test_framework_integration/junit_5/) 
extension provided by the **@Testcontainers** annotation, in combination with the **Container** annotation
two modes are supported:

- Containers are restarted for every test method, by declaring the container as instance field.
- Containers will be shared between test methods, by declaring the container as static field.

With a default LocalStack configuration IAM permissions aren't validated so any access/secret key will be accepted:
```
propertyRegistry.add("cloud.aws.credentials.access-key", localStackContainer::getAccessKey);
propertyRegistry.add("cloud.aws.credentials.secret-key", localStackContainer::getSecretKey);
```
The test will send a message to the SQS queue and then will be validated into the S3 bucket:

The message:
```
{
  "id": "4",
  "name": "Notification Email",
  "details": "Medium priority notification",
  "createdAt": "2021-11-11 12:00:00",
  "priority": 2
}

final GenericMessage<String> message = new GenericMessage<>(data,
                Map.of("contentType", "application/json"));

queueMessagingTemplate.send(QUEUE_NAME, message);

given()
    .ignoreException(AmazonS3Exception.class)
    .await()
    .atMost(5, SECONDS)
    .untilAsserted(() -> assertNotNull(amazonS3.getObject(BUCKET_NAME, "4")));
```

- fix classes names - set failsafe plugin
- Docker compose
- JDBC URL Schema Launcher
- http
- @Rule and classrule
- Ryuk
- BOM
- LocalStack
- awaitility
- -JUnit Jupiter Lifecycle
- DynamicPropertyRegistry
- @DynamicPropertySource


# conclusions
Running a Docker image for every test method can take an enormous amount of time. To increase performance we need to make a real-life compromise. We can run a Docker image per class or even run once for all integration test executions. The second approach has been presented in the code. If we decide to share Docker images between tests, we need to be ready for it. There are many ways to achieve it

- Tests should operate on unique IDs, names, etc. That way, we can avoid collisions of database constraints. In this case, you don’t need to clean up after the test execution. Some problems can occur, for example when you count elements in the database table. You can count elements created by different tests.
- Tests should clean up the state after execution. This approach consumes much more development time and is error-prone.

Pros and Cons
Option one is using real AWS services for our tests and hence making sure the application can work with them. It has the downside of additional AWS costs
You run tests against real components, for example, the PostgreSQL database instead of the H2 database, which doesn’t support the Postgres-specific functionality (e.g. partitioning or JSON operations).
You can mock AWS services with Localstack or Docker images provided by AWS. It will simplify administrative actions, cut costs and make your build offline.
You can run your tests offline - no Internet connection is needed. It is an advantage for people who are traveling or if you have a slow Internet connection (when you have already run them once and there is no version change in the container).
You can test corner cases in HTTP communication like:
programmatically simulate timeout from external services (e.g. by configuring MockServer to respond with a delay that is bigger than the timeout set in your HTTP client),
simulate HTTP codes that are not explicitly supported by our application.
Implementation and tests can be written by developers and exposed in the same pull request by backend developers.
Even one integration test can verify if your application context starts properly and your database migration scripts (e.g. Flyway) are executing correctly.

Disadvantages of using the TestContainers library
We bring another dependency to our system that you need to maintain.
You need to run containers at least once - it consumes time and resources. For example, PostgreSQL as a Docker image needs around 4 seconds to start on my machine, whereas the H2 in-memory database needs only 0.4 seconds. From my experience, Localstack which emulates AWS components, can start much longer, even 20 seconds on my machine.
A continuous integration (e.g. Jenkins) machine needs to be bigger (build uses more RAM and CPU).
Your local computer should be pretty powerful. If you run many Docker images, it can consume a lot of resources.
Sometimes, integration tests with TestContainers are still not sufficient. For example, while testing REST responses with a mockserver container you can miss changes of real API. Inside the integration test, you may not reflect it, and your code still can crash on production. To minimize the risk, you may consider leveraging Contract Testing via Spring Cloud Contract.
