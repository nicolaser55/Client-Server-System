// Client

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Client{
	// Initialize Datagram Socket To Send/Recieve Packets
	private static DatagramSocket datagramSocket;
	// IP Address Of Server Packet Will Be Sent TO
	private static InetAddress IPAddress;
	// Byte Array To Store Data To Send To Server
	private static byte[] byteArray;
	// Communications Status
	private static boolean connection_status = true;
	// Window
	private static int window = 4;
	// Sent Counter
	private static int sent_counter = 0;
	// Received Coutner
	private static int received_counter = 0;
	// Sent Message Array
	private static List<String> sent_messages_buffer = new ArrayList<>();
	// Received Message Array
	private static List<String> received_messages_buffer = new ArrayList<>();
	// Boolean To Check If GO BACK N Is Engages
	public volatile static boolean go_back_n = false;
	// Re transmission Buffer
	private static List<String> re_transmission_buffer = new ArrayList<>();
	
	// Constructor To Assign Datagram Socket And IP Address
	public Client(DatagramSocket datagramSocket, InetAddress IPAddress) {
		Client.datagramSocket = datagramSocket;
		Client.IPAddress = IPAddress;
	}
	
	// Send Message To Server
	public void sendMessage() throws IOException, InterruptedException {
		// Check If Window Is Breached For (STOP & WAIT)
		if ((sent_counter-received_counter) <= window) {
			// If Window Is Not Breached, Proceed To Send Message
			System.out.println("Send Message: ");
			// Read Text Typed Into CMD
			Scanner scanner = new Scanner(System.in);
			// Capture User Input
			String userMessage = scanner.nextLine();
			 // Check If User Typed Close In Scanner
			 if (userMessage.toUpperCase().equals("CLOSE")) {
				 // Terminate Connection If User Input Close
				 connection_status = false;
			 }
			// Add Sequence Number To User Message (Use "|" As Delimiter)
			String messageToSend = String.valueOf(sent_counter)+ "|" + userMessage;
			// Convert Message To Bytes
			byteArray = messageToSend.getBytes();
			// Create Datagram Packet From Combined Sequence Number And Message
			DatagramPacket datagramPacket = new DatagramPacket(byteArray, byteArray.length, IPAddress, 1234);
			// Send To Datagram Socket Passed In Constructor
			datagramSocket.send(datagramPacket);
			// Add Message To Sent Message Buffer
			add_to_buffer(messageToSend, sent_messages_buffer);
			// Add Message To Re-Transmission Buffer
			add_to_buffer(messageToSend, re_transmission_buffer);
			// Increase Sent Counter
			sent_counter++;
		// If Window Is Breached
		}else {
			// STOP & WAIT (For Another Acknowledgement From Server)
			System.out.println("STOP & WAIT (Too Many Messages)");
			System.out.println("Wait 5 seconds before sending another message");
			// Sleep For 5 Seconds
			Thread.sleep(5000);
			// Attempt send() Again
			sendMessage();
		}
	}
	
	// Add Message To Corresponding Message Buffer
	public static void add_to_buffer(String message, List<String> arraylist) {
		// If Size Of Message Buffer Is Less Than Window
		if (arraylist.size() < window) {
			// Add Message To Buffer
			arraylist.add(message);
		// If Size Of Message Buffer Is Equal To Or Greater Than Window
		}else {
			// Remove First Item From Buffer
			arraylist.remove(0);
			// Add Message To Buffer
			arraylist.add(message);
		}
	}
	
	// Receive Message From Server
	public synchronized void receiveMessage() throws IOException, InterruptedException {
		// During Go Back N Do Not Receive Messages
		while (go_back_n) {
			continue;
		}
		// Create Datagram Packet
		DatagramPacket datagramPacket = new DatagramPacket(byteArray, byteArray.length, IPAddress, 1234);
		// Recieve Data From Server
		datagramSocket.receive(datagramPacket);
		// Increase Counter When Message Is Received (Acknowledgement)
		received_counter++;
		// Convert Data From Server To String
		String messageFromServer = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
		// Add Message To received Message Buffer
		add_to_buffer(messageFromServer, received_messages_buffer);
		// Index Number Of Delimiter
		int delimiterIndex = messageFromServer.indexOf("|");
		// Extract Message Payload From Server Message
		String message = messageFromServer.substring(delimiterIndex + 1);
		// Print Message From Server
		System.out.println("Server Recieved: " + message);
	}
	
	
	public static void main(String[]args) throws IOException, InterruptedException, ExecutionException, TimeoutException {
		// Create Datagram Socket To Send/Recieve Packets
		DatagramSocket datagramSocket = new DatagramSocket();
		// Get IP Address Of Server To Send Packets
		InetAddress IPAddress = InetAddress.getByName("localhost");
		// Create Client Object With Datagram Socket and IP Address
		Client client = new Client(datagramSocket, IPAddress);
		// Display Greetings
		System.out.println("Welcome Client");
		// Inform Client That Connection Is Set
		System.out.println("Connection To Server is Set.");
		
		// Begin Sending And Receiving Packets
		while(connection_status == true) {
			
			// Communication Logic
			System.out.println("--------START BLOCK--------");
			
			// Record Messages Sent So Far
			int sent_messages = sent_counter;
			
			// Send Message To Server
			client.sendMessage();
			

			// If A Message Has Been Sent, Start Receive Process
			if (sent_counter > sent_messages) {
				
				// Create Executor Service To Access Pool Of Threads
				ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
				
	    		// Submit receive() Method To Executor Service (Cast To Future)
				// This Enables Program Receive Server Acknowledgements On Another Thread
				// Allows User To Continue Sending Messages On Main Thread
				Future<?> future = executor.submit(new Runnable() {
			        public void run() {
						try {
							// Attempt To Receive Message
							client.receiveMessage();
						}catch (CancellationException ce) {
							// Shutdown Executor If Message Received
							executor.shutdownNow();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
			        }
			    });

				// Go-Back-N (8 Seconds Time Limit)
				// Schedule End Of Future (End Of Receive) & (Go-Back-N)
				executor.schedule(new Runnable(){
				    public void run(){
				    	// After 8 Seconds Passed
				    	// Scheduler Will Force The Receive Method To Complete
				    	try {
							// Force Time Limit For Future Get To Catch Timeout Exception On Receive
						    future.get(100, TimeUnit.MILLISECONDS);
						// If Receive Has Not Completed Within Time Limit
						} catch (TimeoutException e) {
							// Cancel The Future (Receive Method)
						    future.cancel(true);
						    
						    // Set Go-Back-N To True To Halt Receiving Messages
					    	go_back_n = true;
						    
						    // Remove All Received Messages From Re Transmission Buffer
					    	// This Will Leave All Lost Messages
						    re_transmission_buffer.removeAll(received_messages_buffer);
			
						    // Get The Oldest Lost Message
							String messageToSend = re_transmission_buffer.get(0);
							// Re-Send Message
							System.out.println("RE-SENDING: "+messageToSend);
							
							//  Convert Timed Out Messages To Bytes
							byteArray = messageToSend.getBytes();
							// Create Datagram Packet
							DatagramPacket datagramPacket = new DatagramPacket(byteArray, byteArray.length, IPAddress, 1234);
							// Send To Datagram Socket
							try {
								datagramSocket.send(datagramPacket);
							// Catch Input Output Exception
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							// Remove Oldest Message From Buffer
							re_transmission_buffer.remove(0);
							
							// When Re-Transmission Array Is Empty
							if (re_transmission_buffer.size() == 0) {
								// Inform Client That All Messages Have Been Retransmitted
								System.out.println("All Messages Have Been Retransmitted");
								// Change Go Back N To False
								go_back_n = false;
							}
	
						// Shut Down Executor Incase Of Interruption
						} catch (InterruptedException e) {
							e.printStackTrace();
							executor.shutdownNow();
						// Shut Down Executor Incase Of Exception
						} catch (ExecutionException e) {
							e.printStackTrace();
							executor.shutdownNow();
						// Shut Down Executor
						} finally {
						    executor.shutdownNow();
						}
				    }
				// 8 Seconds Time Limit For Go-Back-N
				}, 8, TimeUnit.SECONDS);
				
				// Track Sent Messages & Received Messages
				System.out.println("Sent #: "+sent_counter);
				System.out.println("Received #: "+received_counter);
				// Calculate Difference (Window Difference)
				System.out.println("Window Difference: "+(sent_counter-received_counter));
				System.out.println("----------END BLOCK--------");
			}// End Of "If Message Sent"
		}// End Of Connection With Server
		
		// Display Exit Message
		System.out.println("Thank You For Connecting");
		System.out.println("Connection Terminated");
		//Exit Program
		System.exit(0);
	}// End Of Main Method
}

