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

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;

import java.io.IOException;

public class SoundRecordPlayer {
	private String filePath;
	private MediaPlayer mediaPlayer;
	private MediaPlayer.OnCompletionListener onCompletionListener;
	private OnPlaybackPositionUpdateListener onPlaybackPositionUpdateListener;
	private int maxDuration;
	private int seekPosition;
	private Handler seekTimer;
	private static final int PLAYBACK_UPDATE_PERIOD =100;

	public SoundRecordPlayer(String filePath){
		this.filePath =filePath;
		this.seekTimer=new Handler();
		this.seekPosition=0;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(filePath);
		String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		retriever.release();
		if(TextUtils.isEmpty(duration)){
			maxDuration=0;
		}else {
			maxDuration=Integer.parseInt(duration);
		}
	}

	public void release(){
		seekTimer.removeCallbacks(tick);
		if(mediaPlayer!=null){
			mediaPlayer.setOnCompletionListener(null);
			mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer=null;
		}

	}

	public void play(){
		if(mediaPlayer!=null){
			return;
		}
		mediaPlayer=new MediaPlayer();
		try {
			mediaPlayer.setDataSource(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				release();
				seekPosition=maxDuration;
				if(onPlaybackPositionUpdateListener!=null){
					onPlaybackPositionUpdateListener.onPositionUpdate(seekPosition);
				}
				if(onCompletionListener!=null){
					onCompletionListener.onCompletion(mediaPlayer);
				}
			}
		});

		try {
			mediaPlayer.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(maxDuration > 0) {
			mediaPlayer.seekTo(seekPosition);
			seekTimer.postDelayed(tick,PLAYBACK_UPDATE_PERIOD);
		}
		mediaPlayer.start();
	}

	public void stop() {
		release();
	}

	public void setSeekPosition(int seekPositionInMillisecond){
		this.seekPosition=seekPositionInMillisecond;
	}

	public int getMaxDuration(){
		return maxDuration;
	}

	public void setOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener) {
		this.onCompletionListener = onCompletionListener;
	}

	public void setOnPlaybackPositionUpdateListener(OnPlaybackPositionUpdateListener onPlaybackPositionUpdateListener) {
		this.onPlaybackPositionUpdateListener = onPlaybackPositionUpdateListener;
	}

	private final Runnable tick=new Runnable() {
		@Override
		public void run() {
			seekPosition=Math.min(seekPosition+PLAYBACK_UPDATE_PERIOD,maxDuration);
			if(onPlaybackPositionUpdateListener!=null){
				onPlaybackPositionUpdateListener.onPositionUpdate(seekPosition);
			}
			if(seekPosition < getMaxDuration()) {
				seekTimer.postDelayed(tick, PLAYBACK_UPDATE_PERIOD);
			}else {
				seekTimer.removeCallbacks(tick);
			}
		}
	};

	public interface OnPlaybackPositionUpdateListener{
		void onPositionUpdate(int seekPosition);
	}
}