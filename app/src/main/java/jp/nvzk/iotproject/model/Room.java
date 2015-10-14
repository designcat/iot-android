package jp.nvzk.iotproject.model;

/**
 * Created by user on 15/08/09.
 */
public class Room {
    private String roomName;
    private String userId;
    private String userName;
    private int maxMember;
    private boolean isMax;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String name) {
        this.roomName = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isMax() {
        return isMax;
    }

    public void setIsMax(boolean isMax) {
        this.isMax = isMax;
    }

    public int getMaxMember() {
        return maxMember;
    }

    public void setMaxMember(int maxMember) {
        this.maxMember = maxMember;
    }
}
