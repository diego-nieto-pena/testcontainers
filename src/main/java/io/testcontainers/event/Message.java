package io.testcontainers.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Message {
    private String id;
    private String name;
    private String details;
    private int priority;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonCreator
    public Message(String id, String name, String details, LocalDateTime createdAt, int priority) {
        this.id = id;
        this.name = name;
        this.details = details;
        this.createdAt = createdAt;
        this.priority = priority;
    }

}
