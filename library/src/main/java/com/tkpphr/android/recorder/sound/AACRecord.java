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
