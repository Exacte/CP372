import java.awt.EventQueue;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Receiver {

	private JFrame frame;
	private JTextField ios;
	private JTextField udpos;
	private JTextField udpor;
	private JTextField fname;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Receiver window = new Receiver();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Receiver() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 329, 266);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		ios = new JTextField();
		ios.setBounds(10, 39, 86, 20);
		frame.getContentPane().add(ios);
		ios.setColumns(10);
		
		udpos = new JTextField();
		udpos.setBounds(10, 83, 86, 20);
		frame.getContentPane().add(udpos);
		udpos.setColumns(10);
		
		udpor = new JTextField();
		udpor.setBounds(10, 121, 86, 20);
		frame.getContentPane().add(udpor);
		udpor.setColumns(10);
		
		fname = new JTextField();
		fname.setBounds(10, 165, 86, 20);
		frame.getContentPane().add(fname);
		fname.setColumns(10);
		
		JLabel lblIpOfSender = new JLabel("IP of Sender");
		lblIpOfSender.setBounds(10, 24, 86, 14);
		frame.getContentPane().add(lblIpOfSender);
		
		JLabel lblUdpOfSender = new JLabel("UDP of Sender");
		lblUdpOfSender.setBounds(10, 70, 86, 14);
		frame.getContentPane().add(lblUdpOfSender);
		
		JLabel lblUdpOfReceiver = new JLabel("UDP of Receiver");
		lblUdpOfReceiver.setBounds(10, 106, 86, 14);
		frame.getContentPane().add(lblUdpOfReceiver);
		
		JLabel lblFileNameTo = new JLabel("File Name to Write to");
		lblFileNameTo.setBounds(10, 152, 108, 14);
		frame.getContentPane().add(lblFileNameTo);
		
		JButton btnSetInfo = new JButton("Set Info");
		btnSetInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] input = new String[5];
				String ios_text = ios.getText();
				String udpos_text = udpor.getText();
				String udpor_text = udpos.getText();
				String fname_text = fname.getText();
			    input[0] = ios_text;
			    input[1] = udpor_text;
			    input[2] = udpos_text;
			    input[3] = fname_text;
			    try {
					receive(input);
				} catch (IOException ex) {
		        	System.out.println("I/O Exception");
				}
			}
		});
		btnSetInfo.setBounds(176, 120, 89, 23);
		frame.getContentPane().add(btnSetInfo);
	}
	
	private static void receive(String[] input) throws IOException {
		InetAddress ios = InetAddress.getByName(input[0]);
        int udpos = new Integer(input[1]).intValue();
        int udpor = new Integer(input[2]).intValue();
        String fname = input[3];
        
        DatagramSocket socket = null;
        DatagramPacket packet = null;
        DatagramPacket ackPacket = null;
        FileInputStream finput = null;
        boolean fExists;
		byte[] packetBuffer = new byte[125];
    	byte[] seqNum = new byte[1];
		byte[] fileBuffer = new byte[124];
		byte[] ack = new byte[1];
		int bRead = 0;
        
        socket = new DatagramSocket(udpor);
        socket.setSoTimeout(500);
        
        try {
        	finput = new FileInputStream(fname);
        	fExists = true;
        } catch (FileNotFoundException e) {
        	fExists = false;
        }
        
        if (fExists) {
    		while ((bRead = finput.read(fileBuffer)) != -1) {
    			packetBuffer = concat(seqNum, fileBuffer);
    	        packet = new DatagramPacket(packetBuffer, packetBuffer.length, ios, udpos);
    	        socket.send(packet);
    	        
    	        while(ack[0] != 1) {
    	        	ack = receiveACK(socket, ack, ackPacket, packet);
    	        }

    	        ack = new byte[1];
    	        seqNum[0] = (seqNum[0] == 0) ? (byte)1 : (byte)0;
    	        fileBuffer = new byte[124];
    		}
    		
    		seqNum[0] = (byte)-1;
    		fileBuffer = new byte[124];
    		packetBuffer = concat(seqNum, fileBuffer);
	        packet = new DatagramPacket(packetBuffer, packetBuffer.length, ios, udpos);
	        socket.send(packet);
	        
	        while(ack[0] != 1) {
	        	ack = receiveACK(socket, ack, ackPacket, packet);
	        }
	        
	    	finput.close();
        }
        socket.close();
	}
	public static byte[] receiveACK(DatagramSocket socket, byte[] ack, DatagramPacket ackPacket, DatagramPacket packet) {
        ackPacket = new DatagramPacket(ack, ack.length);
        try {
        	socket.receive(ackPacket);
        } catch (SocketTimeoutException ex) {
    	    System.out.println("Timeout, Resend");
        } catch (IOException ex) {
        	System.out.println("I/O Exception");
        }
        if (ack[0] != 1) {
        	try {
        		socket.send(packet);
        	} catch (IOException ex) {
	        	System.out.println("I/O Exception");
	        }
        }
        return ack;
    }
    
    public static byte[] concat(byte[] a, byte[] b) {
    	byte[] c = new byte[a.length + b.length];
    	System.arraycopy(a, 0, c, 0, a.length);
    	System.arraycopy(b, 0, c, a.length, b.length);
    	return c;
    }
}