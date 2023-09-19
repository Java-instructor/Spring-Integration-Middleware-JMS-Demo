package com.java.instructor.jmspoller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.java.instructor.jmspoller.jms.JmsQueueMessageHandler;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class JMSPollerDemoTest {

  private static final Logger LOGGER =   LoggerFactory.getLogger(JMSPollerDemoTest.class);

  @Value("${JmsQueueName}")
  private String JmsQueueName;
  
  @Autowired
  private ApplicationContext applicationContext;
  
  @Autowired
  private JmsQueueMessageHandler jmsQueueMessageHandler;

  @Test
  public void sendMessageToProducingChannelTest() throws Exception {
    MessageChannel testJMSMsgProducingChannel = applicationContext.getBean("testJMSMsgProducingChannel", MessageChannel.class);
	Map<String, String> headers = Collections.singletonMap(JmsHeaders.DESTINATION, JmsQueueName);
    SubscribableChannel  testLogHandlerChannel = applicationContext.getBean("testLogHandlerChannel", SubscribableChannel.class);
    testLogHandlerChannel.subscribe(new JmsQueueMessageHandler());
    LOGGER.info("sending 10 messages to Producer Chennal");
    for (int i = 0; i < 10; i++) {
      GenericMessage<String> message = new GenericMessage("Hello Spring Integration Message to Chennal  " + i + "!", headers);
      testJMSMsgProducingChannel.send(message);
    }

    jmsQueueMessageHandler.getLatch().await(15000, TimeUnit.MILLISECONDS);
    assertThat(jmsQueueMessageHandler.getLatch().getCount()).isEqualTo(0);
  }
  
  /**
 * @throws Exception
 */
@Test
  public void sendMessageToActiveMQQueueTest() throws Exception {
    Map<String, String> headers = Collections.singletonMap(JmsHeaders.DESTINATION, JmsQueueName);
    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
    Connection connection = connectionFactory.createConnection();
    connection.start();
    SubscribableChannel  testLogHandlerChannel = applicationContext.getBean("testLogHandlerChannel", SubscribableChannel.class);
    testLogHandlerChannel.subscribe(jmsQueueMessageHandler);
    LOGGER.info("sending 10 messages to JMS QUEUE");
    for (int i = 0; i < 10; i++) {
    	String messageStr="Hello Spring Integration Message to JMS Queue " + i + "!";
    	sendMessageToQueue(connection, messageStr);    
    }

    connection.close();
    jmsQueueMessageHandler.getLatch().await(10000, TimeUnit.MILLISECONDS);
    assertThat(jmsQueueMessageHandler.getLatch().getCount()).isEqualTo(0);
  }  
  
  /**
 * @param connection
 * @param msg
 * @throws JMSException
 */
public  void sendMessageToQueue(Connection connection,String msg) throws JMSException { 
      Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);  
      Destination destination = session.createQueue(JmsQueueName); 
      MessageProducer producer = session.createProducer(destination);
      TextMessage message = session.createTextMessage(msg);
      producer.send(message);
    
  }
}
