package com.epam.trainingservice.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableJms
@Configuration
@EnableTransactionManagement
public class JmsConfig {
    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;
    @Value("${spring.activemq.user}")
    private String user;
    @Value("${spring.activemq.password}")
    private String password;
    public static final Logger logger = LoggerFactory.getLogger(JmsConfig.class);

    @Bean
    public SingleConnectionFactory connectionFactory(){
        SingleConnectionFactory factory = new SingleConnectionFactory(
                new ActiveMQConnectionFactory(
                    user,password,brokerUrl
                )
        );
        factory.setClientId("training");
        factory.setReconnectOnException(true);
        return factory;
    }
    @Bean
    public MessageConverter jacksonJmsMessageConverter(){
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setTransactionManager(jmsTransactionManager());
        factory.setErrorHandler(h -> {
            logger.info("Handling error in listener. error: ", h);
        });
        return factory;
    }
    @Bean
    public PlatformTransactionManager jmsTransactionManager(){
        return new JmsTransactionManager(connectionFactory());
    }
    @Bean
    public TransactionManager transactionManager(){
        return new JpaTransactionManager();
    }
    @Bean
    public JmsTemplate jmsTemplate(){
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
        jmsTemplate.setDeliveryPersistent(true);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

}
