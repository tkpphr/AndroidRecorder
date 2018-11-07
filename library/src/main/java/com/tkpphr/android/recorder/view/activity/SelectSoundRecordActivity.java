package com.tkpphr.android.recorder.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;


import com.tkpphr.android.recorder.R;
import com.tkpphr.android.recorder.view.OnSoundRecordListener;
import com.tkpphr.android.recorder.view.customview.SoundRecordListView;
import com.tkpphr.android.recorder.view.customview.SoundRecordPlayerView;
import com.tkpphr.android.recorder.view.dialog.SoundRecorderDialog;

import java.io.File;

public class SelectSoundRecordActivity extends AppCompatActivity implements OnSoundRecordListener, SoundRecordListView.OnFileSelectedListener{
	private Toolbar toolbar;
	private SoundRecordListView soundRecordListView;
	private SoundRecordPlayerView soundPlayerView;
	private Button createNewButton;
	private Button selectButton;
	private File selectedFile;

	public static Intent createIntent(Context context, String title,File saveDirectory, long maxDurationInMilliseconds){
		Intent intent=new Intent(context,SelectSoundRecordActivity.class);
		intent.putExtra("title",title);
		intent.putExtra("save_directory",saveDirectory);
		intent.putExtra("max_duration",maxDurationInMilliseconds);
		return intent;
	}

	public static String getSelectedFilePath(Intent resultData){
		return resultData.getStringExtra("selected_file_path");
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final File saveDirectory=(File)getIntent().getSerializableExtra("save_directory");
		final long maxDuration=getIntent().getLongExtra("max_duration",0);
		setContentView(R.layout.sdrc_activity_select_sound_record);
		toolbar=findViewById(R.id.sdrc_toolbar);
		if(getActionBar()==null && getSupportActionBar()==null) {
			setSupportActionBar(toolbar);
		}else {
			toolbar.setVisibility(View.GONE);
		}
		setTitle(getIntent().getStringExtra("title"));
		selectButton=findViewById(R.id.sdrc_select_button);
		soundPlayerView=findViewById(R.id.sdrc_sound_player_view);
		soundRecordListView =findViewById(R.id.sdrc_list_view);
		soundRecordListView.setOnFileSelectedListener(this);
		createNewButton=findViewById(R.id.sdrc_create_new_button);
		createNewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				soundPlayerView.pause();
				SoundRecorderDialog.newInstance(saveDirectory,maxDuration).show(getSupportFragmentManager(),null);
			}
		});

		selectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectedFile = soundRecordListView.getSelectedFile();
				Intent intent=new Intent();
				intent.putExtra("selected_file_path",selectedFile.getAbsolutePath());
				setResult(RESULT_OK,intent);
				finish();
			}
		});
		setResult(RESULT_CANCELED);
		if(savedInstanceState==null){
			soundRecordListView.reset(saveDirectory);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		soundRecordListView.refresh();
	}

	@Override
	protected void onPause() {
		super.onPause();
		soundPlayerView.pause();
	}

	@Override
	public void onFileSelected(File file) {
		soundPlayerView.load(file.getAbsolutePath());
		selectButton.setEnabled(true);
	}

	@Override
	public void onFileUnselected() {
		soundPlayerView.unload();
		selectButton.setEnabled(false);
	}

	@Override
	public void onRecordStart() {

	}

	@Override
	public void onRecordStop(File outputFile) {
		soundRecordListView.setSelectedFile(outputFile);
	}


}
