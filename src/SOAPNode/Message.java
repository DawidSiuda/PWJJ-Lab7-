package SOAPNode;

import javax.jws.WebService;

import Interfaces.MessageInterface;
import Interfaces.ReceiverInterface;

//@WebService(endpointInterface = "SOAPNode.Message",
//			targetNamespace = "http:/SOAPNode")
@WebService(
		name="Message",
        portName = "MessagePort",
        serviceName = "MessageService",
        targetNamespace = "http://SOAPNode/wsdl",
        endpointInterface = "SOAPNode.Message")
public class Message implements MessageInterface {

	private ReceiverInterface receiver;
	public Message(ReceiverInterface receiver) {
		this.receiver = receiver;
	}

	@Override
	public Boolean sentReceiveMessage(String message) {
		if(receiver!= null)
			receiver.receiveMessage(message);
		return true;
	}
}
