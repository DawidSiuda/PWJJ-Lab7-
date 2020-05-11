package SOAPNode;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import javax.xml.soap.*;

public class Receiver extends Thread {
	private int servicePort;
	public Socket socketAccept;
	public static ServerSocket serverSocket;
	private BufferedReader buffer;
	public static Controller controller;

	public Receiver(int port) {
		this.servicePort = port;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(servicePort);
			while (true) {
				socketAccept = serverSocket.accept();
				buffer = new BufferedReader(new InputStreamReader(socketAccept.getInputStream()));
				if (!socketAccept.isClosed()) {
					String msg, response;
					response = buffer.readLine();

					while ((msg = buffer.readLine()) != null) {
						response += msg;
					}
//                 if (response != null) {
//                     convertMessage(response);
//                 }
				}
				buffer.close();
			}
		} catch (Exception e) {
			// e.printStackTrace()
			System.out.print("Receiver Error!!!");
		}
	}

    public void convertMessage(String message) throws SOAPException, IOException {

    	MessageFactory messageFactory = MessageFactory.newInstance();

        InputStream is = new ByteArrayInputStream(message.getBytes());
        SOAPMessage soapMessage = messageFactory.createMessage(null, is);

        SOAPHeader header = soapMessage.getSOAPPart().getEnvelope().getHeader();
        SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
        Node receiveInfo = (Node) header.getElementsByTagNameNS("uri", "receiverId").item(0);
        String receiver = receiveInfo.getFirstChild().getTextContent();

        Node sendInfo = (Node) header.getElementsByTagNameNS("uri", "senderId").item(0);
        String sender = sendInfo.getFirstChild().getTextContent();

        Node typeInfo = (Node) header.getElementsByTagNameNS("uri", "type").item(0);
        String type = typeInfo.getFirstChild().getTextContent();

        Node msg = (Node) body.getElementsByTagName("msg").item(0);
        String messageFromXML = msg.getFirstChild().getTextContent();

        // ---------------------------------------------------------------

        if(receiver.equals(port))
        {
            controller.receive.setText(messageFromXML);
            controller.showLog("Otrzymano wiadomość od " + sender);
            controller.showMessage(sender + ": " + messageFromXML);
        }  else if (!sender.equals(port)) {
            send(handl.getSoapMessage());
            controller.showLog("Przekazano wiadomość od " + sender + " do " + receiver);
        }
         if (type.equals("Broadcast") && !sender.equals(port)) {
                controller.receive.setText(msg);
                controller.showLog("Otrzymano wiadomość od " + sender);
                controller.showMessage(sender + ": " + messageFromXML);
                if(!nextPort.equals(sender)) {
                    send(handl.getSoapMessage());
                }
            }
    }

}
