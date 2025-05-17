package com.example.micro.config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.jms.Queue;

@Configuration
@EnableJms
public class JmsConfig {

    private static final Logger logger = LoggerFactory.getLogger(JmsConfig.class);

    public static final String WORKLOAD_QUEUE = "workload-queue";
    public static final String WORKLOAD_DLQ = "workload-dlq";

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.password}")
    private String password;

    @Value("${spring.jms.listener.concurrency:1}")
    private int concurrency;

    @Value("${spring.jms.listener.max-concurrency:5}")
    private int maxConcurrency;

    @Bean
    public Queue workloadQueue() {
        return new ActiveMQQueue(WORKLOAD_QUEUE);
    }

    @Bean
    public ActiveMQQueue workloadDlq() {
        return new ActiveMQQueue(WORKLOAD_DLQ);
    }


    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        // Configure for horizontal scaling with multiple concurrent consumers
        factory.setConcurrency(concurrency + "-" + maxConcurrency);

        // Use transactions for better message handling
        factory.setSessionTransacted(true);

        // Configure error handling with logging
        factory.setErrorHandler(t -> {
            logger.error("Error in JMS message processing: {}", t.getMessage(), t);
        });

        // Set destination resolver
        factory.setDestinationResolver(new DynamicDestinationResolver());

        configurer.configure(factory, connectionFactory);
        return factory;
    }



    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(brokerUrl);

        if (username != null && !username.isEmpty()) {
            factory.setUserName(username);
            factory.setPassword(password);
        }

        factory.setTrustAllPackages(false);
        factory.setTrustedPackages(List.of(
                "com.example.micro.messaging",
                "com.zura.gymCRM.messaging",
                "java.util",
                "java.lang"
        ));

        // Configure redelivery policy
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(3);
        redeliveryPolicy.setInitialRedeliveryDelay(1000);
        redeliveryPolicy.setBackOffMultiplier(2);
        redeliveryPolicy.setUseExponentialBackOff(true);
        redeliveryPolicy.setDestination(workloadDlq());
        factory.setRedeliveryPolicy(redeliveryPolicy);

        return factory;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(connectionFactory());
        factory.setSessionCacheSize(10);
        factory.setCacheConsumers(true);
        factory.setCacheProducers(true);
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate template = new JmsTemplate(cachingConnectionFactory());
        template.setMessageConverter(jacksonJmsMessageConverter());
        template.setDeliveryPersistent(true);
        template.setSessionTransacted(true);
        template.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        return template;
    }

    // Production profile configuration
    @Configuration
    @Profile("prod")
    public static class ProdJmsConfig {

        @Value("${spring.activemq.prod.broker-url}")
        private String prodBrokerUrl;

        @Value("${spring.activemq.prod.user}")
        private String prodUsername;

        @Value("${spring.activemq.prod.password}")
        private String prodPassword;

        @Bean
        public ActiveMQConnectionFactory connectionFactory() {
            logger.info("Initializing Production ActiveMQ connection factory");

            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
            factory.setBrokerURL(prodBrokerUrl);
            factory.setUserName(prodUsername);
            factory.setPassword(prodPassword);
            factory.setTrustAllPackages(false);
            factory.setTrustedPackages(List.of(
                    "com.example.micro.messaging",
                    "java.util",
                    "java.lang"
            ));

            // Production-specific redelivery policy
            RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
            redeliveryPolicy.setMaximumRedeliveries(5);
            redeliveryPolicy.setInitialRedeliveryDelay(5000);
            redeliveryPolicy.setBackOffMultiplier(2);
            redeliveryPolicy.setUseExponentialBackOff(true);
            factory.setRedeliveryPolicy(redeliveryPolicy);

            return factory;
        }

        @Bean
        public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
                ConnectionFactory connectionFactory) {
            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            factory.setConcurrency("3-10"); // Higher concurrency for production
            factory.setSessionTransacted(true);

            // Configure error handling
            factory.setErrorHandler(t -> {
                logger.error("[PROD] Error in JMS message processing: {}", t.getMessage(), t);
                // In production, we might want to notify operations team
            });

            return factory;
        }
    }

    // Development profile configuration
    @Configuration
    @Profile("dev")
    public static class DevJmsConfig {

        private static final Logger logger = LoggerFactory.getLogger(DevJmsConfig.class);

        @Value("${spring.activemq.dev.broker-url:tcp://localhost:61616}")
        private String devBrokerUrl;

        @Bean
        public ActiveMQConnectionFactory connectionFactory() {
            logger.info("Initializing Development ActiveMQ connection factory");

            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
            factory.setBrokerURL(devBrokerUrl);
            factory.setTrustAllPackages(true); // Less strict for development

            // Dev-specific redelivery policy - more lenient
            RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
            redeliveryPolicy.setMaximumRedeliveries(10); // More retries in dev to debug
            redeliveryPolicy.setInitialRedeliveryDelay(1000);
            factory.setRedeliveryPolicy(redeliveryPolicy);

            return factory;
        }
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);

        // Add type mappings to handle messages from GymCRM
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("com.zura.gymCRM.messaging.WorkloadMessage",
                com.example.micro.messaging.WorkloadMessage.class);

        converter.setTypeIdMappings(typeIdMappings);
        converter.setTypeIdPropertyName("_type");

        return converter;
    }
}