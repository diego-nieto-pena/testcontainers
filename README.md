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
## Elasticsearch Module (extending specialized containers)

Based on the official Docker image provided by elastic, the example in the **ElasticsearchIT.java** class shows
an alternative for configuring containers by extending specialized container classes:

```
public class ElasticsearchTestContainer extends ElasticsearchContainer {
    private static final String DOCKER_ELASTIC = "docker.elastic.co/elasticsearch/elasticsearch:7.11.2";

    private static final String CLUSTER_NAME = "local-cluster";

    private static final String ELASTIC_SEARCH = "elasticsearch";

    public ElasticsearchTestContainer() {
        super(DOCKER_ELASTIC);
        this.addFixedExposedPort(9200, 9200);
        this.addFixedExposedPort(9300, 9300);
        this.addEnv(CLUSTER_NAME, ELASTIC_SEARCH);
    }
}
```

## Waiting for the containers to be ready

#### Wait for an HTTP(S) endpoint to return a particular status code
```
public static GenericContainer myServer = new GenericContainer("alpine:3.2")
                    .withExposedPorts(8005)
                    .waitingFor(Wait.forHttp("/")
                            .forStatusCode(200));
```
#### Wait for multiple possible status codes
```
Wait.forHttp("/")
    .forStatusCode(200)
    .forStatusCode(301)
```
### Wait for Log output strategy
In some cases a log output is a simple way to determine the container status
```
public static GenericContainer myServer = new GenericContainer("alpine:3.2")
                    .withExposedPorts(8005)
                    .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1));
```

Other wait strategies can be checked at the [**Wait**](https://www.javadoc.io/doc/org.testcontainers/testcontainers/latest/org/testcontainers/containers/wait/strategy/Wait.html) class 
or the various subclasses of [**WaitStrategy**](https://www.javadoc.io/doc/org.testcontainers/testcontainers/latest/org/testcontainers/containers/wait/strategy/WaitStrategy.html)

## Startup check strategies

All logic is implemented in [**StartupCheckStrategy**](https://www.javadoc.io/doc/org.testcontainers/testcontainers/latest/org/testcontainers/containers/startupcheck/StartupCheckStrategy.html) child classes.

The strategy used by default Testcontainers just checks if the container is running, [**IsRunningStartupCheckStrategy**](https://www.javadoc.io/doc/org.testcontainers/testcontainers/latest/org/testcontainers/containers/startupcheck/IsRunningStartupCheckStrategy.html) implemented in class

#### One shot startup strategy example

Targeted for containers that runs for a short period of time, success is considered when the container has stopped with exit code 0.
```
public GenericContainer<?> bboxWithOneShot = new GenericContainer<>(DockerImageName.parse("busybox:1.31.1"))
    .withCommand(String.format("echo %s", HELLO_TESTCONTAINERS))
    .withStartupCheckStrategy(
        new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(3))
    )
```
#### Indefinite one shot startup strategy example
Variant of one shot strategy that does not impose a timeout. For situations such as when a long-running task forms a part of container startup.
```
public GenericContainer<?> bboxWithIndefiniteOneShot = new GenericContainer<>(DockerImageName.parse("busybox:1.31.1"))
    .withCommand("sh", "-c", String.format("sleep 5 && echo \"%s\"", HELLO_TESTCONTAINERS))
    .withStartupCheckStrategy(
        new IndefiniteWaitOneShotStartupCheckStrategy()
    );
```
#### Minimum duration startup strategy
Checks if the container is running and has been running for a minimum period of time
```
    public static GenericContainer myServer =
            new GenericContainer("alpine:3.2")
                    .withExposedPorts(8005)
                    .withCommand("/bin/sh", "-c", "while true; do echo "
                            + "\"HTTP/1.1 200 OK\n\nHello World!\" | nc -l -p 8005; done")
                    .withStartupCheckStrategy(
                            new MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(1))
                    );
```
Before running any containers Testcontainers will perform a set of startup validations, ensuring the correct environment configuration.
```
        ℹ︎ Checking the system...
        ✔ Docker version should be at least 1.6.0
        ✔ Docker environment should have more than 2GB free disk space
        ✔ File should be mountable
        ✔ A port exposed by a docker container should be accessible

```
Those validations take some seconds, avoiding these validations is possible by adding **checks.disable=true**
in the **$HOME/.testcontainers.properties**.

## Manual container lifecycle control

Containers can be started and stopped by using the **start()** and **stop()** methods, additionally container classes implement **AutoClosable**.
```
try (GenericContainer container = new GenericContainer("imagename")) {
    container.start();
    // ... container usage
}
```
## Ryuk container 
[Ryuk](https://github.com/testcontainers/moby-ryuk) is the resource reaper is responsible for container removal and automatic cleanup of dead containers at JVM shutdown.
Ryuk must be started as a **privileged container**, if there is already an implemented container cleanup strategy after the execution,
you can turn off the Ryuk container by setting **TESTCONTAINERS_RYUK_DISABLED** environment variable to **true**.

# Pros

- Run tests again real components, e.g. H2 doesn't support postgreSQL-specific functionality. 
- Mock AWS services with LocalStack will simplify administrative actions, lower costs and provide an offline environment.
- Test corner cases; HTTP communication timeouts, simulated unexpected response codes.
- Homogeneous environment configuration for all developers.
- Applications with several microservices and dependencies could be mocked easily.
- A single container can be run for all integration test executions.
- Startup validations could verify if the application context starts properly and DB migration scripts are executing as expected.

# Cons

- Bring additional dependencies to maintain.
- Running a Docker image for every test method can take an enormous amount of time and resources.
- For avoiding conflicts/collisions tests should operate on unique IDs, names, etc.
- Resources cleanup is error-prone. 
- Containers startup time is higher than other strategies e.g. PostgreSQl takes much more time than H2 to start.
- May be necessary to scale up on hardware for continuos integration. 
- Local machine should be a good one in order to perform well.