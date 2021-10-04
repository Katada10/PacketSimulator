import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PacketReceiver extends Thread {

	public static void main(String[] args) {
		PacketReceiver receiver = new PacketReceiver();
		receiver.start();
	}

	public void run() {
		try {
			ServerSocket socket = new ServerSocket(80);
			
			System.out.println("Server started, waiting for connection...");
			
			Socket client = socket.accept();

			System.out.println("Client Connected, waiting for packet...");

			PrintStream out = new PrintStream(client.getOutputStream(), true);
			
			out.println("connected");
			
			
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			String inData = "";

			while ((inData = in.readLine()) != null) {
				receiveMessage(inData);	
				out.println("Packet received successfuly");
				break;
			}
			
			
			
			out.println("close");
			
		
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// receiveMessage(message);
	}

	private void receiveMessage(String message) {
		message = message.replace(" ", "");

		String headerLength = message.substring(4, 8);

		String id = message.substring(8, 12);

		String checksum = message.substring(20, 24);

		String clientIp = message.substring(24, 32);

		String serverIp = message.substring(32, 40);

		String data = message.substring(40, message.length());

		String decodedClient = Integer.decode("0x" + clientIp.substring(0, 2)) + "."
				+ Integer.decode("0x" + clientIp.substring(2, 4)) + "."
				+ Integer.decode("0x" + clientIp.substring(4, 6)) + "."
				+ Integer.decode("0x" + clientIp.substring(6, 8));

		StringBuilder asciiData = new StringBuilder();

		for (int i = 0; i < data.length(); i += 2) {
			asciiData.append((char) Integer.parseInt(data.substring(i, i + 2), 16));
		}

		String realData = asciiData.toString().trim();

		int packetLength = Integer.decode("0x" + headerLength);
		int dataLength = packetLength - 20;

		System.out.println("The data received from " + decodedClient + " is " + realData);
		System.out.println("The data has " + dataLength * 8 + " bits or " + dataLength
				+ " bytes. Total length of the packet is " + packetLength + " bytes.");
		System.out.println(verifyChecksum(headerLength, id, checksum, clientIp, serverIp));

	}

	private String verifyChecksum(String headerLength, String id, String checksum, String clientIp, String serverIp) {
		int firstSum = 0x0000;

		firstSum += 0x4000 + 0x4006 + 0x4500 + Integer.parseInt(headerLength, 16) + Integer.parseInt(id, 16)
				+ +Integer.parseInt(clientIp.substring(0, 4), 16) + Integer.parseInt(clientIp.substring(4, 8), 16)
				+ Integer.parseInt(serverIp.substring(0, 4), 16) + Integer.parseInt(serverIp.substring(4, 8), 16)
				+ Integer.parseInt(checksum, 16);

		String temp = Integer.toHexString(firstSum);
		int carrySum = 0;

		if (temp.length() > 4) {
			carrySum = Integer.parseInt(temp.substring(0, 1), 16)
					+ Integer.parseInt(temp.substring(1, temp.length()), 16);
		}

		if ((0xFFFF - carrySum) != 0) {
			return "The verification of the checksum demonstrates that the packet received is corrupted. Packet\r\n"
					+ "discarded!";
		}

		return "The verification of the checksum demonstrates that the packet received is correct.";
	}

}
