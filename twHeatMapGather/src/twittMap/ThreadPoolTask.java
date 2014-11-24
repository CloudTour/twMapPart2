package twittMap;

import java.io.Serializable;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import com.alchemyapi.api.*;

public class ThreadPoolTask implements Runnable, Serializable {
	private static final long serialVersionUID = 0;
	// ������������Ҫ������
	private Object threadPoolTaskData;

	ThreadPoolTask(Object tasks) {
		this.threadPoolTaskData = tasks;
	}

	public void run() {
		// ����һ����������Ĵ���ʽ̫���ˣ�������һ����ӡ���
		System.out.println("start .." + threadPoolTaskData);
		try {
			// Create an AlchemyAPI object.
			AlchemyAPI alchemyObj = AlchemyAPI
					.GetInstanceFromFile("api_key.txt");
			Document doc = alchemyObj
					.TextGetTextSentiment(SimpleQueueService.PickUpAmsg());
			System.out.println(getStringFromDocument(doc));
		} catch (Exception e) {
			e.printStackTrace();
		}
		threadPoolTaskData = null;
	}

	public Object getTask() {
		return this.threadPoolTaskData;
	}

	// utility method
	private static String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);

			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}