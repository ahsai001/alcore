package com.ahsailabs.alcore.utils.audio;

public final class BackSoundVolumeEvent {
	private float volume = -1;
	public BackSoundVolumeEvent(float volume) {
		this.volume = volume;
	}
	public float getVolume() {
		return volume;
	}
	public void setVolume(float volume) {
		this.volume = volume;
	}
}
