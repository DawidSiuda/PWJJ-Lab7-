package Interfaces;

public interface ReceiverInterface {
	public void receiveMessage(String msg, int sourcePort, int destinationPort, String type);
	public void asyncLog(String str);
}
