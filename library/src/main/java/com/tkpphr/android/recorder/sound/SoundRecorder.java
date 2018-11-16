/*
   Copyright 2018 tkpphr

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
