package com.tkpphr.android.recorder.view.customview;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tkpphr.android.recorder.R;
import com.tkpphr.android.recorder.sound.SoundRecorder;
import com.tkpphr.android.recorder.view.OnSoundRecordListener;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SoundRecorderView extends LinearLayout{
	private TextView recorderMessage;
	private TextView limitMessage;
	private ImageView startButton;
	private TextView stopButton;
	private EditText recordName;
	private TextView nameErrorMessage;
	private SoundRecorder soundRecorder;
	private File saveDirectory;
	private OnSoundRecordListener onSoundRecordListener;

	public SoundRecorderView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.sdrc_view_sound_recorder,this,true);
		if(isInEditMode()){
			return;
		}

		soundRecorder=new SoundRecorder();
		soundRecorder.addCallback(soundRecorderCallback);
		recorderMessage=findViewById(R.id.sdrc_recorder_message);
		recorderMessage.setText(getContext().getString(R.string.sdrc_record_ready));
		limitMessage =findViewById(R.id.sdrc_limit_message);
		setMaxDuration(0);
		nameErrorMessage =findViewById(R.id.sdrc_name_error_message);
		recordName=findViewById(R.id.sdrc_record_name);
		recordName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				recordNameCheck(charSequence.toString());
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});
		startButton =findViewById(R.id.sdrc_record_start_button);
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				if (recordName.hasFocus()) {
					InputMethodManager inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(recordName.getWindowToken(), 0);
				}
				soundRecorder.start(saveDirectory,recordName.getText().toString());
			}
		});
		TypedValue outValue=new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.textColorPrimary,outValue,true);
		startButton.getDrawable().setColorFilter(ResourcesCompat.getColor(getResources(),outValue.resourceId,context.getTheme()), PorterDuff.Mode.SRC_ATOP);
		stopButton =findViewById(R.id.sdrc_record_stop_button);
		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				soundRecorder.stop();
			}
		});
		recordNameCheck(recordName.getText().toString());
	}

	@Nullable
	@Override
	protected Parcelable onSaveInstanceState() {
		if(saveDirectory==null) {
			return super.onSaveInstanceState();
		}else {
			Bundle outState = new Bundle();
			outState.putParcelable("super_state", super.onSaveInstanceState());
			outState.putString("record_name", recordName.getText().toString());
			outState.putLong("max_duration", soundRecorder.getMaxDuration());
			outState.putSerializable("save_directory", saveDirectory);
			return outState;
		}
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if(state instanceof Bundle){
			Bundle savedState=(Bundle)state;
			state=savedState.getParcelable("super_state");
			reset((File) savedState.getSerializable("save_directory"),savedState.getLong("max_duration"));
			recordName.setText(savedState.getString("record_name"));
		}
		super.onRestoreInstanceState(state);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		soundRecorder.removeCallback(soundRecorderCallback);
		soundRecorder.release();
	}

	public void reset(@NonNull File saveDirectory, long maxDuration){
		this.saveDirectory=saveDirectory;
		setMaxDuration(maxDuration);
		startButton.setEnabled(true);
		recordName.setEnabled(true);
	}

	public void refresh(){
		recordNameCheck(recordName.getText().toString());
	}

	public void setMaxDuration(long maxDuration){
		soundRecorder.setMaxDuration(maxDuration);
		if(maxDuration<0){
			limitMessage.setText("");
		}else {
			String timeLimit=String.format(Locale.ROOT,"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(maxDuration),
								TimeUnit.MILLISECONDS.toMinutes(maxDuration) % TimeUnit.HOURS.toMinutes(1),
								TimeUnit.MILLISECONDS.toSeconds(maxDuration) % TimeUnit.MINUTES.toSeconds(1));
			limitMessage.setText(String.format("%s(%s)",getContext().getString(R.string.sdrc_record_limit),timeLimit));
		}
	}

	public void setOnSoundRecordListener(OnSoundRecordListener onSoundRecordListener) {
		this.onSoundRecordListener = onSoundRecordListener;
	}

	private void recordNameCheck(String recordName){
		boolean inputError;
		if(TextUtils.isEmpty(recordName)){
			inputError=true;
			nameErrorMessage.setText(getContext().getString(R.string.sdrc_name_empty));
		}else if(isExistsFileName(recordName)){
			inputError=true;
			nameErrorMessage.setText(getContext().getString(R.string.sdrc_name_exists));
		}else {
			inputError=false;
			nameErrorMessage.setText("");
		}
		startButton.setVisibility(inputError ? INVISIBLE : VISIBLE);
	}

	private boolean isExistsFileName(final String fileName){
		if(!saveDirectory.isDirectory()){
			return false;
		}
		File[] files=saveDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
		});
		if(files==null){
			return false;
		}
		String fileNameLowerCase=fileName.toLowerCase();
		for(File file : files){
			int index=file.getName().lastIndexOf(".");
			if(index!=-1 && fileNameLowerCase.equals(file.getName().toLowerCase().substring(0,index))) {
				return true;
			}
		}
		return false;
	}


	private final SoundRecorder.Callback soundRecorderCallback=new SoundRecorder.Callback() {

		@Override
		public void onStart() {
			recordName.setEnabled(false);
			startButton.setVisibility(INVISIBLE);
			stopButton.setVisibility(VISIBLE);
			if(onSoundRecordListener!=null){
				onSoundRecordListener.onRecordStart();
			}
		}

		@Override
		public void onStop(File outputFile) {
			recordName.setEnabled(true);
			startButton.setVisibility(VISIBLE);
			stopButton.setVisibility(INVISIBLE);
			recorderMessage.setText(String.format("%s : %s",getContext().getString(R.string.sdrc_record_finished),recordName.getText()));
			recordNameCheck(recordName.getText().toString());
			if(onSoundRecordListener!=null){
				onSoundRecordListener.onRecordStop(outputFile);
			}
		}

		@Override
		public void onTickRecordTime(String time) {
			recorderMessage.setText(String.format("%s %s",getContext().getString(R.string.sdrc_recording),time));
		}
	};

}
