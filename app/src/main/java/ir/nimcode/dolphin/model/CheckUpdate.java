package ir.nimcode.dolphin.model;

import com.google.gson.annotations.SerializedName;

public class CheckUpdate {

    @SerializedName("new_version_name")
    private String newVersionName ;

    @SerializedName("download_link")
    private String downloadLink ;

    @SerializedName("new_features")
    private String newFeatures ;

    public String getNewVersionName() {
        return newVersionName;
    }

    public void setNewVersionName(String newVersionName) {
        this.newVersionName = newVersionName;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getNewFeatures() {
        return newFeatures;
    }

    public void setNewFeatures(String newFeatures) {
        this.newFeatures = newFeatures;
    }

    @Override
    public String toString() {
        return
                "CheckUpdate{" +
                        "new_version_name = '" + newVersionName + '\'' +
                        ",download_link = '" + downloadLink + '\'' +
                        ",new_features = '" + newFeatures + '\'' +
                        "}";
    }
}
