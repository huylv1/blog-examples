package com.tomasmalmsten.exmples.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * This test will atempt to open a connection to a running RabbitMQ server. If
 * the system does not have a running RabbitMQ server the test will faile with
 * a ConnectionExcepion in the setupConnectionAndChannelAndExchange method.
 */
public class HelloAMQPTest {

  private static final String EXCHANGE_NAME = "hello";
  private static final String ROUTING_KEY = "routingkey";
  private static final String MESSAGE = "Hello World";
  private static final boolean AUTO_ACK = false;
  private static final String TYPE = "direct";
  private static final boolean DURABILITY = true;
  private Channel channel;
  private Connection connection;
  private String queueName;
  private String message;

  @Before
  public void setUpConnectionAndChannelAndExchange() throws IOException {
    ConnectionFactory factory = new ConnectionFactory();
    connection = factory.newConnection();
    channel = connection.createChannel();
    channel.exchangeDeclare(EXCHANGE_NAME, TYPE, DURABILITY);
    queueName = channel.queueDeclare().getQueue();
    channel.queueBind(queueName, EXCHANGE_NAME, ROUTING_KEY);
  }

  @Test
  public void receiveSentMessage() throws InterruptedException, IOException {
    givenMessageIsSentThroughConfiguredChannel();
    whenTheMessageIsRetrievedFromTheChannel();
    thenTheReceivedMessageShouldMatchTheSentMessage();
  }

  private void givenMessageIsSentThroughConfiguredChannel() throws IOException {
    channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, MESSAGE.getBytes());
  }

  private void whenTheMessageIsRetrievedFromTheChannel() throws IOException, InterruptedException {
    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(queueName, AUTO_ACK, consumer);
    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
    message = new String(delivery.getBody());
    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
  }

  private void thenTheReceivedMessageShouldMatchTheSentMessage() {
    assertEquals(MESSAGE, message);
  }

  @After
  public void closeChannelAndConnection() throws IOException {
    channel.close();
    connection.close();
  }
}
