package pub;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Publisher_cmd {
	public static void main(String[] args) {
		new Publisher_cmd();
	}

	Publisher_cmd() {
		try {
			
			//video
			Process process_v = new ProcessBuilder("/home/lonelyzerg/bin/ffmpeg", // ffmpeg location
					"-thread_queue_size", "512",
					"-f", "alsa", 
					"-ac", "2", 
					"-ar", "44100", 
					"-i", "default", 
					"-acodec", "libfdk_aac",
					"-strict", "-2",
					"-f", "video4linux2", 
					"-i", "/dev/video0", // input
					"-tune", "zerolatency",																								// file
					"-vcodec", "libx264",
					"-preset", "ultrafast",
					//"-crf", "22",
					//"-g", "20",
					"-framerate", "10",
					
					"-f", "flv",
					"pipe:1" // this tells ffmpeg to outout the result to stdin)
			).start();
			
			//audio
			Process process_a = new ProcessBuilder("/home/lonelyzerg/bin/ffmpeg", // ffmpeg location
					"-thread_queue_size", "512",
					"-f", "alsa", 
					"-ac", "2", 
					"-ar", "44100", 
					"-i", "default", 
					"-acodec", "aac",
					"-strict", "-2",
					"-f", "flv",
					"pipe:1" // this tells ffmpeg to outout the result to stdin)
			).start();
			
			//video
			new Thread() {
				public void run() {
					try {
						MqttPub_v m = new MqttPub_v();
						InputStream is = process_v.getInputStream();
						ByteArrayOutputStream b = new ByteArrayOutputStream();
						byte[] data = new byte[700000];
						// do whatever has to be done with inputStream
						//BufferedReader r = new BufferedReader(new InputStreamReader(is));
						BufferedReader er = new BufferedReader(new InputStreamReader(process_v.getErrorStream()));
						while (true) {
							if (is.available() != 0) {
								//System.out.println(l);
								int cnt = is.read(data);
								if(cnt > 700000)
									System.out.println("overflow!");
								b.reset();
								b.write(data, 0, cnt);
								b.flush();
								m.pub(b.toByteArray());
								//System.out.println("recorded!");
								Thread.sleep(50);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
			
			//audio
			new Thread() {
				public void run() {
					try {
						MqttPub_a m = new MqttPub_a();
						InputStream is = process_a.getInputStream();
						ByteArrayOutputStream b = new ByteArrayOutputStream();
						byte[] data = new byte[700000];
						// do whatever has to be done with inputStream
						//BufferedReader r = new BufferedReader(new InputStreamReader(is));
						BufferedReader er = new BufferedReader(new InputStreamReader(process_v.getErrorStream()));
						while (true) {
							if (is.available() != 0) {
								//System.out.println(l);
								int cnt = is.read(data);
								if(cnt > 700000)
									System.out.println("overflow!");
								b.reset();
								b.write(data, 0, cnt);
								b.flush();
								m.pub(b.toByteArray());
								//System.out.println("recorded!");
								Thread.sleep(50);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

class MqttPub_v implements MqttCallback {
	MqttClient client;
	// PipedInputStream pis;
	MqttMessage message;

	public MqttPub_v() {
		try {
			// this.pis = pis;
			client = new MqttClient("tcp://localhost:1883", "video_publisher");

			client.connect();
			System.out.println("Publisher connected!");
			message = new MqttMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void pub(byte[] data) {
		try {

			// client.subscribe("h264");
			System.out.print("\rPublished data: " + data.length + ".\t");
			message.setPayload(data);
			// message.setPayload("test!".getBytes());
			client.publish("h264", message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println(message);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}
}

class MqttPub_a implements MqttCallback {
	MqttClient client;
	// PipedInputStream pis;
	MqttMessage message;

	public MqttPub_a() {
		try {
			// this.pis = pis;
			client = new MqttClient("tcp://localhost:1883", "audio_publisher");

			client.connect();
			System.out.println("Publisher connected!");
			message = new MqttMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void pub(byte[] data) {
		try {

			// client.subscribe("h264");
			System.out.print("\rPublished data: " + data.length + ".\t");
			message.setPayload(data);
			// message.setPayload("test!".getBytes());
			client.publish("aac", message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println(message);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}
}
