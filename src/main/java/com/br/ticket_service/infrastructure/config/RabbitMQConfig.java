package com.br.ticket_service.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TICKET_CREATED_QUEUE = "ticket.created.queue";
    public static final String TICKET_EXCHANGE = "ticket.exchange";
    public static final String TICKET_ROUTING_KEY = "ticket.created.rk";

    @Bean
    public Queue ticketQueue() {
        return QueueBuilder.durable(TICKET_CREATED_QUEUE).build();
    }

    @Bean
    public TopicExchange ticketExchange() {
        return new TopicExchange(TICKET_EXCHANGE);
    }

    @Bean
    public Binding ticketBinding(Queue ticketQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(ticketQueue).to(ticketExchange).with(TICKET_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}