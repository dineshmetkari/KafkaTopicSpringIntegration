package com.stackroute.messaging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

/**
 * The messaging application demonstrates using the Spring Integration with Apache Kafka Java Configuration DSL.
 * Creates the kafka topic of given name and can send and receive messages on the topic using XML configration file.
 *
 * @author Dinesh Metkari (dineshmetkari@gmail.com)
 */
@SpringBootApplication
@EnableIntegration
@ImportResource("/xml/outbound-kafka-integration.xml")
public class MessagingAppliationXML {

    private Log log = LogFactory.getLog(getClass());

    @Bean
    @DependsOn("kafkaOutboundChannelAdapter")
    CommandLineRunner kickOff(@Qualifier("inputToKafka") MessageChannel in) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                in.send(new GenericMessage<>("XML Message: " + i));
                log.info("XML Message: " + i);
            }
        };
    }

//
//    public static void main(String args[]) {
//        SpringApplication.run(MessagingAppliationXML.class, args);
//    }
}
