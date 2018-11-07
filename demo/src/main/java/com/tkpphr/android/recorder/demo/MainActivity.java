package com.tkpphr.android.recorder.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.tkpphr.android.recorder.demo.databinding.ActivityMainBinding;
import com.tkpphr.android.recorder.view.activity.SelectSoundRecordActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
	private File saveDirectory;
	private static final long MAX_DURATION=10*1000L;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding=DataBindingUtil.setContentView(this, R.layout.activity_main);
		setSupportActionBar(binding.toolbar);
		saveDirectory=getFilesDir();
		if(!saveDirectory.exists()){
			saveDirectory.mkdirs();
		}

		binding.buttonSelect.setOnClickListener(view-> {
			startActivityForResult(SelectSoundRecordActivity.createIntent(this, "Select Record",saveDirectory,MAX_DURATION),0);
		});
		ActivityCompat.requestPermissions(this,new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"},0);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		for(String permission:permissions){
			if(ActivityCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
				Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
				finish();
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,0,0,"Delete Records");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==0){
			File[] files=saveDirectory.listFiles();
			if(files!=null) {
				for (File file : files) {
					file.delete();
				}
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK && requestCode==0){
			String filePath= SelectSoundRecordActivity.getSelectedFilePath(data);
			selectFile(filePath);
		}
	}

	private void selectFile(String filePath){
		binding.textViewFilePath.setText(filePath);
		binding.soundPlayerView.load(filePath);
	}
}
