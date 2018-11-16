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

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RecorderTimer {
	private long maxDuration;
	private long elapsedMilliseconds;
	private Handler recordTimer;
	private Handler delayTimer;
	private List<Callback> callbacks;
	private long timerPeriod;

	public RecorderTimer(long timerPeriod){
		this.maxDuration=0;
		this.callbacks=new ArrayList<>();
		this.timerPeriod=timerPeriod;
	}

	public void addCallback(Callback callback){
		callbacks.add(callback);
	}

	public void removeCallback(Callback callback){
		callbacks.remove(callback);
	}

	public void start(){
		start(0);
	}

	public void start(long delayInMills){
		if (!isFinished() && recordTimer == null && delayTimer==null) {
			if(delayInMills > 0) {
				delayTimer=new Handler();
				delayTimer.postDelayed(delay,delayInMills);
			}else {
				delay.run();
			}
		}
	}

	public void stop() {
		if(delayTimer!=null){
			delayTimer.removeCallbacks(delay);
			delayTimer=null;
		}
		if(recordTimer !=null) {
			recordTimer.removeCallbacks(tick);
			recordTimer = null;
		}
		elapsedMilliseconds = 0;
	}

	public long getMaxDuration(){
		return maxDuration;
	}

	public void setMaxDuration(long maxDurationInMilliseconds){
		stop();
		this.maxDuration=maxDurationInMilliseconds;
	}

	public boolean isFinished(){
		return maxDuration > 0 && elapsedMilliseconds>=maxDuration;
	}

	public String millisToHms(long millis){
		return String.format(Locale.ROOT,"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
				TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
	}

	private final Runnable tick=new Runnable() {
		@Override
		public void run() {
			elapsedMilliseconds+= timerPeriod;
			for(Callback callback : callbacks){
				callback.onTick(millisToHms(elapsedMilliseconds));
			}
			if(isFinished()){
				for(Callback callback : callbacks){
					callback.onFinish();
				}
			}else {
				recordTimer.postDelayed(tick, timerPeriod);
			}
		}
	};

	private final Runnable delay=new Runnable() {
		@Override
		public void run() {
			for (Callback callback : callbacks) {
				callback.onTick(millisToHms(elapsedMilliseconds));
			}
			recordTimer = new Handler();
			recordTimer.postDelayed(tick, timerPeriod);
		}
	};

	public interface Callback{
		void onTick(String time);
		void onFinish();
	}
}
