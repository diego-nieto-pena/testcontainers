package io.testcontainers.listener;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import io.testcontainers.event.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MessageListener.class);

    private final AmazonS3 amazonS3;
    private final ObjectMapper objectMapper;
    private final String eventBucket;

    public MessageListener(
            @Value("${event-processing.order-event-bucket}") String eventBucket,
            AmazonS3 amazonS3,
            ObjectMapper objectMapper) {
        this.amazonS3 = amazonS3;
        this.objectMapper = objectMapper;
        this.eventBucket = eventBucket;
    }

    @SqsListener(value = "${event-processing.order-event-queue}")
    public void processMessage(@Payload Message event) throws JsonProcessingException {
        LOG.info("Incoming message: '{}'", event);

        amazonS3.putObject(eventBucket, event.getId(), objectMapper.writeValueAsString(event));

        LOG.info("Successfully uploaded message to S3");
    }
}
