sensor.py is used to get heart rate sensor data by bluetooth and send it to localhost with udp. pybluez should be installed.
see https://github.com/karulis/pybluez

VoiceTransmit.java is used to receive the heart rate data and record voice and send them to a remote server (localhost here, should be AWS).

Server.java is used to receive heart rate data and voice data and playback the voice recorded.

##Update:


