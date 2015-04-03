package drink;

import android.annotation.SuppressLint;
import com.lastone.lastoneGen.R;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

import lastone.LastOneRenderer;

import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

/**
 * Created by richykapadia on 3/29/15.
 */
public class DrinkActivity extends Activity implements SensorEventListener {

    private GLSurfaceView glSurfaceView;
    LastOneRenderer renderer;

    private int particleSize = 4;
    private int particleCount = 5000;
    private int particleLifetime = 0;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    static{
        System.loadLibrary("liquidfun");
        System.loadLibrary("liquidfun_jni");
    }

    @Override @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        renderer = new LastOneRenderer(this,particleCount,particleSize,1000, particleLifetime);
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(renderer);
        setContentView(glSurfaceView);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SENSOR_DELAY_NORMAL);

        //touch for debugging dimensions





    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float axisX = event.values[0];
            float axisY = event.values[1];
            renderer.handleRotation(axisX, axisY);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public boolean onTouchEvent(MotionEvent e){
        float x = e.getX();
        float y = e.getY();

//        System.out.println("Touch: (" + x + ", " + y + ")");

        return true;

    }
}
