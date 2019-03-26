package com.erkprog.madcompass;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.erkprog.madcompass.model.CompassModel;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
  TextView tvAzimut;
  ImageView img;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initViews();

    CompassViewModel model = ViewModelProviders.of(this).get(CompassViewModel.class);
    model.getData().observe(this, new Observer<CompassModel>() {
      @Override
      public void onChanged(@Nullable CompassModel model) {
        if (model != null) {
          float azimuth = model.getAzimuth();
          rotateImg(azimuth);
          showAzimuth(azimuth);
        }
      }
    });
  }

  private void initViews() {
    tvAzimut = findViewById(R.id.testtv);
    img = findViewById(R.id.comp_face);
  }

  private void showAzimuth(float azimuth) {
    String azimuthStr = String.format(Locale.UK, "%d °", Math.round(azimuth));
    tvAzimut.setText(azimuthStr);
  }

  private void rotateImg(float azimuth) {
    img.setPivotX((float) img.getWidth() / 2);
    img.setPivotY((float) img.getHeight() / 2);
    img.setRotation(-azimuth);
  }
}


