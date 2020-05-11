package SOAPNode;

import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Interfaces.MessageInterface;
import Interfaces.ReceiverInterface;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class Controler implements ReceiverInterface {

	// ---------------- Settings controls -----------------------
	public Label labelInputPort;
	public Label labelNextNodePort;

	public TextField textFieldInputPort;
	public TextField textFieldNextNodePort;

	public Button buttonReloadInputPort;
	public Button buttonReloadNextNodePort;

	// ---------------- Send message controls -------------------
	public TextArea textAreaWriteMessage;

	public ChoiceBox<String> choiceBoxUnicastBroadcast;

	public TextField textFieldDestinationPort;

	public Button buttonSendMessage;

	// ---------------- Receive message controls -----------------
	public TextArea textAreaReceivedMessges;

	// ---------------- Log controls -----------------------------
	public ListView<String> listViewLogs;

	// ---------------- Variable ---------------------------------

	List<String> listLogs;
	int inputPort;
	int nextNodePort;

	Endpoint endpoint;
	Message message;
	String address;

	String allMessages;

	public Controler() {
		// Set variables.
		inputPort = 8093;
		nextNodePort = 8094;

		allMessages = "";

		endpoint = null;

		listLogs = new ArrayList<String>();
	}

	public void destructor() {
		if (endpoint != null) {
			endpoint.stop();
		}
	}

	public void initialize() {
		// ----------------Create endpoint --------------------------

		message = new Message(this);
		while (true) {
			try {
				address = "http://localhost:" + inputPort + "/Message";
				endpoint = Endpoint.publish(address, message);
			} catch (Exception e) {
				log("Port " + inputPort + " is busy.");
				inputPort--;
				continue;
			}
			break;
		}

		// nextNodePort = inputPort + 1;
		nextNodePort = inputPort;
		log("Created endpoint at: " + address);

		// ---------------- Settings controls -----------------------
		labelInputPort.setText(String.valueOf(inputPort));
		labelNextNodePort.setText(String.valueOf(nextNodePort));

		textFieldInputPort.setText(String.valueOf(inputPort));
		textFieldNextNodePort.setText(String.valueOf(nextNodePort));

		// ---------------- Send message controls -------------------
		textAreaWriteMessage.setText("example message");
		choiceBoxUnicastBroadcast.getItems().add("Broadcast                                                         ");
		choiceBoxUnicastBroadcast.getItems().add("Unicast                                                           ");
		choiceBoxUnicastBroadcast.getSelectionModel().selectFirst();

		// choiceBoxUnicastBroadcast.onActionProperty();

		textFieldDestinationPort.setText(String.valueOf(nextNodePort));

		// ---------------- Receive message controls -----------------
		textAreaReceivedMessges.setText("");
	}

	public void buttonReloadInputPortClicked() {
		int port;

		try {
			port = Integer.parseInt(textFieldInputPort.getText());
		} catch (Exception e) {
			MyMessage.show("Wrong port number.");
			return;
		}

		inputPort = port;
		labelInputPort.setText(String.valueOf(inputPort));
		textFieldInputPort.setText(String.valueOf(inputPort));

		// TO DO
		// Reload endpoint.

		return;
	}

	public void buttonReloadNextNodePortClicked() {
		int port;

		try {
			port = Integer.parseInt(textFieldNextNodePort.getText());
		} catch (Exception e) {
			MyMessage.show("Wrong port number.");
			return;
		}

		nextNodePort = port;
		labelNextNodePort.setText(String.valueOf(nextNodePort));
		textFieldNextNodePort.setText(String.valueOf(nextNodePort));
		return;
	}

	public void buttonSendMessageClicked() {
		log("Sending message...");

		try {
			SOAPMessage msg = createMessage("wiadomosc", String.valueOf(inputPort), String.valueOf(nextNodePort),
					"uicast");
			System.out.print(msg);


			Socket socket = new Socket("127.0.0.1", nextNodePort);
	        PrintStream out = new PrintStream(socket.getOutputStream(), true);
	        msg.writeTo(out);
	        out.close();

		} catch (Exception e) {
			log("Cannot create SOAP message.");
		}

//		try {
//			String urlString = "http://localhost:" + nextNodePort + "/Message?wsdl";
//
//			final Service messageService = Service.create(new URL(urlString),
//					new QName("http://SOAPNode/wsdl", "MessageService"));
//
//			if (messageService == null) {
//				log("Cannot read service from wsdl.");
//				return;
//			}
//
//			final MessageInterface message = messageService.getPort(new QName("http://SOAPNode/wsdl", "MessagePort"),
//					MessageInterface.class);
//
//			String messageToSend = textAreaWriteMessage.getText();
//			message.sentReceiveMessage(messageToSend);
//
//		} catch (Exception e) {
//			log("Cannot send message.");
//			MyMessage.show("Cannot send message.");
//		}
	}

	@Override
	public void receiveMessage(String msg) {
		addNewMessage(msg);
		return;
	}

	public static SOAPMessage createMessage(String text, String receiver, String sender, String msgType)
			throws SOAPException {

		MessageFactory messageFactory;
		messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();
		SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
		SOAPBody soapBody = soapEnvelope.getBody();

		Name bodyName = SOAPFactory.newInstance().createName("msg");
		SOAPBodyElement soapBodyElement = soapBody.addBodyElement(bodyName);
		soapBodyElement.addTextNode(text);

		SOAPHeader header = soapEnvelope.getHeader();

		Name receiverHeader = SOAPFactory.newInstance().createName("receiverId", "pre", "uri");
		SOAPElement receiveElement = header.addChildElement(receiverHeader);
		receiveElement.addTextNode(receiver);

		Name senderHeader = SOAPFactory.newInstance().createName("senderId", "pre", "uri");
		SOAPElement sendElement = header.addChildElement(senderHeader);
		sendElement.addTextNode(sender);

		Name typeHeader = SOAPFactory.newInstance().createName("type", "pre", "uri");
		SOAPElement typeElement = header.addChildElement(typeHeader);
		typeElement.addTextNode(msgType);

		soapMessage.saveChanges();
		return soapMessage;
	}

	private void addNewMessage(String str) {
		allMessages = str + "\n ----------- " + java.time.LocalTime.now() + "-------------------- \n" + allMessages;
		textAreaReceivedMessges.setText(allMessages);
	}

	private void log(String str) {
		listViewLogs.getItems().add(str);
		System.out.println(str);
	}
}