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
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.tkpphr.android.recorder.R;
import com.tkpphr.android.recorder.view.adapter.SoundRecordListAdapter;

import java.io.File;

public class SoundRecordListView extends LinearLayout{
	private TextView recordName;
	private ListView listView;
	private TextView noRecordsMessage;
	private SoundRecordListAdapter adapter;
	private File selectedFile;
	private OnFileSelectedListener onFileSelectedListener;

	public SoundRecordListView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.sdrc_view_sound_record_list,this,true);
		if(isInEditMode()){
			return;
		}

		recordName=findViewById(R.id.sdrc_record_name);
		listView=findViewById(R.id.sdrc_list_view_record);
		noRecordsMessage =findViewById(R.id.sdrc_no_records_message);
		adapter=new SoundRecordListAdapter(context);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				setSelectedFile(adapter.getItem(i));
			}
		});
	}

	@Nullable
	@Override
	protected Parcelable onSaveInstanceState() {
		if(adapter.getDirectory()==null) {
			return super.onSaveInstanceState();
		}else {
			Bundle outState = new Bundle();
			outState.putParcelable("super_state", super.onSaveInstanceState());
			outState.putSerializable("directory", adapter.getDirectory());
			outState.putSerializable("selected_file",selectedFile);
			return outState;
		}
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if(state instanceof Bundle){
			Bundle savedState=(Bundle)state;
			state=savedState.getParcelable("super_state");
			adapter.reset((File) savedState.getSerializable("directory"));
			File selectedFile=(File) savedState.getSerializable("selected_file");
			if(selectedFile!=null) {
				setSelectedFile(selectedFile);
			}
		}
		super.onRestoreInstanceState(state);
	}

	public void reset(File directory){
		adapter.reset(directory);
		noRecordsMessage.setVisibility(adapter.getCount()==0 ? VISIBLE : INVISIBLE);
		recordName.setText(R.string.sdrc_not_selected_record);
	}

	public void refresh(){
		adapter.refresh();
		if(selectedFile!=null && !selectedFile.exists()){
			unselectFile();
		}
		noRecordsMessage.setVisibility(adapter.getCount()==0 ? VISIBLE : INVISIBLE);
		if(selectedFile==null) {
			recordName.setText(R.string.sdrc_not_selected_record);
		}else {
			recordName.setText(getFileName(selectedFile));
		}
	}

	public File getSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(File file) {
		adapter.refresh();
		int index=adapter.indexOf(file);
		if(index==-1){
			return;
		}
		noRecordsMessage.setVisibility(INVISIBLE);
		listView.setSelection(index);
		listView.setItemChecked(index,true);
		adapter.setSelectedItem(index);
		this.selectedFile=file;
		recordName.setText(getFileName(file));
		if(onFileSelectedListener!=null){
			onFileSelectedListener.onFileSelected(selectedFile);
		}
	}

	public void unselectFile(){
		for(int i=0;i<adapter.getCount();i++){
			listView.setItemChecked(i,false);
		}
		adapter.setSelectedItem(-1);
		recordName.setText(R.string.sdrc_not_selected_record);
		selectedFile=null;
		if(onFileSelectedListener!=null){
			onFileSelectedListener.onFileUnselected();
		}
	}



	private String getFileName(File file){
		String fullFileName=file.getName();
		int index=fullFileName.lastIndexOf(".");
		if(index==-1){
			return fullFileName;
		}else {
			return fullFileName.substring(0,index);
		}
	}

	public void setOnFileSelectedListener(OnFileSelectedListener onFileSelectedListener) {
		this.onFileSelectedListener = onFileSelectedListener;
	}

	public interface OnFileSelectedListener{
		void onFileSelected(File file);
		void onFileUnselected();
	}

}
