package com.tirmizee.configurations;

import com.tirmizee.streams.StreamChannels;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding(StreamChannels.class)
public class KafkaStreamConfig {
}
