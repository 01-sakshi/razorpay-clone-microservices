package common_lib.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/* To enable load of KafkaProperties configuration properties file */
@AutoConfiguration
@EnableConfigurationProperties(KafkaProperties.class)
public class SharedKafkaAutoConfiguration {
}
