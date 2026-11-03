package com.barsege.paymentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;

@Configuration
public class RabbitMQConfig {

    public static final String STOCK_EXCHANGE = "stock.exchange";
    public static final String STOCK_RESERVED_QUEUE = "payment.stock-reserved.queue";
    public static final String STOCK_RESERVED_ROUTING_KEY = "stock.reserved";

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";
    
    public static final String STOCK_RESERVED_DLX = "payment.stock-reserved.dlx";
    public static final String STOCK_RESERVED_DLQ = "payment.stock-reserved.dlq";
    public static final String STOCK_RESERVED_DLQ_ROUTING_KEY = "stock.reserved.failed";

    @Bean
    public TopicExchange stockExchange() {
        return new TopicExchange(STOCK_EXCHANGE);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue stockReservedQueue() {
    	return QueueBuilder
                .durable(STOCK_RESERVED_QUEUE)
                .deadLetterExchange(STOCK_RESERVED_DLX)
                .deadLetterRoutingKey(STOCK_RESERVED_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding stockReservedBinding() {
        return BindingBuilder
                .bind(stockReservedQueue())
                .to(stockExchange())
                .with(STOCK_RESERVED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
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
    		SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
    
    @Bean
    public DirectExchange stockReservedDeadLetterExchange() {
        return new DirectExchange(STOCK_RESERVED_DLX);
    }

    @Bean
    public Queue stockReservedDeadLetterQueue() {
        return QueueBuilder
                .durable(STOCK_RESERVED_DLQ)
                .build();
    }

    @Bean
    public Binding stockReservedDeadLetterBinding() {
        return BindingBuilder
                .bind(stockReservedDeadLetterQueue())
                .to(stockReservedDeadLetterExchange())
                .with(STOCK_RESERVED_DLQ_ROUTING_KEY);
    }
}