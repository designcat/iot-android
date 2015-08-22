package jp.nvzk.iotprojectandroid.model;

import java.io.Serializable;

/**
 * Created by menteadmin on 2015/08/07.
 */
public class Sensor implements Serializable{
    private byte side;
    private byte pressure_thumb;
    private byte pressure_pinkie;
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

    public void setSensor(byte[] bytes){
        setSide(bytes[0]);
        setPressure_thumb(bytes[1]);
        setPressure_pinkie(bytes[2]);
        setPressure_heel(bytes[3]);
        setAcceleration_x(bytes[4]);
        setAcceleration_y(bytes[5]);
        setAcceleration_z(bytes[6]);
        setGyro_x(bytes[7]);
        setGyro_y(bytes[8]);
        setGyro_z(bytes[9]);
        setCompass_x(bytes[10]);
        setCompass_y(bytes[11]);
        setCompass_z(bytes[12]);
        setAtmos_pressure(bytes[13]);
    }


    public byte getPressure_thumb() {
        return pressure_thumb;
    }

    public void setPressure_thumb(byte pressure_thumb) {
        this.pressure_thumb = pressure_thumb;
    }

    public byte getPressure_pinkie() {
        return pressure_pinkie;
    }

    public void setPressure_pinkie(byte pressure_pinkie) {
        this.pressure_pinkie = pressure_pinkie;
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

    public byte getSide() {
        return side;
    }

    public void setSide(byte side) {
        this.side = side;
    }
}
