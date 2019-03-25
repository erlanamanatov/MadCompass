package com.erkprog.madcompass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import static android.content.Context.SENSOR_SERVICE;

public class SensorHelper implements SensorEventListener {

  private SensorHelperListener mListener;

  private SensorManager mSensorManager;
  private WindowManager mWindowManager;
  private Sensor mAccelerometerSensor;
  private Sensor mMagneticSensor;

  private static final String TAG = "CompassListener";
  private final float mOrientation[] = new float[3];

  private float[] mGravity = new float[3];
  private float[] mGeomagnetic = new float[3];

  private float[] R1 = new float[9];
  private float[] I1 = new float[9];

  int mScreenRotation;
  Display mDisplay;

  public interface SensorHelperListener {
    void onRotationChanged(float azimuth, float roll, float pitch);

    void onMagneticFieldChanged(float value);
  }

  SensorHelper(Context context, SensorHelperListener listener) {
    mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
    mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    mDisplay = mWindowManager.getDefaultDisplay();
    mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    mListener = listener;
  }

  public void start() {
    mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_GAME);
  }

  public void stop() {
    mSensorManager.unregisterListener(this);
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    final float alpha = 0.97f;
    synchronized (this) {

      if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
        mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
        mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
      }

      if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
        mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0];
        mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1];
        mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2];

        float magneticField = (float) Math.sqrt(mGeomagnetic[0] * mGeomagnetic[0]
            + mGeomagnetic[1] * mGeomagnetic[1]
            + mGeomagnetic[2] * mGeomagnetic[2]);

        if (mListener != null) {
          mListener.onMagneticFieldChanged(magneticField);
        }
      }

      float[] remappedMatrix = new float[9];

      boolean success = SensorManager.getRotationMatrix(R1, I1, mGravity, mGeomagnetic);
      if (success) {
        int x_axis = SensorManager.AXIS_X;
        int y_axis = SensorManager.AXIS_Y;

        mScreenRotation = mDisplay.getRotation();
        switch (mScreenRotation) {
          case (Surface.ROTATION_90):
            x_axis = SensorManager.AXIS_Y;
            y_axis = SensorManager.AXIS_MINUS_X;
            break;
          case (Surface.ROTATION_180):
            y_axis = SensorManager.AXIS_MINUS_Y;
            break;
          case (Surface.ROTATION_270):
            x_axis = SensorManager.AXIS_MINUS_Y;
            y_axis = SensorManager.AXIS_X;
            break;
          default:
            break;
        }

        SensorManager.remapCoordinateSystem(R1,
            x_axis, y_axis,
            remappedMatrix);
        SensorManager.getOrientation(remappedMatrix, mOrientation);
        float azimuth = (float) Math.toDegrees(mOrientation[0]);
        azimuth = (azimuth + 360) % 360;
        float pitch = (float) Math.toDegrees(mOrientation[1]);
        float roll = (float) Math.toDegrees(mOrientation[2]);

        if (mListener != null) {
          mListener.onRotationChanged(azimuth, pitch, roll);
        }
      }
    }
  }


  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  public void removeListener() {
    mListener = null;
  }
}
