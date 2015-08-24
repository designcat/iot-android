package jp.nvzk.iotproject.model;

import java.io.Serializable;

/**
 * Created by menteadmin on 2015/08/07.
 */
public class Sensor implements Serializable{
    private int side;
    private int pressure_thumb;
    private int pressure_pinkie;
    private int pressure_heel;
    private int acceleration_x;
    private int acceleration_y;
    private int acceleration_z;
    private int gyro_x;
    private int gyro_y;
    private int gyro_z;
    private int compass_x;
    private int compass_y;
    private int compass_z;
    private int atmospheric_pressure;

    public void setSensor(byte[] bytes){
        setSide(bytes[0] & 0xff);
        setPressure_thumb(bytes[1] & 0xff);
        setPressure_pinkie(bytes[2] & 0xff);
        setPressure_heel(bytes[3] & 0xff);
        setAcceleration_x(bytes[4] & 0xff);
        setAcceleration_y(bytes[5] & 0xff);
        setAcceleration_z(bytes[6] & 0xff);
        setGyro_x(bytes[7] & 0xff);
        setGyro_y(bytes[8] & 0xff);
        setGyro_z(bytes[9] & 0xff);
        setCompass_x(bytes[10] & 0xff);
        setCompass_y(bytes[11] & 0xff);
        setCompass_z(bytes[12] & 0xff);
        setAtmos_pressure(bytes[13] & 0xff);
    }

    public Sensor getNewSensor(byte[] bytes){
        Sensor sensor = new Sensor();
        sensor.setSide(bytes[0] & 0xff);
        sensor.setPressure_thumb(bytes[1] & 0xff);
        sensor.setPressure_pinkie(bytes[2] & 0xff);
        sensor.setPressure_heel(bytes[3] & 0xff);
        sensor.setAcceleration_x(bytes[4] & 0xff);
        sensor.setAcceleration_y(bytes[5] & 0xff);
        sensor.setAcceleration_z(bytes[6] & 0xff);
        sensor.setGyro_x(bytes[7] & 0xff);
        sensor.setGyro_y(bytes[8] & 0xff);
        sensor.setGyro_z(bytes[9] & 0xff);
        sensor.setCompass_x(bytes[10] & 0xff);
        sensor.setCompass_y(bytes[11] & 0xff);
        sensor.setCompass_z(bytes[12] & 0xff);
        sensor.setAtmos_pressure(bytes[13] & 0xff);
        return sensor;
    }

    public int getPressure_thumb() {
        return pressure_thumb;
    }

    public void setPressure_thumb(int pressure_thumb) {
        this.pressure_thumb = pressure_thumb;
    }

    public int getPressure_pinkie() {
        return pressure_pinkie;
    }

    public void setPressure_pinkie(int pressure_pinkie) {
        this.pressure_pinkie = pressure_pinkie;
    }

    public int getPressure_heel() {
        return pressure_heel;
    }

    public void setPressure_heel(int pressure_heel) {
        this.pressure_heel = pressure_heel;
    }

    public int getAcceleration_x() {
        return acceleration_x;
    }

    public void setAcceleration_x(int acceleration_x) {
        this.acceleration_x = acceleration_x;
    }

    public int getAcceleration_y() {
        return acceleration_y;
    }

    public void setAcceleration_y(int acceleration_y) {
        this.acceleration_y = acceleration_y;
    }

    public int getAcceleration_z() {
        return acceleration_z;
    }

    public void setAcceleration_z(int acceleration_z) {
        this.acceleration_z = acceleration_z;
    }

    public int getGyro_x() {
        return gyro_x;
    }

    public void setGyro_x(int gyro_x) {
        this.gyro_x = gyro_x;
    }

    public int getGyro_y() {
        return gyro_y;
    }

    public void setGyro_y(int gyro_y) {
        this.gyro_y = gyro_y;
    }

    public int getGyro_z() {
        return gyro_z;
    }

    public void setGyro_z(int gyro_z) {
        this.gyro_z = gyro_z;
    }

    public int getCompass_x() {
        return compass_x;
    }

    public void setCompass_x(int compass_x) {
        this.compass_x = compass_x;
    }

    public int getCompass_y() {
        return compass_y;
    }

    public void setCompass_y(int compass_y) {
        this.compass_y = compass_y;
    }

    public int getCompass_z() {
        return compass_z;
    }

    public void setCompass_z(int compass_z) {
        this.compass_z = compass_z;
    }

    public int getAtmos_pressure() {
        return atmospheric_pressure;
    }

    public void setAtmos_pressure(int atmos_pressure) {
        this.atmospheric_pressure = atmos_pressure;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }
}
