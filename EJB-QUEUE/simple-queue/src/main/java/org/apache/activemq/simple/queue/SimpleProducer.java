package org.apache.activemq.simple.queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleProducer {
    private static final Log LOG = LogFactory.getLog(SimpleProducer.class);

    private static final Boolean NON_TRANSACTED = false;
    private static final long MESSAGE_TIME_TO_LIVE_MILLISECONDS = 0;
    private static final int MESSAGE_DELAY_MILLISECONDS = 1000;

    private static final int NUM_MESSAGES_TO_BE_SENT = 100;
    private static final String CONNECTION_FACTORY_NAME = "myJmsFactory";
    private static final String DESTINATION_NAME = "queue/simple";

    public static void main(String args[]) {
        Connection connection = null;

        try {
            // JNDI lookup of JMS Connection Factory and JMS Destination
            Context context = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) context.lookup(CONNECTION_FACTORY_NAME);
            Destination destination = (Destination) context.lookup(DESTINATION_NAME);

            connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(destination);

            producer.setTimeToLive(MESSAGE_TIME_TO_LIVE_MILLISECONDS);

            List<String> messages = new ArrayList<>(Arrays.asList("mes", "hello", "good luck"));

            messages.forEach(m -> {
                TextMessage message = null;
                try {
                    message = session.createTextMessage(m);
                    LOG.info("Sending to destination: " + destination.toString() + " this text: " + message.getText());
                    producer.send(message);
                    Thread.sleep(MESSAGE_DELAY_MILLISECONDS);
                } catch (JMSException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
//            for (int i = 1; i <= NUM_MESSAGES_TO_BE_SENT; i++) {
//                TextMessage message = session.createTextMessage(i + ". message sent");
//                LOG.info("Sending to destination: " + destination.toString() + " this text: '" + message.getText());
//                producer.send(message);
//                Thread.sleep(MESSAGE_DELAY_MILLISECONDS);
//            }

            // Cleanup
            producer.close();
            session.close();
        } catch (Throwable t) {
            LOG.error(t);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    LOG.error(e);
                }
            }
        }
    }
}
