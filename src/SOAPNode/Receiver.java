package SOAPNode;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.xml.soap.SOAPMessage;

import Interfaces.ReceiverInterface;

import javax.xml.soap.*;

public class Receiver extends Thread {
	private int servicePort;
	public Socket socketAccept;
	public static ServerSocket serverSocket;
	private BufferedReader buffer;

	ReceiverInterface receiverInterface;

	public Receiver(int port, ReceiverInterface receiverInterface) {
		this.servicePort = port;
		this.receiverInterface = receiverInterface;
	}

	@Override
	public void run() {
		try {

				try {
					serverSocket = new ServerSocket(servicePort);
					//break;
				} catch (Exception e) {
					receiverInterface.asyncLog("Cannot set receiver on port: " + servicePort);
					return;
				}

			while (true) {
				socketAccept = serverSocket.accept();

				buffer = new BufferedReader(new InputStreamReader(socketAccept.getInputStream()));

				if (socketAccept.isClosed() == false) {
					String msg;
					String receivedXML;

					receivedXML = buffer.readLine();

					while ((msg = buffer.readLine()) != null) {
						receivedXML += msg;
					}

					if (receivedXML != null) {

						parseXMLandSendToContrler(receivedXML);
					}
				}
				buffer.close();
			}
		} catch (SocketException e) {
			receiverInterface.asyncLog("INFO: End receiver thread.");
		} catch (Exception e) {
			receiverInterface.asyncLog("ERROR: Receiver Error!!!");
			endThread();
		}
	}

	public Boolean parseXMLandSendToContrler(String message) {

		String text;
		int sourcePort;
		int destinationPort;
		String type;

		try {
			MessageFactory messageFactory = MessageFactory.newInstance();

			InputStream is = new ByteArrayInputStream(message.getBytes());
			SOAPMessage soapMessage = messageFactory.createMessage(null, is);

			SOAPHeader header = soapMessage.getSOAPPart().getEnvelope().getHeader();
			SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
			Node receiveInfo = (Node) header.getElementsByTagNameNS("uri", "destinationPort").item(0);
			destinationPort = Integer.parseInt(receiveInfo.getFirstChild().getTextContent());

			Node sendInfo = (Node) header.getElementsByTagNameNS("uri", "sourcePort").item(0);
			sourcePort = Integer.parseInt(sendInfo.getFirstChild().getTextContent());

			Node typeInfo = (Node) header.getElementsByTagNameNS("uri", "msgType").item(0);
			type = typeInfo.getFirstChild().getTextContent();

			Node msg = (Node) body.getElementsByTagName("msg").item(0);
			text = msg.getFirstChild().getTextContent();

			receiverInterface.receiveMessage(text, sourcePort, destinationPort, type);

		} catch (Exception e) {
			receiverInterface.asyncLog("ERROR: Cannot parse received XML message");
			return false;
		}

		return true;
	}

	public void endThread() {
		if (serverSocket != null) {
			try {
				receiverInterface.asyncLog("INFO: Going to close receiver socket.");
				serverSocket.close();
				receiverInterface.asyncLog("INFO: Sent close socket request.");
			} catch (Exception e) {
				receiverInterface.asyncLog("ERROR: Cannot close socket.");
			}
		}
	}
}
