package org.interview.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "MESSAGES")
public class Message implements Serializable {
    @Id
    @Column(name = "MESSAGE")
    private String message;
    @Column(name = "LAST_UPDATE")
    private LocalDateTime lastUpdate;

    public Message() {
    }

    public Message(String message, LocalDateTime lastUpdate) {
        this.message = message;
        this.lastUpdate = lastUpdate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equals(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, lastUpdate);
    }

    @Override
    public String toString() {
        String formattedDate = lastUpdate != null
                ? lastUpdate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : "null";
        return "Message{" +
                "message='" + message + '\'' +
                ", lastUpdate=" + formattedDate +
                '}';
    }
}