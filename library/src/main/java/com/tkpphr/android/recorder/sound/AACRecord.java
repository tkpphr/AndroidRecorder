package com.tkpphr.android.recorder.sound;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

public class AACRecord implements SoundRecord {
	private MediaRecorder mediaRecorder;
	private String outputFilePath;
	private static final String FILE_EXTENSION=".3gp";

	public AACRecord(File saveDirectory, String fileName){
		this.outputFilePath=saveDirectory.getAbsolutePath()+"/"+fileName+FILE_EXTENSION;
	}

	@Override
	public void start() {
		mediaRecorder=new MediaRecorder();
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		mediaRecorder.setOutputFile(outputFilePath);
		try {
			mediaRecorder.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mediaRecorder.start();
	}

	@Override
	public void stop(){
		if(mediaRecorder!=null) {
			mediaRecorder.stop();
			mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder = null;
		}
	}

	@Override
	public void release(){
		stop();
	}

	@Override
	public String getOutputFilePath() {
		return outputFilePath;
	}


}
