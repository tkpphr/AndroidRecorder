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
package com.tkpphr.android.recorder.view.adapter;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.tkpphr.android.recorder.R;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SoundRecordListAdapter extends BaseAdapter{
	private Context context;
	private File directory;
	private File[] files;
	private int selectedItem;

	public SoundRecordListAdapter(Context context){
		this.context=context;
		this.selectedItem=-1;
	}

	@Override
	public int getCount() {
		return files==null ? 0 : files.length;
	}

	@Override
	public File getItem(int i) {
		return files[i];
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null){
			convertView= LayoutInflater.from(context).inflate(R.layout.sdrc_sound_record_list_item,parent,false);
		}

		File file=getItem(position);
		if(selectedItem==position){
			((CheckedTextView)convertView.findViewById(R.id.sdrc_choice_indicator_single)).setChecked(true);
		}else {
			((CheckedTextView)convertView.findViewById(R.id.sdrc_choice_indicator_single)).setChecked(false);
		}
		((TextView)convertView.findViewById(R.id.sdrc_file_name)).setText(getFileName(file));
		((TextView)convertView.findViewById(R.id.sdrc_file_date)).setText(getLastModifiedDate(file));
		((TextView)convertView.findViewById(R.id.sdrc_file_size)).setText(getReadableFileSize(file));
		((TextView)convertView.findViewById(R.id.sdrc_file_sound_length)).setText(getSoundLengthInHms(file));
		return convertView;
	}

	public File getDirectory() {
		return directory;
	}

	public int getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(int selectedItem) {
		this.selectedItem = selectedItem;
		notifyDataSetChanged();
	}

	public void reset(File directory){
		this.directory=directory;
		refresh();
	}

	public void refresh(){
		files=directory.listFiles(new FileFilter() {
				  @Override
				  public boolean accept(File file) {
					  String fileNameLowerCase = file.getName();
					  if(fileNameLowerCase.endsWith(".wav")){
						  return file.isFile();
					  }
					  return false;
				  }
			  });
		if(files!=null){
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File file1, File file2) {
					if(file1.lastModified()==file2.lastModified()) return 0;
					return file1.lastModified()>=file2.lastModified() ? -1 : 1;
				}
			});
		}
		notifyDataSetChanged();
	}

	public int indexOf(File file){
		if(files==null){
			return -1;
		}
		for(int i=0 ; i<files.length;i++){
			if(files[i].getAbsolutePath().equals(file.getAbsolutePath())){
				return i;
			}
		}
		return -1;
	}

	public String getFileName(File file){
		String fullFileName=file.getName();
		int index=fullFileName.lastIndexOf(".");
		if(index==-1){
			return fullFileName;
		}else {
			return fullFileName.substring(0,index);
		}
	}

	private String getLastModifiedDate(File file){
		return DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.SHORT, Locale.getDefault())
				.format(new Date(file.lastModified()));
	}

	private String getReadableFileSize(File file){
		if(!file.exists()){
			return "";
		}
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(file.length())/Math.log10(1024));
		if(digitGroups >= 0 && digitGroups < units.length){
			return new DecimalFormat("#,##0.#").format(file.length()/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
		}else {
			return "0 B";
		}
	}

	private String getSoundLengthInHms(File file){
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(file.getAbsolutePath());
		String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		int millis;
		retriever.release();
		if(TextUtils.isEmpty(duration)){
			millis=0;
		}else {
			millis=Integer.parseInt(duration);
		}
		return String.format(Locale.ROOT,"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
				TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
	}
}
