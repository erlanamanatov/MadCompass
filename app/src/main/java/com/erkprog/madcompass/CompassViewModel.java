package com.erkprog.madcompass;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;

import com.erkprog.madcompass.model.CompassModel;


public class CompassViewModel extends AndroidViewModel implements SensorHelper.SensorHelperListener {

  private SensorHelper mSensorHelper;

  public CompassViewModel(@NonNull Application application) {
    super(application);
  }

  private MutableLiveData<CompassModel> liveData;

  public LiveData<CompassModel> getData() {
    if (liveData == null) {
      liveData = new MutableLiveData<>();
      loadState();
    }
    return liveData;
  }

  private void loadState() {
    Context context = getApplication();
    mSensorHelper = new SensorHelper(context, this);
    mSensorHelper.start();
  }

  @Override
  protected void onCleared() {
    mSensorHelper.stop();
    mSensorHelper.removeListener();
    super.onCleared();
  }

  @Override
  public void onRotationChanged(float azimuth, float roll, float pitch) {
    String str = ((int) azimuth) + "° ";
    CompassModel newState = new CompassModel();
    newState.setOrientStr(str);
    liveData.postValue(newState);
  }

  @Override
  public void onMagneticFieldChanged(float value) {
  }
}