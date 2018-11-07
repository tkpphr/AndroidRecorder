package com.tkpphr.android.recorder.sound;

public interface SoundRecord {
	void start();
	void stop();
	void release();
	String getOutputFilePath();
}
