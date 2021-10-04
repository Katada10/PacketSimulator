import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class PacketSender extends Thread{

	static Scanner sc;
	public void run() {
		sc = new Scanner(System.in);

		System.out.println("Please enter the server address: ");

		String serverIp = sc.nextLine();
		
		connectToServer(serverIp);
		
	}
	private void connectToServer(String serverIp) {
		// TODO Auto-generated method stub
		Socket socket;
		try {
			socket = new Socket(serverIp, 80);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintStream out = new PrintStream(socket.getOutputStream(), true);
			
			String inData = "";

			while((inData = in.readLine()) != null) {
				if(inData.equalsIgnoreCase("close"))
					break;
				else if(inData.equalsIgnoreCase("connected"))
					sendData(socket);
				else
					System.out.println(inData);
				
			}
			
			in.close();
			out.close();
			socket.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void sendData(Socket s) {
		
		System.out.println("Please enter the payload: ");
		String payloadStr = sc.nextLine();

		// Create the packet and send it
		String packet = createPacket(payloadStr);
	
		PrintStream out;
		try {
			out = new PrintStream(s.getOutputStream());
			
			System.out.println("Sending packet");
			out.println(packet);
			
			System.out.println("Packet sent, awaiting response...");
			return;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) {
		
		PacketSender sender = new PacketSender();
		sender.start();
	}


	private String createPacket(String payloadStr) {
		String payload = createPayload(payloadStr);

		String header = createHeader(payload.replace(" ", "").length() / 2, 0x1c46, "c0a80003", "c0a80001");

		while ((header.replace(" ", "").length() + payload.replace(" ", "").length()) % 8 != 0) {
			// fix divisible by 8
			String padding = "0";

			payload = padding + payload;

		}

		String packet = header + " " + payload;
		return packet;
	}

	private String createHeader(int payloadLength, int id, String clientIp, String serverIp) {

		// 2 hex letters = 1 byte

		// Header length in hex (make it 4 bits always)
		String headerLength = Integer.toHexString(20 + payloadLength);

		// This bit of code adds padding to make it 4 hex bits
		if (headerLength.length() < 4) {
			String padding = "";
			for (int i = 0; i < 4 - headerLength.length(); i++) {
				padding += "0";
			}
			headerLength = padding + headerLength;
		}

		String checkSum = calculateChecksum(id, clientIp, serverIp, headerLength);

		String header = "4500 " + headerLength + " " + Integer.toHexString(id) + " 4000 " + "4006 " + checkSum + " "
				+ clientIp.substring(0, 4) + " " + clientIp.substring(4, 8) + " " + serverIp.substring(0, 4) + " "
				+ serverIp.substring(4, 8);

		return header;
	}

	private String calculateChecksum(int id, String clientIp, String serverIp, String headerLength) {
		int firstSum = 0x0000;
		;

		firstSum += 0x4000 + 0x4006 + 0x4500 + Integer.parseInt(headerLength, 16) + id
				+ Integer.parseInt(clientIp.substring(0, 4), 16) + Integer.parseInt(clientIp.substring(4, 8), 16)
				+ Integer.parseInt(serverIp.substring(0, 4), 16) + Integer.parseInt(serverIp.substring(4, 8), 16);

		String temp = Integer.toHexString(firstSum);
		int carrySum = 0;

		if (temp.length() > 4) {
			carrySum = Integer.parseInt(temp.substring(0, 1), 16)
					+ Integer.parseInt(temp.substring(1, temp.length()), 16);
		}

		String checksum = Integer.toHexString(0xFFFF - carrySum);

		return checksum;
	}

	private String createPayload(String payloadStr) {
		StringBuffer payload = new StringBuffer();

		int counter = 0;
		for (char c : payloadStr.toCharArray()) {
			if (counter == 2) {
				counter = 0;
				payload.append(" ");
			}

			payload.append(Integer.toHexString(c));
			counter++;
		}

		return payload.toString();
	}

}
