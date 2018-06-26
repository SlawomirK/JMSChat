package zad1;

/* @author Kobyliński Sławomir S12410 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JMSChatStart {
	/** Należy zmienić np na 2 przed uruchomieniem drugi raz dla pewności, że to inna. */
	private static String iDCzatu = "4";
										/** Instancja programu. */
	private static TopicConnectionFactory tconFactory;
	private static TopicConnection tcon;
	private static TopicSession tsession;
	private static TopicPublisher tpublisher;
	private static Topic topic;
	private static BufferedReader bf;

	public static void main(String[] args) {
		Hashtable<String, String> properties = setProperties();
		System.out.println("Chat" + iDCzatu + "\n-------------------");
		try {
			TopicSubscriber tsubscriber = initialize(properties);
			runJMSChat(tsubscriber);
		} catch (NamingException | JMSException | IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	private static void close() {
		if (tcon != null) {
			try {
				tcon.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
			if (bf != null) {
				try {
					bf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void runJMSChat(TopicSubscriber tsubscriber) throws JMSException, IOException, InterruptedException {
		String msg = "";
		tcon.start();

		do {
			if (!msg.isEmpty()) {
				sendMessage(tpublisher, tsession, iDCzatu, msg);
			}
			Thread.sleep(100);
			getMessage(tsession, tsubscriber, iDCzatu);
		} while (!"bye".equals(msg = bf.readLine()));
		System.out.println("Program zakończył komunikację");
		tcon.close();
		System.exit(0);
	}

	private static TopicSubscriber initialize(Hashtable<String, String> properties)
			throws NamingException, JMSException {
		Context ctx = new InitialContext(properties);		
		tconFactory = (TopicConnectionFactory) ctx.lookup("ConnectionFactory");
		tcon = tconFactory.createTopicConnection();		
		tsession = tcon.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);		
		topic = (Topic) ctx.lookup("topic1");
		tpublisher = tsession.createPublisher(topic);
		TopicSubscriber tsubscriber = tsession.createSubscriber(topic);
		bf = new BufferedReader(new InputStreamReader(System.in));
		return tsubscriber;
	}

	private static Hashtable<String, String> setProperties() {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
		properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");
		return properties;
	}

	private static void getMessage(TopicSession tsession2, TopicSubscriber tsubscriber, String iDCzatu2)
			throws JMSException {
		tsubscriber.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message arg0) {
				TextMessage msg = (TextMessage) arg0;
				System.out.println( "Chat"+iDCzatu2+" odbiera "+msg);
			}
		});
	}

	private static void sendMessage(TopicPublisher tPub, TopicSession ses, String id, String msg2)
			throws IOException, JMSException {	
		tPub.publish(ses.createTextMessage("Chat"+id+" pisze: "+msg2));
		
	}
}
