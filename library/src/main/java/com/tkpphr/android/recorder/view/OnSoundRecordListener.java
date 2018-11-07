package com.tkpphr.android.recorder.view;

import java.io.File;

public interface OnSoundRecordListener {
	void onRecordStart();
	void onRecordStop(File outputFile);
}