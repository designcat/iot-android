package jp.nvzk.iotproject.model;

/**
 * Created by yurina on 2015/08/22.
 */
public class MyData {
    private String userId;
    private String name;
    private int roomId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
