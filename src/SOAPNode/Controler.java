package SOAPNode;

import java.io.PrintStream;
import java.net.Socket;

import java.util.ArrayList;
import java.util.List;

import Interfaces.ReceiverInterface;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
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
	int thisAppPort;
	int nextNodePort;

	String address;

	Receiver receiver;

	String allMessages;

	public Controler() {

		// Set variables.
		thisAppPort = 8190;

		nextNodePort = thisAppPort;

		allMessages = "";

		listLogs = new ArrayList<String>();
	}

	public void destructor() {
		receiver.endThread();
		try {
			log("INFO: Waiting for join receier thread.");
			receiver.join();
			log("INFO: Receier thread has been joined.");
		} catch (Exception e) {
			log("ERROR: Receiver thread throw exception while joining.");
			return;
		}
	}

	public void initialize() {

		// ---------------- Run receiver ----------------------------

		receiver = new Receiver(thisAppPort, this);
		receiver.start();

		// ---------------- Settings controls -----------------------
		labelInputPort.setText(String.valueOf(thisAppPort));
		labelNextNodePort.setText(String.valueOf(nextNodePort));

		textFieldInputPort.setText(String.valueOf(thisAppPort));
		textFieldNextNodePort.setText(String.valueOf(nextNodePort));

		// ---------------- Send message controls -------------------
		textAreaWriteMessage.setText("example message");
		choiceBoxUnicastBroadcast.getItems().add("Broadcast");
		choiceBoxUnicastBroadcast.getItems().add("Unicast");
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

		thisAppPort = port;
		labelInputPort.setText(String.valueOf(thisAppPort));
		textFieldInputPort.setText(String.valueOf(thisAppPort));

		//
		// Reload endpoint.
		//
		receiver.endThread();
		try {
			log("INFO: Waiting for join receier thread.");
			receiver.join();
			log("INFO: Receier thread has been joined.");
		} catch (Exception e) {
			log("ERROR: Receiver thread throw exception while joining.");
			log("ERROR: New receiver thread has been not created.");
			return;
		}

		receiver = new Receiver(thisAppPort, this);
		receiver.start();

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
		String message = textAreaWriteMessage.getText();
		String msgType = choiceBoxUnicastBroadcast.getSelectionModel().getSelectedItem();

		int destinationPort = -1;
		try {
			destinationPort = Integer.parseInt(textFieldDestinationPort.getText());
		} catch (Exception e) {
			MyMessage.show("Wrong destination port.");
			return;
		}

		log("INFO: Sending message from port " + thisAppPort + " to " + nextNodePort + " type: " + msgType);
		sendMessage(message, thisAppPort, destinationPort, msgType);

		return;
	}

	@Override
	public void receiveMessage(String msg, int sourcePort, int destinationPort, String msgType) {

		log("INFO: Received message from port " + destinationPort + " to " + destinationPort + " type: " +  msgType);

		if(sourcePort == -1 ||  destinationPort == -1) {
			log("ERROR: Message is incorrect.");
			return;
		}


		if (sourcePort == thisAppPort) {
			log("WARNING: Message back to sender.");
			return;
		} else if (msgType == "Broadcast") {
			log("INFO: Received message from port: " + sourcePort + ".");
			addNewMessage(msg);

			sendMessage(msg, sourcePort, destinationPort, msgType);
			addNewMessage(msg);
		} else if (destinationPort == thisAppPort) {
			log("INFO: Received message from port: " + sourcePort + ".");
			addNewMessage(msg);
		} else {
			log("INFO: Message from port: " + sourcePort + " forwarded to the next node.");
			sendMessage(msg, sourcePort, destinationPort, msgType);
			addNewMessage(msg);
		}

		return;
	}

	public void sendMessage(String msg, int sourcePort, int destinationPort, String msgType) {

		try {
			SOAPMessage soapMessage = createMessage(msg, sourcePort, destinationPort, msgType);

			if(soapMessage == null) {
				return;
			}

			Socket socket = new Socket("127.0.0.1", nextNodePort);
			PrintStream out = new PrintStream(socket.getOutputStream(), true);
			soapMessage.writeTo(out);
			out.close();
			socket.close();

		} catch (Exception e) {
			log("ERROR: Cannot send message.");
		}

		return;
	}

	public SOAPMessage createMessage(String text, int sourcePort, int destinationPort, String msgType) {

		SOAPMessage soapMessage = null;

		try {
			MessageFactory messageFactory;
			messageFactory = MessageFactory.newInstance();
			soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
			SOAPBody soapBody = soapEnvelope.getBody();

			Name bodyName = SOAPFactory.newInstance().createName("msg");
			SOAPBodyElement soapBodyElement = soapBody.addBodyElement(bodyName);
			soapBodyElement.addTextNode(text);

			SOAPHeader header = soapEnvelope.getHeader();

			Name receiverHeader = SOAPFactory.newInstance().createName("destinationPort", "pre", "uri");
			SOAPElement receiveElement = header.addChildElement(receiverHeader);
			receiveElement.addTextNode(String.valueOf(destinationPort));

			Name senderHeader = SOAPFactory.newInstance().createName("sourcePort", "pre", "uri");
			SOAPElement sendElement = header.addChildElement(senderHeader);
			sendElement.addTextNode(String.valueOf(sourcePort));

			Name typeHeader = SOAPFactory.newInstance().createName("msgType", "pre", "uri");
			SOAPElement typeElement = header.addChildElement(typeHeader);
			typeElement.addTextNode(msgType);

			soapMessage.saveChanges();
		} catch (Exception e) {
			log("ERROR: Cannot create SOAP message.");
			return null;
		}
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

	public void asyncLog(String str) {
		//System.out.println(str);
		log(str);
	}
}