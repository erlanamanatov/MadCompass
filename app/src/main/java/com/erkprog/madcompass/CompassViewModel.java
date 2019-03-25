package com.erkprog.madcompass;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import static android.content.Context.SENSOR_SERVICE;

public class CompassViewModel extends AndroidViewModel implements SensorEventListener {
  private SensorManager mSensorManager;
  private Sensor mAccelerometerSensor;
  private Sensor mMagneticSensor;

  private static final String TAG = "CompassListener";
  private final float mOrientation[] = new float[3];

  private float[] mGravity = new float[3];
  private float[] mGeomagnetic = new float[3];

  private int mIntervalTime = 0;
  private long mLastTime = 0;

  private float[] R1 = new float[9];
  private float[] I1 = new float[9];

  public CompassViewModel(@NonNull Application application) {
    super(application);
  }

  private MutableLiveData<CompassModel> orientState;

  public LiveData<CompassModel> getOrientState() {
    if (orientState == null) {
      orientState = new MutableLiveData<>();
      loadState();
    }
    return orientState;
  }

  private void loadState() {
    Context context = getApplication();
    mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
    mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_GAME);
  }

  @Override
  protected void onCleared() {
    mSensorManager.unregisterListener(this);
    super.onCleared();
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    final float alpha = 0.97f;
    long time = System.currentTimeMillis();
    if (time - mLastTime > mIntervalTime) {
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

          onMagneticFieldChanged(magneticField);
        }


        boolean success = SensorManager.getRotationMatrix(R1, I1, mGravity, mGeomagnetic);
        if (success) {
          SensorManager.getOrientation(R1, mOrientation);
          float azimuth = (float) Math.toDegrees(mOrientation[0]);
          azimuth = (azimuth + 360) % 360;
          float pitch = (float) Math.toDegrees(mOrientation[1]);
          float roll = (float) Math.toDegrees(mOrientation[2]);

          onRotationChanged(azimuth, pitch, roll);
        }
      }
      mLastTime = time;
    }

  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  private void onRotationChanged(float azimuth, float roll, float pitch) {
    String str = ((int) azimuth) + "° ";
    CompassModel newState = new CompassModel();
    newState.setOrientStr(str);

    orientState.postValue(newState);
  }

  private void onMagneticFieldChanged(float value) {
  }
}
