package com.tkpphr.android.recorder.sound;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SoundRecorder {
	private SoundRecord soundRecord;
	private boolean isRecording;
	private RecorderTimer recorderTimer;
	private final List<Callback> callbacks;

	public SoundRecorder(){
		this.callbacks=new ArrayList<>();
		this.isRecording=false;
		this.recorderTimer=new RecorderTimer(100);
		this.recorderTimer.addCallback(new RecorderTimer.Callback() {
			@Override
			public void onTick(String time) {
				for(Callback callback : callbacks) {
					callback.onTickRecordTime(time);
				}
			}

			@Override
			public void onFinish() {
				stop();
			}
		});
	}

	public long getMaxDuration(){
		return recorderTimer.getMaxDuration();
	}

	public void setMaxDuration(long maxDuration){
		recorderTimer.setMaxDuration(maxDuration);
	}

	public void addCallback(Callback callback){
		this.callbacks.add(callback);
	}

	public void removeCallback(Callback callback){
		this.callbacks.remove(callback);
	}

	public void start(File saveDirectory,String fileName){
		if(isRecording()){
			return;
		}
		if(!saveDirectory.exists()){
			if(!saveDirectory.mkdirs()){
				return;
			}
		}

		soundRecord =new PCMRecord(saveDirectory,fileName);
		soundRecord.start();
		for (Callback callback:callbacks){
			callback.onStart();
		}
		recorderTimer.start();
		isRecording=true;
	}

	public void stop(){
		if(!isRecording()){
			return;
		}
		if(soundRecord !=null) {
			soundRecord.stop();
		}
		recorderTimer.stop();
		isRecording=false;
		File outputFile=new File(soundRecord.getOutputFilePath());
		for(Callback callback : callbacks){
			callback.onStop(outputFile);
		}
	}

	public void release(){
		if(soundRecord !=null) {
			soundRecord.stop();
			soundRecord.release();
		}
		recorderTimer.stop();
		isRecording=false;
	}

	public boolean isRecording() {
		return isRecording;
	}

	public interface Callback{
		void onStart();
		void onStop(File outputFile);
		void onTickRecordTime(String time);
	}
}
