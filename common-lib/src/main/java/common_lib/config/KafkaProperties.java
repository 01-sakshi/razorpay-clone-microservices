package common_lib.config;

import common_lib.enums.EventAggregateType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties(prefix = "app.kafka")
@Component
@Getter
@Setter
public class KafkaProperties {

    private Map<String, String> topics;

    /**
     * Looks up the topic configured under the lower-case aggregate name.
     *
     * @param aggregateType event aggregate whose topic is required
     * @return the configured Kafka topic
     * @throws IllegalStateException if no topic is configured for the aggregate
     */
    public String topicFor(EventAggregateType aggregateType) {
        String topic = topics.get(aggregateType.name().toLowerCase());
        if (topic == null) {
            throw new IllegalStateException("No kafka topic is configured for aggregate type: " + aggregateType);
        }
        return topic;
    }
}
