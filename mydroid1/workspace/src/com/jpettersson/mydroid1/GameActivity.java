/*
Copyright 2011 Jonathan A Pettersson  

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.jpettersson.mydroid1;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class GameActivity extends Activity {
	private Game game = null;
	private SensorManager sensorManager;
	
	@SuppressWarnings("deprecation")
	private final SensorListener sensorListener = new SensorListener() {

	  	  public void onSensorChanged(int sensor, float[] values) {
	  	    updateOrientation(values[SensorManager.DATA_Z], 
	  	                      values[SensorManager.DATA_Y], 
	  	                      values[SensorManager.DATA_X]);
	  	  }

	        public void onAccuracyChanged(int sensor, int accuracy) {}
	  	};

  	private void updateOrientation(float roll, float pitch, float heading) {
  		if(game != null) {
  			game.updateOrientation(roll, pitch);
  		}
    }
	  	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        game = new Game(this);
        setContentView(game.getSurfaceView());
        
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerListeners();
        
		super.onCreate(savedInstanceState);
    }
    
    private void registerListeners()
  	{
  		sensorManager.registerListener(sensorListener, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_FASTEST);
  		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 500.0f, this);
  	  	//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 100.0f, this);
  	}
  	
  	private void removeListeners()
  	{
  		sensorManager.unregisterListener(sensorListener);
  	}
    
    @Override
	protected void onPause() {
		//stage.pause();
    	removeListeners();
		super.onPause();
	}

	@Override
	protected void onResume() {
		//stage.resume();
		registerListeners();
		super.onResume();
	}

	protected void onStop() {
		//stage.stop();
		super.onStop();
	}
	
	protected boolean isFullscreenOpaque() {
		return true;
	}
}