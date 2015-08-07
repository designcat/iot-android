package jp.nvzk.iotprojectandroid.model;

import java.io.Serializable;

/**
 * Created by menteadmin on 2015/08/07.
 */
public class Sensor implements Serializable{
    private byte pressure_thumb;
    private byte pressure_pinky;
    private byte pressure_heel;
    private byte acceleration_x;
    private byte acceleration_y;
    private byte acceleration_z;
    private byte gyro_x;
    private byte gyro_y;
    private byte gyro_z;
    private byte compass_x;
    private byte compass_y;
    private byte compass_z;
    private byte atmospheric_pressure;


    public byte getPressure_thumb() {
        return pressure_thumb;
    }

    public void setPressure_thumb(byte pressure_thumb) {
        this.pressure_thumb = pressure_thumb;
    }

    public byte getPressure_pinky() {
        return pressure_pinky;
    }

    public void setPressure_pinky(byte pressure_pinky) {
        this.pressure_pinky = pressure_pinky;
    }

    public byte getPressure_heel() {
        return pressure_heel;
    }

    public void setPressure_heel(byte pressure_heel) {
        this.pressure_heel = pressure_heel;
    }

    public byte getAcceleration_x() {
        return acceleration_x;
    }

    public void setAcceleration_x(byte acceleration_x) {
        this.acceleration_x = acceleration_x;
    }

    public byte getAcceleration_y() {
        return acceleration_y;
    }

    public void setAcceleration_y(byte acceleration_y) {
        this.acceleration_y = acceleration_y;
    }

    public byte getAcceleration_z() {
        return acceleration_z;
    }

    public void setAcceleration_z(byte acceleration_z) {
        this.acceleration_z = acceleration_z;
    }

    public byte getGyro_x() {
        return gyro_x;
    }

    public void setGyro_x(byte gyro_x) {
        this.gyro_x = gyro_x;
    }

    public byte getGyro_y() {
        return gyro_y;
    }

    public void setGyro_y(byte gyro_y) {
        this.gyro_y = gyro_y;
    }

    public byte getGyro_z() {
        return gyro_z;
    }

    public void setGyro_z(byte gyro_z) {
        this.gyro_z = gyro_z;
    }

    public byte getCompass_x() {
        return compass_x;
    }

    public void setCompass_x(byte compass_x) {
        this.compass_x = compass_x;
    }

    public byte getCompass_y() {
        return compass_y;
    }

    public void setCompass_y(byte compass_y) {
        this.compass_y = compass_y;
    }

    public byte getCompass_z() {
        return compass_z;
    }

    public void setCompass_z(byte compass_z) {
        this.compass_z = compass_z;
    }

    public byte getAtmos_pressure() {
        return atmospheric_pressure;
    }

    public void setAtmos_pressure(byte atmos_pressure) {
        this.atmospheric_pressure = atmos_pressure;
    }
}
