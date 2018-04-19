package sub;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class Subscriber_cmd {
	public static void main(String[] args) {
		new Subscriber_cmd();
	}

	Subscriber_cmd() {
		try {

			Process process = new ProcessBuilder("/home/lonelyzerg/bin/ffplay",// ffmpeg location
					//"-vcodec", "libx264",
					"-f", "flv",
					"-fflags", "nobuffer",
					"-flags", "low_delay",
					"-framedrop",
					"-i", "-"
					//"-"
			
			// this tells ffmpeg to outout the result to stdin)
			).start();
			new Thread() {
				public void run() {
					try {
						PipedOutputStream pos = new PipedOutputStream();
						PipedInputStream pis = new PipedInputStream(pos, 900000);
						mqttSub m = new mqttSub(pos);
						OutputStream os = process.getOutputStream();
						byte[] data = new byte[600000];
						// do whatever has to be done with inputStream
						// BufferedReader r = new BufferedReader(new InputStreamReader(is));
						BufferedReader er = new BufferedReader(new InputStreamReader(process.getErrorStream()));
						while (true) {
							int cnt = pis.read(data);
							os.write(data, 0, cnt);
							System.out.println("Playing!");

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

class mqttSub implements MqttCallback {
	MqttClient client;
	PipedOutputStream pos;
	int rcv;

	public mqttSub(PipedOutputStream pos) {
		try {
			this.rcv = 0;
			this.pos = pos;
			client = new MqttClient("tcp://localhost:1883", "subscriber");
			client.connect();
			System.out.println("Subscriber connected!");
			client.setCallback(this);
			client.subscribe("h264");

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// System.out.print("\rReceived byte: " + message.getPayload().length + ".");
		byte[] payload = message.getPayload();
		// System.out.print("Received " + ++rcv + " length: " + payload.length + "\n");
		pos.write(payload);
		pos.flush();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}
}