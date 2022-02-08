package io.testcontainers.containers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import io.testcontainers.commons.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@ActiveProfiles("aws")
@Testcontainers
@SpringBootTest
public class MessageListenerIT {
    private static final String QUEUE_NAME = "order-event-test-queue";
    private static final String BUCKET_NAME = "order-event-test-bucket";

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Container
    static LocalStackContainer localStackContainer =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.13.0"))
                    .withServices(S3, SQS);

    @BeforeAll
    static void setup() throws IOException, InterruptedException {
        localStackContainer.execInContainer("awslocal", "sqs",
                "create-queue", "--queue-name", QUEUE_NAME);
        localStackContainer.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET_NAME);
    }

    @DynamicPropertySource
    static void configuration(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("event-processing.order-event-queue", () -> QUEUE_NAME);
        propertyRegistry.add("event-processing.order-event-bucket", () -> BUCKET_NAME);
        propertyRegistry.add("cloud.aws.sqs.endpoint", () -> localStackContainer.getEndpointOverride(SQS));
        propertyRegistry.add("cloud.aws.s3.endpoint", () -> localStackContainer.getEndpointOverride(S3));
        propertyRegistry.add("cloud.aws.credentials.access-key", localStackContainer::getAccessKey);
        propertyRegistry.add("cloud.aws.credentials.secret-key", localStackContainer::getSecretKey);
    }

    @Test
    void test_send_sqs_msg_then_put_in_bucket() throws IOException, URISyntaxException {

        String data = Utils.getFileData("message.json");

        final GenericMessage<String> message = new GenericMessage<>(data,
                Map.of("contentType", "application/json"));

        queueMessagingTemplate.send(QUEUE_NAME, message);

        given()
            .ignoreException(AmazonS3Exception.class)
            .await()
            .atMost(5, SECONDS)
            .untilAsserted(() -> assertNotNull(amazonS3.getObject(BUCKET_NAME, "4")));
    }
}
