package com.java.instructor.jmspoller.jms;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.LoggingHandler.Level;
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener;
import org.springframework.integration.jms.JmsMessageDrivenEndpoint;
import org.springframework.integration.jms.JmsSendingMessageHandler;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

@Configuration
public class ProducerConsumerChannelConfig {
	 
	 @Value("${JmsQueueName}")
	  private String JmsQueueName;
	  
	// producer
	  @Bean
	  public DirectChannel testJMSMsgProducingChannel() {
	    return new DirectChannel();
	  }

	  @Bean
	  @ServiceActivator(inputChannel = "testJMSMsgProducingChannel")
	  public MessageHandler jmsMessageHandler(JmsTemplate jmsTemplate) {
	    JmsSendingMessageHandler handler = new JmsSendingMessageHandler(jmsTemplate);
	    handler.setDestinationName(JmsQueueName);
	    return handler;
	  }
	  
	  // Consumer
	  
	  @Bean
	  public DirectChannel testJMSMsgConsumingChannel() {
	    return new DirectChannel();
	  }
	  
	  @Bean
	  public DirectChannel testLogHandlerChannel() {
	    return new DirectChannel();
	  }

	  @Bean
	  public JmsMessageDrivenEndpoint jmsMessageDrivenEndpoint(
	      ConnectionFactory connectionFactory) {
	    JmsMessageDrivenEndpoint endpoint = new JmsMessageDrivenEndpoint(simpleMessageListenerContainer(connectionFactory), channelPublishingJmsMessageListener());
	    endpoint.setOutputChannel(testJMSMsgConsumingChannel());
	    return endpoint;
	  }

	  @Bean
	  public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory) {
	    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
	    container.setConnectionFactory(connectionFactory);
	    container.setDestinationName(JmsQueueName);
	    return container;
	  }

	  @Bean
	  public ChannelPublishingJmsMessageListener channelPublishingJmsMessageListener() {
	    return new ChannelPublishingJmsMessageListener();
	  }

/*	  @Bean
	  @ServiceActivator(inputChannel = "testJMSMsgConsumingChannel")
	  public JmsQueueMessageHandler countDownLatchHandler() {
	    return new JmsQueueMessageHandler();
	  }*/
	  
		@Transformer(inputChannel = "testJMSMsgConsumingChannel", outputChannel = "testLogHandlerChannel")
		public Message<String> writeFileChannelTransformer(final Message inputMessage) {

			System.out.println("============================================================================");
			System.out.println(inputMessage.getPayload());
			System.out.println("============================================================================");
			return inputMessage;
		}
		@Bean
		@ServiceActivator(inputChannel = "testLogHandlerChannel")
		public LoggingHandler logging1() {
			LoggingHandler adapter = new LoggingHandler(Level.INFO);
			adapter.setLoggerName("testLogHandlerChannel");
			adapter.setLogExpressionString("'log msg------------------>>: ' + payload");
			return adapter;
		}
}
