package com.java.instructor.jmspoller.jms;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

@Component
public class JmsQueueMessageHandler implements MessageHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JmsQueueMessageHandler.class);

  private CountDownLatch latch = new CountDownLatch(5);

  public CountDownLatch getLatch() {
    return latch;
  }

  public void handleMessage(Message<?> message) {
    LOGGER.info("received message='{}'", message);
    latch.countDown();
  }
}
