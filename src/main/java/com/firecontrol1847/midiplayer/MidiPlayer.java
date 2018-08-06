package com.firecontrol1847.midiplayer;

import java.io.File;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;

public class MidiPlayer {

	public static final File midi = new File(
			"D:\\Users\\FireController1847\\Music\\Midi\\Disney_Pixar_Up_Theme_Piano.mid");
	public static final String outputDeviceName = "Keppy's Synthesizer";

	public static void main(String[] args) throws Exception {
		System.out.println("Reading file " + midi.getName());
		Sequence seq = MidiSystem.getSequence(midi);
		Integer bpm = getBpmFromSequence(seq);
		if (bpm == null)
			throw new Error("Unable to find BPM from Midi.");

		// Fetch Midi Device
		MidiDevice.Info[] devicesInfo = MidiSystem.getMidiDeviceInfo();
		MidiDevice device = null;
		for (MidiDevice.Info info : devicesInfo) {
			if (info.getName().equals(outputDeviceName))
				device = MidiSystem.getMidiDevice(info);
		}
		device.open();

		System.out.println(
				"Selected device: " + device.getDeviceInfo().getName() + " v" + device.getDeviceInfo().getVersion());

		// Setup & Play
		Sequencer sequencer = MidiSystem.getSequencer(false);
		sequencer.getTransmitter().setReceiver(device.getReceiver());
		sequencer.setSequence(seq);
		sequencer.open();
		long lengthSeconds = seq.getMicrosecondLength() / 1000 / 1000;
		System.out.println("Now playing " + midi.getName() + ". Duration: "
				+ String.format("%d:%02d", lengthSeconds / 60, lengthSeconds % 60));
		sequencer.start();

		while (true) {
			if (sequencer.isRunning()) {
				try {
					Thread.sleep(1000);
					long currentLengthSeconds = sequencer.getMicrosecondPosition() / 1000 / 1000;
					System.out.println("Current time: " + String.format("%d:%02d", currentLengthSeconds / 60, currentLengthSeconds % 60));
				} catch (Exception i) {
					break;
				}
			} else {
				break;
			}
		}

		sequencer.stop();
		sequencer.close();
		device.close();
	}

	public static Integer getBpmFromSequence(Sequence seq) {
		Track[] tracks = seq.getTracks();
		for (Track track : tracks) {
			for (int i = 0; i < track.size(); i++) {
				MidiMessage msg = track.get(i).getMessage();
				String type = String.format("%02X", msg.getMessage()[1]);
				if (type.equals("51")) {
					MetaMessage mmsg = (MetaMessage) msg;
					byte[] data = mmsg.getData();
					int tempo = (data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff);
					return 60000000 / tempo;
				}
			}
		}
		return null;
	}

}
