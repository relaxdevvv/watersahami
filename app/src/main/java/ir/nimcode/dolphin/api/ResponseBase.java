package ir.nimcode.dolphin.api;

import com.google.gson.annotations.SerializedName;

public class ResponseBase<MODEL> {

    @SerializedName("status_code")
    private int statusCode;

    @SerializedName("data")
    private MODEL data;

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setData(MODEL data) {
        this.data = data;
    }

    public MODEL getData() {
        return data;
    }

    @Override
    public String toString() {
        return
                "ResponseBase{" +
                        "status_code = '" + statusCode + '\'' +
                        ",data = '" + data + '\'' +
                        "}";
    }
}