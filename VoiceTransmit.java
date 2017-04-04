/*
 * Created on 01/18/2017
 * @authur: Tianhang
*/
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;  
import java.net.SocketTimeoutException; 
import java.net.InetAddress;
import javax.sound.sampled.*;


public class VoiceTransmit {
	boolean stopCapture = false;
	private Socket client;
	private InputStream in;
	private OutputStream out;
	public static void main(String[] args) {
		VoiceTransmit v = new VoiceTransmit();
		v.voiceTransmit();
	}
	
	
	public void voiceTransmit(){
		
		try{
			System.out.println("abc" + 1);
			//client = new Socket("52.14.103.104", 10010);
			client = new Socket("127.0.0.1", 10010); // Create socket
			client.setSoTimeout(10000);
			in = client.getInputStream();
			out = client.getOutputStream();
			Thread capture = new Capture();
			capture.start();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}
		
	}
	
	class heartRateRcv extends Thread{
		public void run(){
			try{
				byte[] buffer = new byte[20];
				DatagramSocket hrSocket = new DatagramSocket();
				DatagramPacket hrPacket = new DatagramPacket(buffer, buffer.length);
				while(!stopCapture){
					hrSocket.receive(hrPacket);
					Send(addMark(hrPacket.getData()));
				}
				sleep(200);
				hrSocket.close();
			} catch (Exception e){
				System.out.println(e);
				System.exit(0);
			}
		}
	}

	class Capture extends Thread {
		PipedOutputStream pos = new PipedOutputStream();
		PipedInputStream pis = new PipedInputStream();
		
		public void run() {
			try {
				pos.connect(pis);
				byte inputBuffer[] = new byte[800];
				AudioFormat audioFormat = getAudioFormat();
				TargetDataLine targetDataLine;
				DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
				targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
				targetDataLine.open(audioFormat);
				targetDataLine.start();
				Thread reader = new Reader();
				reader.start();
				while (!stopCapture) {
					// Read data from the internal buffer of the data line, then write it to pipe.
					int cnt = targetDataLine.read(inputBuffer, 0, inputBuffer.length);
					if (cnt > 0) {
						System.out.println(cnt);
						pos.write(inputBuffer);
					}
				}
				sleep(200);
				client.close();
				pos.close();
				pis.close();
			} catch (Exception e) {
				System.out.println(e);
				System.exit(0);
			}

		}
		
		class Reader extends Thread{
			//read audio data from pipe and send it through tcp socket.
			public void run(){
				byte outputBuffer[] = new byte[100];
				int read = 0;
				while(!stopCapture) {
					try {
						read = pis.read(outputBuffer,0,100);
						if (read > 0)
							//Send("Voice data".getBytes());
							Send(outputBuffer);
							//Send(addMark(outputBuffer));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
	}
	
	public void Send(byte[] data){
		try{			
			out.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] addMark (byte[] b){
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    try {
	    	//outputStream.write(byte(0xff));
	    	outputStream.write("\n".getBytes());
	    	outputStream.write(b);
	    	//outputStream.write(byte(0xff));
	    	outputStream.write("\n".getBytes());
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    return outputStream.toByteArray();
	}
	
	


	public static AudioFormat getAudioFormat() {
		float sampleRate = 8000.0F;
		// 8000,11025,16000,22050,44100
		int sampleSizeInBits = 16;
		// 8,16
		int channels = 1;
		// 1,2
		boolean signed = true;
		// true,false
		boolean bigEndian = true;
		// true,false
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}
}
