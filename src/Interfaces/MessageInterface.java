package Interfaces;

import javax.jws.WebMethod;
import javax.jws.WebService;

//@WebService
@WebService(targetNamespace = "http://SOAPNode/wsdl")
public interface MessageInterface {
	@WebMethod public Boolean sentReceiveMessage(String message);
}
