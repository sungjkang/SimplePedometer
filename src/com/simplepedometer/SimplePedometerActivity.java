package com.simplepedometer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;


public class SimplePedometerActivity extends Activity implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor sAccelerometer;
	private Sensor sLinearAcceleration;
	private Sensor sGravity;
	private Sensor sMagnet;
	private TextView output;
	private float inR[] = new float[16];
	private float outR[] = new float[16];
	private float result[] = new float[4];
	private float gravity[] = null;
	private float linearAcceleration[] = {0,0,0,0};
	private float magnet[] = null;
	private int step = 0;
	private float localMin = 0;
	private float localMax = 0;
	private float localMean = 0;
	private float xyAcceleration = 0;
	private int threshold = 0;
	private float direction = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_pedometer);
        
        output = (TextView) findViewById(R.id.output);
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sMagnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_simple_pedometer, menu);
        return true;
    }

    @Override
    public void onResume() {
    	super.onResume();
    	mSensorManager.registerListener(this, sAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    	mSensorManager.registerListener(this, sMagnet, SensorManager.SENSOR_DELAY_FASTEST);
    	mSensorManager.registerListener(this, sLinearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);
    	mSensorManager.registerListener(this, sGravity, SensorManager.SENSOR_DELAY_FASTEST);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mSensorManager.unregisterListener(this);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	mSensorManager.unregisterListener(this);
    }
    
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			gravity = event.values;
		}
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magnet = event.values;
		}
		if (gravity != null && magnet != null && event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			displayData(event.values);
		}		
		
	}
	
	private void displayData(float[] values) {
		SensorManager.getRotationMatrix(inR, null, gravity, magnet);
		android.opengl.Matrix.invertM(outR, 0, inR, 0);
		
		output.setText("");
		
		linearAcceleration[0] = values[0];
		linearAcceleration[1] = values[1];
		linearAcceleration[2] = values[2];
		
		
		//check if phone is being held up right
		if (0.9 < outR[6] && outR[6] < 1.1 ) {
			//read in qr code//
			return;
		}
		
		android.opengl.Matrix.multiplyMV(result, 0, outR, 0, linearAcceleration, 0);
		
		xyAcceleration = android.util.FloatMath.sqrt(result[0]*result[0] + result[1]*result[1]);
		
		
		//xyMovement = false;
		if (xyAcceleration > 1.3) {
			//xyMovement = true;
			
			if (localMax < result[2]) {
				localMax = result[2];
			}
			if (localMin > result[2] && result[2] < localMax) {
				localMin = result[2];
			}
			localMean = (localMax + localMin) / 2;
			//if (localMedian - 0.2 >= result[2] && result[2] <= localMedian + 0.2) {
			if (localMin <= localMean && localMean <= localMax) {
				threshold++;
				//step++;
				localMax = 0;
				localMin = 0;
				
			}
			if (threshold > 50) {
				threshold = 0;
				step++;
			}
		}
		
		output.append(
				"Acceleration:\n" +
				"x:" + result[0] + "\n" +
				"y:" + result[1] + "\n" +
				"z:" + result[2] + "\n" +
				"|x+y|:" + xyAcceleration + "\n" +
				"mean:" + localMean + "\n"
										);
		
		output.append("\nthreshold:"+ threshold + "\nsteps:"+ step);
	

	}
	
	public void resetSteps(View view) {
		step = 0;
	}
	
}