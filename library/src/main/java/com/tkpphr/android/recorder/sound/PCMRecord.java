package com.tkpphr.android.recorder.sound;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class PCMRecord implements SoundRecord {
	private AudioRecord audioRecord;
	private FileChannel fileChannel;
	private String outputFilePath;
	private int samplingRate;
	private int minBufferSize;
	private int frameBufferSize;
	private short[] shortDataBuffer;
	private ByteBuffer dataBuffer;
	private ByteBuffer sizeBuffer;
	private static final String FILE_EXTENSION=".wav";

	public PCMRecord(File saveDirectory, String fileName){
		this(saveDirectory,fileName,24000);
	}

	public PCMRecord(File saveDirectory, String fileName, int samplingRate){
		this.outputFilePath=saveDirectory.getAbsolutePath()+"/"+fileName+FILE_EXTENSION;
		this.samplingRate = samplingRate;
		this.minBufferSize=AudioRecord.getMinBufferSize(samplingRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
		this.frameBufferSize=this.minBufferSize/2/10;
	}

	@Override
	public void start(){
		try {
			File outputFile=new File(outputFilePath);
			if(outputFile.exists()) {
				if(!outputFile.delete()){
					return;
				}
			}else {
				if (!outputFile.createNewFile()) {
					return;
				}
			}
			fileChannel=new FileOutputStream(outputFile).getChannel();
			fileChannel.write(createHeader(samplingRate,1,16));
		} catch (IOException e) {
			e.printStackTrace();
		}
		shortDataBuffer =new short[frameBufferSize];
		dataBuffer=ByteBuffer.allocate(frameBufferSize*2);
		dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
		sizeBuffer=ByteBuffer.allocate(4);
		sizeBuffer.order(ByteOrder.LITTLE_ENDIAN);
		audioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,minBufferSize);
		audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
			@Override
			public void onMarkerReached(AudioRecord audioRecord) {

			}

			@Override
			public void onPeriodicNotification(AudioRecord audioRecord) {
				writeData();
			}
		});
		audioRecord.setPositionNotificationPeriod(frameBufferSize);
		audioRecord.startRecording();
		audioRecord.read(shortDataBuffer,0,frameBufferSize);
	}

	@Override
	public void stop(){
		if(audioRecord!=null){
			audioRecord.setRecordPositionUpdateListener(null);
			audioRecord.setPositionNotificationPeriod(0);
			audioRecord.stop();
			audioRecord.release();
			audioRecord=null;
		}
		if(fileChannel!=null) {
			try {
				fileChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fileChannel=null;
		}
		dataBuffer=null;
		sizeBuffer=null;
		shortDataBuffer =null;
	}

	@Override
	public void release(){
		stop();
	}

	@Override
	public String getOutputFilePath() {
		return outputFilePath;
	}

	private ByteBuffer createHeader(int rate, int channel, int bit){
		ByteBuffer byteBuffer=ByteBuffer.allocate(44);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		byteBuffer.put("RIFF".getBytes());//4
		byteBuffer.putInt(36);//8
		byteBuffer.put("WAVE".getBytes());//12
		byteBuffer.put("fmt ".getBytes());//16
		byteBuffer.putInt(16);//20
		byteBuffer.put(new byte[]{1,0});//22
		byteBuffer.putShort((short)channel);//24
		byteBuffer.putInt(rate);//28
		byteBuffer.putInt((rate*channel)*(bit/8));//32
		byteBuffer.putShort((short)((bit*channel)/8));//34
		byteBuffer.putShort((short)bit);//36
		byteBuffer.put("data".getBytes());//40
		byteBuffer.putInt(0);//44
		byteBuffer.flip();
		return byteBuffer;
	}

	private void writeData(){
		if(!(audioRecord!=null && fileChannel!=null)){
			return;
		}
		try {
			dataBuffer.clear();
			dataBuffer.position(0);
			audioRecord.read(shortDataBuffer,0,frameBufferSize);
			for(short data : shortDataBuffer){
				dataBuffer.putShort(data);
			}
			dataBuffer.flip();
			fileChannel.position(fileChannel.size());
			fileChannel.write(dataBuffer);

			fileChannel.position(4);
			sizeBuffer.clear();
			sizeBuffer.position(0);
			sizeBuffer.putInt((int)fileChannel.size());
			sizeBuffer.flip();
			fileChannel.write(sizeBuffer);

			fileChannel.position(40);
			sizeBuffer.clear();
			sizeBuffer.position(0);
			sizeBuffer.putInt((int)(fileChannel.size()-44));
			sizeBuffer.flip();
			fileChannel.write(sizeBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
