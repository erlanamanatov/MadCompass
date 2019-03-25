package com.erkprog.madcompass.model;

public class CompassModel {
  private float magneticField;
  private float azimuth;

  public float getAzimuth() {
    return azimuth;
  }

  public void setAzimuth(float azimuth) {
    this.azimuth = azimuth;
  }

  public float getMagneticField() {
    return magneticField;
  }

  public void setMagneticField(float magneticField) {
    this.magneticField = magneticField;
  }
}
