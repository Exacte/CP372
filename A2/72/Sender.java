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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;

public class Sender {

	private static JFrame frame;
	private JTextField ior;
	private JTextField udpor;
	private JTextField udpos;
	private JTextField fname;
	private JButton btnTransfer;
	private JTextField maxsize;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Sender window = new Sender();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Sender() {
		initialize();
	}

	public void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 356, 367);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JCheckBox chckbxUnreliable = new JCheckBox("Unreliable");
		chckbxUnreliable.setBounds(193, 71, 97, 23);
		frame.getContentPane().add(chckbxUnreliable);
		
		ior = new JTextField();
		ior.setBounds(43, 72, 86, 20);
		frame.getContentPane().add(ior);
		ior.setColumns(10);
		
		udpor = new JTextField();
		udpor.setBounds(43, 120, 86, 20);
		frame.getContentPane().add(udpor);
		udpor.setColumns(10);
		
		udpos = new JTextField();
		udpos.setBounds(43, 164, 86, 20);
		frame.getContentPane().add(udpos);
		udpos.setColumns(10);
		
		fname = new JTextField();
		fname.setBounds(43, 210, 86, 20);
		frame.getContentPane().add(fname);
		fname.setColumns(10);
		
		btnTransfer = new JButton("Transfer");
		btnTransfer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String[] input = new String[5];
				String ior_text = ior.getText();
				String udpor_text = udpor.getText();
				String udpos_text = udpos.getText();
				String fname_text = fname.getText();
				String maxsize_text = maxsize.getText();
			    input[0] = ior_text;
			    input[1] = udpor_text;
			    input[2] = udpos_text;
			    input[3] = fname_text;
			    input[5] = maxsize_text;
				if(chckbxUnreliable.isSelected()) {
				  input[4] = "10";
				} else{
				  input[4] = "0";
				}
			    try {
					send(input);
				} catch (IOException ex) {
		        	System.out.println("I/O Exception");
		        }
			}
		});
		btnTransfer.setBounds(43, 241, 89, 23);
		frame.getContentPane().add(btnTransfer);
		
		maxsize = new JTextField();
		maxsize.setBounds(139, 210, 86, 20);
		frame.getContentPane().add(maxsize);
		maxsize.setColumns(10);
		
		JLabel lblIpOfReceiver = new JLabel("IP of Receiver");
		lblIpOfReceiver.setBounds(43, 53, 86, 14);
		frame.getContentPane().add(lblIpOfReceiver);
		
		JLabel lblUdpOfReceiver = new JLabel("UDP of Receiver");
		lblUdpOfReceiver.setBounds(43, 103, 86, 14);
		frame.getContentPane().add(lblUdpOfReceiver);
		
		JLabel lblUdpOfSender = new JLabel("UDP of Sender");
		lblUdpOfSender.setBounds(43, 149, 86, 14);
		frame.getContentPane().add(lblUdpOfSender);
		
		JLabel lblFileName = new JLabel("File Name");
		lblFileName.setBounds(43, 195, 46, 14);
		frame.getContentPane().add(lblFileName);
		
		JLabel lblMaxSizeUdp = new JLabel("Max Size. UDP");
		lblMaxSizeUdp.setBounds(139, 195, 86, 14);
		frame.getContentPane().add(lblMaxSizeUdp);
		
		JLabel lblReport = new JLabel("Report");
		lblReport.setBounds(193, 245, 46, 14);
		frame.getContentPane().add(lblReport);
	}
	
	private static void send(String[] input) throws IOException {
		InetAddress ior = InetAddress.getByName(input[0]);
        int udpor = new Integer(input[1]).intValue();
        int udpos = new Integer(input[2]).intValue();
        String fname = input[3];
        int reliability = new Integer(input[4]).intValue();
        
        DatagramSocket socket = null;
        DatagramPacket packet = null;
		FileOutputStream foutput = new FileOutputStream(fname);
		byte[] packetBuffer = new byte[Integer.valueOf(input[5])];
		byte[] seqNum = new byte[1];
		byte[] fileBuffer = new byte[Integer.valueOf(input[5])-1];
		byte[] ack = new byte[1];
		ack[0] = (byte)1;
        int prevSeqNum = 1;
		int packetcount = 0;
		boolean lostpacket = false;
		boolean start = false;
        socket = new DatagramSocket(udpos);
        long startTime = 0;
		
		while(true) {
		    packet = new DatagramPacket(packetBuffer, packetBuffer.length);
		    socket.receive(packet);
			if (start == false){
				startTime = System.currentTimeMillis();
				start = true;
			}
		    System.arraycopy(packetBuffer, 0, seqNum, 0, seqNum.length);
		    System.arraycopy(packetBuffer, 1, fileBuffer, 0, fileBuffer.length);
		    packetcount += 1;
		    
		    if (reliability != 0) lostpacket = packetcount % reliability == 0;
		    if (seqNum[0] != -1 && !lostpacket) {
		    	if (seqNum[0] != prevSeqNum) {
		    		foutput.write(fileBuffer);
		    		prevSeqNum = seqNum[0];
		    	}
	    		packet = new DatagramPacket(ack, ack.length, ior, udpor);
	    		socket.send(packet);
		    
		    } else {
	    		packet = new DatagramPacket(ack, ack.length, ior, udpor);
	    		socket.send(packet);
		    	break;
		    }
	    }
		final long endTime = System.currentTimeMillis();
		
		JLabel lblReportText = new JLabel("");
		lblReportText.setBounds(193, 263, 137, 54);
		frame.getContentPane().add(lblReportText);
		long ftime = endTime - startTime;
		lblReportText.setText(Long.toString(ftime));
		
		foutput.close();
        socket.close();
	}
}