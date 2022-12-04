package com.taxi.partner.reviewservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * Created by vivek on 01/12/22.
 */
@Configuration
public class JmsConfig {
    public static final String REVIEW_REQUEST_QUEUE = "review-request" ;
    public static final String REVIEW_STATUS_QUEUE = "review-status" ;
    public static final String VALIDATE_REVIEW_QUEUE = "validate-order";
    public static final String VALIDATE_REVIEW_RESPONSE_QUEUE = "validate-order-response";
    public static final String VERIFY_REVIEW_QUEUE = "allocate-order";
    public static final String VERIFY_REVIEW_RESPONSE_QUEUE = "allocate-order-response";
    public static final String VERIFICATION_FAILURE_QUEUE = "allocation-failure";
    public static final String APPLICATION_CANCEL_QUEUE = "deallocate-order" ;

    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
