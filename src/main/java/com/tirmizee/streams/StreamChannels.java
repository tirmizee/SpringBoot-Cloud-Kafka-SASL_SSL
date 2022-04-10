package com.tirmizee.streams;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface StreamChannels {

    @Input("customerConsumer")
    SubscribableChannel customerConsumer();

    @Output("customerProducer")
    MessageChannel customerProducer();

}
