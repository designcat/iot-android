package jp.nvzk.iotproject.model;

/**
 * Created by menteadmin on 2015/10/15.
 */
public class ResponseData {
    private int statusCode;
    private String message;
    private String details;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
