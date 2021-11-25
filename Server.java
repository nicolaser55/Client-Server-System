// Server

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Server {
	// Initialize Datagram Socket To Send/Recieve Packets
	private DatagramSocket datagramSocket;
	// Byte Array To Store Data Sent From Client
	private byte[] byteArray = new byte[256];
	// Received Messages Counter (-1 Because Sent Messages Start At 0)
	private static int received_counter = -1;
	// Save All Received Messages
	private static List<String> received_messages = new ArrayList<>();
	
	// Constructor To Initialize Datagram Socket
	public Server(DatagramSocket datagramSocket) {
		this.datagramSocket = datagramSocket;
	}
	
	// Method To Test GO-BACK-N
	public static void test_GO_BACK_N(String message) throws InterruptedException {
		// Delay Speficic Message (TEST GO-BACK-N)
		if (message.equals("ignore")) {
			// Display Delay Warning
			System.out.println("Message Ignored: 10 Second Delay");
			// Delay Acknowledgement For 10 Seconds
			TimeUnit.SECONDS.sleep(10);
		}
	}
	
	// Method To Test STOP AND WAIT
	public static void test_STOP_AND_WAIT() throws InterruptedException {
		// Set Artificial Delay (TEST STOP & WAIT)
		System.out.println("2 second delay");
		// Delay Acknowledgement For 2 Seconds
		TimeUnit.SECONDS.sleep(2);
	}
	
	// Method Will Recieve A Message Then Send It Back
	public void receiveThenSend() throws InterruptedException {
		// While Loop To Keep Server Running
		while (true) {
			// Attempt To Receive A Datagram Packet
			try {
				// Create UDP Datagram Packet
				DatagramPacket datagramPacket = new DatagramPacket(byteArray, byteArray.length);
				// Recieve Packets From Client (sender)
				datagramSocket.receive(datagramPacket);
				// Get the IP Address From Client (sender)
				InetAddress IPAddress = datagramPacket.getAddress();
				// Get Port Number of Client (sender)
				int port = datagramPacket.getPort();
				// Convert Client Packet Into String
				String messageFromClient = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
				// Display Full Message From Client
				System.out.println("Message From Client: "+messageFromClient);
				
				// If Message Received Has Been Sent Before (RE-TRANSMISSION)
				if (received_messages.contains(messageFromClient)) {
					// Display That Message Has Already Been Received
					System.out.println(messageFromClient+" [has already been received]");
					
				// If Message Received Is New
				}else {
					// Add Message To Array List
					received_messages.add(messageFromClient);
					// Increase Received Counter
					received_counter++;
					// Obtain Index Number Of Delimiter
					int delimiterIndex = messageFromClient.indexOf("|");
					// Get Sequence Number Of Message
					String sequenceNumber = messageFromClient.substring(0,delimiterIndex);
					// Print Obtained And Expected SN
					System.out.println("SN: "+sequenceNumber+" Expected SN: "+received_counter);
					// If Sequence Number Matches Sequence Number Expected
					if (Integer.valueOf(sequenceNumber) == received_counter) {
						// Get Message Content
						String message = messageFromClient.substring(delimiterIndex + 1);
						
						// Test Go Back N By Delaying Specific Message
						test_GO_BACK_N(message);
						// Test STOP and WAIT By Delaying All Messages
//						test_STOP_AND_WAIT();
						
						// Reconstruct Datagram Packet
						datagramPacket = new DatagramPacket(byteArray, byteArray.length, IPAddress, port);
						// Send Acknowledgement Back To Client
						datagramSocket.send(datagramPacket);
						// Display That Message Has Been Sent Back
						System.out.println("'"+message+"' Sent Back");
					// If Sequence Number Does Not Match Sequence Number Expected
					}else {
						// Display That SN Did Not Match Expected SN
						System.out.println("SN Does Not Match Expected SN");
					}
				}
			// End Of While Loop (Catch Input Output Exception)
			}catch(IOException e) {
				e.printStackTrace();
				// End Program
				break;
			}
		}
	}
	
	public static void main(String[] args) throws SocketException, InterruptedException {
		System.out.println("Server is Online");
		// Create Datagram Socket
		DatagramSocket datagramSocket = new DatagramSocket(1234);
		// Create Server With Datagram Socket
		Server server = new Server(datagramSocket);
		// Server Recieves Then Sends Message
		server.receiveThenSend();
	}
}
