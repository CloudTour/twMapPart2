package endpoint;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet implementation class SNSServlet
 */
@WebServlet("/")
public class SNSServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private	 DBManager db;
	private String subscribeUrl;
	 private String message;
	private String debug;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SNSServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		System.out.println("init DB connection");
		db = new DBManager();
		db.getDirver();
		db.connectAWS();	
		System.out.println("init done");
		debug="{ \"Type\" : \"SubscriptionConfirmation\", \"MessageId\" : \"77c04f1f-5baa-4bde-8a90-8ebab2737ac2\", \"Token\" : \"2336412f37fb687f5d51e6e241d638b114f4eb043adc26e9705ea7763f57a803e2bc1e242595e709ddedb5aaa3d4697e566b93ff4fe58be44a89cd154af16338a8b5a824452fd37331689ea3ffc9d2b027e1fe3927a25d0e20554e749051da3dff7d19a665ee0b466df4e6e7f732c374\", \"TopicArn\" : \"arn:aws:sns:us-west-2:721409569289:test\", \"Message\" : \"You have chosen to subscribe to the topic arn:aws:sns:us-west-2:721409569289:test.\nTo confirm the subscription, visit the SubscribeURL included in this message.\", \"SubscribeURL\" : \"https://sns.us-west-2.amazonaws.com/?Action=ConfirmSubscription&TopicArn=arn:aws:sns:us-west-2:721409569289:test&Token=2336412f37fb687f5d51e6e241d638b114f4eb043adc26e9705ea7763f57a803e2bc1e242595e709ddedb5aaa3d4697e566b93ff4fe58be44a89cd154af16338a8b5a824452fd37331689ea3ffc9d2b027e1fe3927a25d0e20554e749051da3dff7d19a665ee0b466df4e6e7f732c374\", \"Timestamp\" : \"2014-12-02T21:44:13.321Z\", \"SignatureVersion\" : \"1\", \"Signature\" : \"U8Xq7ur7sSAc0XQNlwO0vypdwdOaVioP7j2Xa2MJY/hsE3S/X34vPw1kqN2voWQ65KaLdD4qzzydfGwZmwxWkwtXKUVQGhYe/QB+nAXcGC57t7lsplvhn44G/A7+uufA9pngSIObRdvhv2Nh7cBRrLHsE1Gpkbd+QJCpJSYLb2CTmQZxxOAWhHi+Ldb62WA5twQZrmM/7JCp4zmnbu7wnTrUJX9+BObiAdTpOrre4FAaNYTKefoxjnCjkNYEYVx3gP45kel/Oseh5U+PMH8LKF5YGQ0WGElN7bcPK25rVdUuQb68H9Nn8l4O8kStmhAdnxEEdXiQiea7ss5QB8BglA==\", \"SigningCertURL\" : \"https://sns.us-west-2.amazonaws.com/SimpleNotificationService-d6d679a1d18e95c2f9ffcf11f4f9e198.pem\"}";
		debug="{ \"Type\":\"hahaha\" , \"MessageId\" : \"77c04f1f-5baa-4bde-8a90-8ebab2737ac2\"}";
		debug="";
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.getWriter().println("</p>this servlet is working!</p>");
		response.getWriter().println("</p>subscribeUrl: "+ subscribeUrl + "</p>");
		response.getWriter().println("</p>message: "+ message + "</p>");
		response.getWriter().println("</p>debug: "+ debug + "</p>");


//		System.out.println(readMessageFromJson(debug));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SecurityException{
		
		//Get the message type header.
		String messagetype = request.getHeader("x-amz-sns-message-type");
		//If message doesn't have the message type header, don't process it.
		if (messagetype == null)
			return;

		// Parse the JSON message in the message body
		// and hydrate a Message object with its contents 
		// so that we have easy access to the name/value pairs 
		// from the JSON message.
		Scanner scan = new Scanner(request.getInputStream());
		StringBuilder builder = new StringBuilder();
		while (scan.hasNextLine()) {
			builder.append(scan.nextLine());
		}
		scan.close();
		message = "<p>" + builder.toString() + "</p>";
		SNSMessage msg = readMessageFromJson(builder.toString());

		// The signature is based on SignatureVersion 1. 
		// If the sig version is something other than 1, 
		// throw an exception.
		if (msg.getSignatureVersion().equals("1")) {
			// Check the signature and throw an exception if the signature verification fails.
			if (isMessageSignatureValid(msg))
				System.out.println(">>Signature verification succeeded");
			else {
				System.out.println(">>Signature verification failed");
				throw new SecurityException("Signature verification failed.");
			}
		}
		else {
			System.out.println(">>Unexpected signature version. Unable to verify signature.");
			throw new SecurityException("Unexpected signature version. Unable to verify signature.");
		}
		
		// Process the message based on type.
		if (messagetype.equals("Notification")) {
			System.out.println(msg.getMessage());
			//TODO: Parse this message and store into DB.
			String[] contents = msg.getMessage().split(" ");
			Long sId = Long.parseLong(contents[0]);
			String type = contents[1];
			String score = contents[2];
			
			db.insertAttitude(sId, type, score);			
			System.out.println("evaluation " + sId + " saved");
			
		} 
		else if (messagetype.equals("SubscriptionConfirmation"))
		{
			//TODO: You should make sure that this subscription is from the topic you expect. Compare topicARN to your list of topics 
			//that you want to enable to add this endpoint as a subscription.

			//Confirm the subscription by going to the subscribeURL location 
			//and capture the return value (XML message body as a string)
			Scanner sc = new Scanner(new URL(msg.getSubscribeURL()).openStream());
			StringBuilder sb = new StringBuilder();
			while (sc.hasNextLine()) {
				sb.append(sc.nextLine());
			}
			sc.close();
			subscribeUrl = msg.getSubscribeURL();
			System.out.println(">>Subscription confirmation (" + msg.getSubscribeURL() +") Return value: " + sb.toString());
			//TODO: Process the return value to ensure the endpoint is subscribed.
			SNSHelper.INSTANCE.confirmTopicSubmission(msg);
		}
		else if (messagetype.equals("UnsubscribeConfirmation")) {
			//TODO: Handle UnsubscribeConfirmation message. 
			//For example, take action if unsubscribing should not have occurred.
			//You can read the SubscribeURL from this message and 
			//re-subscribe the endpoint.
			System.out.println(">>Unsubscribe confirmation: " + msg.getMessage());
		}
		else {
			//TODO: Handle unknown message type.
			System.out.println(">>Unknown message type.");
		}
		System.out.println(">>Done processing message: " + msg.getMessageId());
	}


	private boolean isMessageSignatureValid(SNSMessage msg) {

		try {
			URL url = new URL(msg.getSigningCertUrl());
			InputStream inStream = url.openStream();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
			inStream.close();

			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(cert.getPublicKey());
			sig.update(getMessageBytesToSign(msg));
			return sig.verify(Base64.decodeBase64(msg.getSignature().getBytes()));
		}
		catch (Exception e) {
			throw new SecurityException("Verify method failed.", e);

		}
	}

	private byte[] getMessageBytesToSign(SNSMessage msg) {

		byte [] bytesToSign = null;
		if (msg.getType().equals("Notification"))
			bytesToSign = buildNotificationStringToSign(msg).getBytes();
		else if (msg.getType().equals("SubscriptionConfirmation") || msg.getType().equals("UnsubscribeConfirmation"))
			bytesToSign = buildSubscriptionStringToSign(msg).getBytes();
		return bytesToSign;
	}

	//Build the string to sign for Notification messages.
	private static String buildNotificationStringToSign( SNSMessage msg) {
		String stringToSign = null;

		//Build the string to sign from the values in the message.
		//Name and values separated by newline characters
		//The name value pairs are sorted by name 
		//in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		if (msg.getSubject() != null) {
			stringToSign += "Subject\n";
			stringToSign += msg.getSubject() + "\n";
		}
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

	//Build the string to sign for SubscriptionConfirmation 
	//and UnsubscribeConfirmation messages.
	private static String buildSubscriptionStringToSign(SNSMessage msg) {
		String stringToSign = null;
		//Build the string to sign from the values in the message.
		//Name and values separated by newline characters
		//The name value pairs are sorted by name 
		//in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		stringToSign += "SubscribeURL\n";
		stringToSign += msg.getSubscribeURL() + "\n";
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "Token\n";
		stringToSign += msg.getToken() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}


	private SNSMessage readMessageFromJson(String string) {
		ObjectMapper mapper = new ObjectMapper(); 
		SNSMessage message = null;
		try {
			message = mapper.readValue(string, SNSMessage.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return message;
	}
	

}