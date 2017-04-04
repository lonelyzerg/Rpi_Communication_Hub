import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;  
import java.net.SocketTimeoutException; 
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.sound.sampled.*;

public class Server {
	private ServerSocket serverSocket;
	boolean stopFlag = false;
	PipedInputStream pis;
	PipedOutputStream pos;
	public static void main(String[] args) {
		Server s = new Server();
		s.server();
	}
	
	public void server(){
		try{
			serverSocket = new ServerSocket(10010);
			while(!stopFlag){
				Socket socket = serverSocket.accept();
				System.out.println("Started");
				Handle h = new Handle(socket);
				h.start();
				Play p = new Play();
				p.start();
			}
		} catch (Exception e){
			System.out.println(e);
			System.exit(0);
		}
	}
	
	class Handle extends Thread{
		Socket s;
		
		public Handle(Socket s){
			this.s = s;
		}
		
		public void run(){
			try{
				pos = new PipedOutputStream();
				InputStream in = s.getInputStream();
				byte[] data = new byte[10000];
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				int len = 0;
				int length = 0;
				
				while((len = in.read(data)) != 0){
					//System.out.println(data);
					//outputStream.write(data, 0, len);
					pos.write(data, 0, len);
//					length += 1;
//					
//					if(length >= 1){
//						System.out.println("play one piece of data.");
//						Thread play = new Play(outputStream.toByteArray());
//						outputStream.reset();
//						play.start();
//						length = 0;
//					}
				}
			} catch (Exception e){
				System.out.println(e);
				System.exit(0);
			}
		}
	}
	
	class Play extends Thread{
		
		byte tempBuffer[] = new byte[10000];
		private AudioFormat audioFormat;
		private AudioInputStream audioInputStream;
		private SourceDataLine sourceDataLine;
		byte[] audioData;
		
//		public Play(byte[] data){
//			this.audioData = data;		
//		}
		public void run(){
			try{
				pis = new PipedInputStream();
				pis.connect(pos);
				sleep(300);
				
				//Get an input stream on the
				// byte array containing the data
				//InputStream bis = new ByteArrayInputStream();
				AudioFormat audioFormat = getAudioFormat();
				System.out.println(pis.available());
				//audioInputStream = new AudioInputStream(bis,audioFormat,audioData.length/audioFormat.getFrameSize());
				
				DataLine.Info dataLineInfo =new DataLine.Info(SourceDataLine.class,audioFormat);
				sourceDataLine = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
				sourceDataLine.open(audioFormat);
				sourceDataLine.start();
				
				//Create a thread to play back
				// the data and start it
				// running.  It will run until
				// all the data has been played
				// back.
				int cnt;
				//Keep looping until the input
				// read method returns -1 for
				// empty stream.
				while((cnt = pis.read(tempBuffer)) != -1){
					//cnt = audioInputStream.read(tempBuffer)
					System.out.println(pis.available());
					if(cnt > 0){
						//System.out.println("playing");
						//Write data to the internal
						// buffer of the data line
						// where it will be delivered
						// to the speaker.
						sourceDataLine.write(
								tempBuffer, 0, cnt);
					}
				}
				sourceDataLine.drain();
				sourceDataLine.close();
			}catch (Exception e) {
				System.out.println(e);
				System.exit(0);
			}//end catch
		}//end run
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