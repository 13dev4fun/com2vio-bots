package com.com2vio.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
class KafkaTopicConfig {
    @Bean
    public NewTopic repo() {
        return TopicBuilder.name("repo").build();
    }

    @Bean
    public NewTopic pull() {
        return TopicBuilder.name("pull").build();
    }

    @Bean
    public NewTopic file() {
        return TopicBuilder.name("file").build();
    }

    @Bean
    public NewTopic status() {
        return TopicBuilder.name("status").build();
    }

    @Bean
    public NewTopic matchEvent() {
        return TopicBuilder.name("match-event").build();
    }

    @Bean
    public NewTopic labelEvent() {
        return TopicBuilder.name("label-event").build();
    }

    @Bean
    public NewTopic recommendEvent() {
        return TopicBuilder.name("recommend-event").build();
    }
}
