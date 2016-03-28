package demo.messaging.producer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.util.FileSystemUtils;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

@SpringBootApplication
@EnableJms
public class MessageWriterApplication {

	private static final int PORT = 32786;

	@Bean
	static ConnectionFactory connectionFactory() {
		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://192.168.99.100:" + PORT);
		cf.setUserName("admin");
		cf.setPassword("admin");
		return cf;
	}

	// Strictly speaking this bean is not necessary as boot creates a default
	@Bean
	JmsListenerContainerFactory<?> myJmsContainerFactory() {
		SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory());
		return factory;
	}

	public static void main(String[] args) {
		String hostname = "";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException ex) {
			System.out.println("unknown host exception : " + ex.getMessage());
		}

		// Clean out any ActiveMQ data from a previous run
		FileSystemUtils.deleteRecursively(new File("activemq-data"));

		// Launch the application
		ConfigurableApplicationContext context = SpringApplication.run(MessageWriterApplication.class, args);

		// Send a message
		final String finalHostname = hostname;
		MessageCreator messageCreator = new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(new Date(System.currentTimeMillis()) + " - ping from " + finalHostname);
			}
		};
//        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
		System.out.println("MessageCreator created");

		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
		System.out.println("JmsTemplate created");
		while (true) {
			try {
				System.out.println("sending...");
				jmsTemplate.send("mailbox-destination", messageCreator);
				System.out.println("sent");
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}


			try {
				Thread.sleep(3000);
			} catch (InterruptedException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
}
