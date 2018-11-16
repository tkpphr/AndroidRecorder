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
package com.tkpphr.android.recorder.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;


import com.tkpphr.android.recorder.R;
import com.tkpphr.android.recorder.view.OnSoundRecordListener;
import com.tkpphr.android.recorder.view.customview.SoundRecorderView;

import java.io.File;

public class SoundRecorderDialog extends AppCompatDialogFragment{
	private SoundRecorderView soundRecorderView;
	private OnSoundRecordListener onSoundRecordListener;

	public static SoundRecorderDialog newInstance(File saveDirectory, long maxDurationInMilliseconds) {

		Bundle args = new Bundle();
		args.putSerializable("save_directory", saveDirectory);
		args.putLong("max_duration",maxDurationInMilliseconds);
		SoundRecorderDialog fragment = new SoundRecorderDialog();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if(context instanceof OnSoundRecordListener){
			onSoundRecordListener =(OnSoundRecordListener)context;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
			if (activity instanceof OnSoundRecordListener) {
				onSoundRecordListener = (OnSoundRecordListener) activity;
			}
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if(getTargetFragment() instanceof OnSoundRecordListener){
			onSoundRecordListener =(OnSoundRecordListener)getTargetFragment();
		}else if(getParentFragment() instanceof OnSoundRecordListener){
			onSoundRecordListener =(OnSoundRecordListener) getParentFragment();
		}
		AlertDialog.Builder dialog=new AlertDialog.Builder(getContext());
		View view=View.inflate(getContext(), R.layout.sdrc_dialog_sound_recorder,null);
		soundRecorderView=(SoundRecorderView) view;
		soundRecorderView.setOnSoundRecordListener(new OnSoundRecordListener() {
			@Override
			public void onRecordStart() {
				setCancelable(false);
				if(onSoundRecordListener!=null){
					onSoundRecordListener.onRecordStart();
				}
			}

			@Override
			public void onRecordStop(File outputFile) {
				setCancelable(true);
				if(onSoundRecordListener !=null){
					onSoundRecordListener.onRecordStop(outputFile);
				}
			}
		});
		if(savedInstanceState==null){
			Bundle args=getArguments();
			soundRecorderView.reset((File)args.getSerializable("save_directory"),args.getLong("max_duration"));
		}
		setCancelable(true);
		return dialog.setView(view).create();
	}

	@Override
	public void onResume() {
		super.onResume();
		soundRecorderView.refresh();
	}

}
