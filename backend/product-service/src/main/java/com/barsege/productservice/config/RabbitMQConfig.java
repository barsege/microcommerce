package com.barsege.productservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_CREATED_QUEUE = "product.order-created.queue";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    public static final String STOCK_EXCHANGE = "stock.exchange";
    public static final String STOCK_RESERVED_ROUTING_KEY = "stock.reserved";
    public static final String STOCK_RESERVATION_FAILED_ROUTING_KEY = "stock.reservation.failed";
    
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_FAILED_QUEUE = "product.payment-failed.queue";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";
    
    public static final String PAYMENT_COMPLETED_QUEUE = "product.payment-completed.queue";
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange stockExchange() {
        return new TopicExchange(STOCK_EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
    
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE).build();
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder
                .bind(paymentFailedQueue())
                .to(paymentExchange())
                .with(PAYMENT_FAILED_ROUTING_KEY);
    }
    
    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE).build();
    }

    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder
                .bind(paymentCompletedQueue())
                .to(paymentExchange())
                .with(PAYMENT_COMPLETED_ROUTING_KEY);
    }
}