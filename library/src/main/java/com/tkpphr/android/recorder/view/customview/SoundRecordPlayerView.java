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
package com.tkpphr.android.recorder.view.customview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.tkpphr.android.recorder.R;
import com.tkpphr.android.recorder.sound.SoundRecordPlayer;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SoundRecordPlayerView extends TableLayout{
	private TextView soundDuration;
	private SeekBar soundSeekBar;
	private ImageView playbackButton;
	private Drawable playIcon;
	private Drawable pauseIcon;
	private SoundRecordPlayer mediaPlayerSound;
	private boolean isPlaying;

	public SoundRecordPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.sdrc_view_sound_record_player,this,true);
		if(isInEditMode()){
			return;
		}
		initialize();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		releaseSound();
	}

	public void load(String filePath){
		setMediaPlayerSound(new SoundRecordPlayer(filePath));
	}

	public void unload(){
		setMediaPlayerSound(null);
	}

	public void play(){
		if(!isPlaying){
			playbackButton.performClick();
		}
	}

	public void pause(){
		if(isPlaying){
			playbackButton.performClick();
		}
	}

	private void initialize(){
		this.soundDuration =findViewById(R.id.sdrc_sound_duration);
		this.soundSeekBar=findViewById(R.id.sdrc_sound_seek_bar);
		this.soundSeekBar.setEnabled(false);
		this.soundSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		this.playbackButton =findViewById(R.id.sdrc_playback_button);
		this.playIcon = ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_play,getContext().getTheme());
		this.pauseIcon = ResourcesCompat.getDrawable(getResources(),android.R.drawable.ic_media_pause,getContext().getTheme());
		TypedValue outValue=new TypedValue();
		getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary,outValue,true);
		int color=ResourcesCompat.getColor(getResources(), outValue.resourceId,getContext().getTheme());
		this.playIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		this.pauseIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		this.playbackButton.setImageDrawable(playIcon);
		this.playbackButton.setEnabled(false);
		this.playbackButton.setOnClickListener(onClickPlaybackListener);
		this.isPlaying=false;
		updateDurationText();
	}

	private void setMediaPlayerSound(@Nullable SoundRecordPlayer mediaPlayerSound){
		releaseSound();
		this.mediaPlayerSound=mediaPlayerSound;
		isPlaying=false;
		if(mediaPlayerSound==null){
			playbackButton.setEnabled(false);
			soundSeekBar.setEnabled(false);
		}else {
			playbackButton.setEnabled(true);
			soundSeekBar.setEnabled(true);
			soundSeekBar.setMax(this.mediaPlayerSound.getMaxDuration());
			mediaPlayerSound.setOnPlaybackPositionUpdateListener(onPlaybackPositionUpdateListener);
			mediaPlayerSound.setOnCompletionListener(onCompletionListener);
		}
		soundSeekBar.setProgress(0);
		playbackButton.setImageDrawable(playIcon);
		updateDurationText();
	}

	private void releaseSound(){
		if(mediaPlayerSound!=null){
			mediaPlayerSound.setOnCompletionListener(null);
			mediaPlayerSound.setOnPlaybackPositionUpdateListener(null);
			mediaPlayerSound.stop();
			mediaPlayerSound.release();
			mediaPlayerSound=null;
		}
	}

	private void updateDurationText(){
		int duration=soundSeekBar.getProgress();
		String durationText=String.format(Locale.ROOT,"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration),
											TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
											TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
		soundDuration.setText(durationText);
	}

	private final SoundRecordPlayer.OnPlaybackPositionUpdateListener onPlaybackPositionUpdateListener=new SoundRecordPlayer.OnPlaybackPositionUpdateListener() {
		@Override
		public void onPositionUpdate(int seekPosition) {
			soundSeekBar.setProgress(seekPosition);
		}
	};

	private final MediaPlayer.OnCompletionListener onCompletionListener=new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			isPlaying=false;
			playbackButton.setImageDrawable(playIcon);
		}
	};

	private final OnClickListener onClickPlaybackListener=new OnClickListener() {
		@Override
		public void onClick(View view) {
			if(isPlaying){
				playbackButton.setImageDrawable(playIcon);
				mediaPlayerSound.stop();
				isPlaying=false;
			}else {
				playbackButton.setImageDrawable(pauseIcon);
				if(soundSeekBar.getProgress()==soundSeekBar.getMax()) {
					mediaPlayerSound.setSeekPosition(0);
				}
				mediaPlayerSound.play();
				isPlaying=true;
			}
		}
	};

	private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			if(isPlaying){
				mediaPlayerSound.stop();
			}
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mediaPlayerSound.setSeekPosition(seekBar.getProgress());
			if(isPlaying){
				mediaPlayerSound.play();
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
			updateDurationText();
		}
	};

}
