package ir.nimcode.dolphin.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Pagination<MODEL> {

    @SerializedName("first_page_url")
    private String firstPageUrl;

    @SerializedName("path")
    private String path;

    @SerializedName("per_page")
    private int perPage;

    @SerializedName("total")
    private int total;

    @SerializedName("data")
    private List<MODEL> data;

    @SerializedName("last_page")
    private int lastPage;

    @SerializedName("last_page_url")
    private String lastPageUrl;

    @SerializedName("next_page_url")
    private String nextPageUrl;

    @SerializedName("from")
    private int from;

    @SerializedName("to")
    private int to;

    @SerializedName("prev_page_url")
    private String prevPageUrl;

    @SerializedName("current_page")
    private int currentPage;

    public void setFirstPageUrl(String firstPageUrl) {
        this.firstPageUrl = firstPageUrl;
    }

    public String getFirstPageUrl() {
        return firstPageUrl;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }

    public int getPerPage() {
        return perPage;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    public void setData(List<MODEL> data) {
        this.data = data;
    }

    public List<MODEL> getData() {
        return data;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void setLastPageUrl(String lastPageUrl) {
        this.lastPageUrl = lastPageUrl;
    }

    public String getLastPageUrl() {
        return lastPageUrl;
    }

    public void setNextPageUrl(String nextPageUrl) {
        this.nextPageUrl = nextPageUrl;
    }

    public String getNextPageUrl() {
        return nextPageUrl;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getFrom() {
        return from;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public int getTo() {
        return to;
    }

    public void setPrevPageUrl(String prevPageUrl) {
        this.prevPageUrl = prevPageUrl;
    }

    public String getPrevPageUrl() {
        return prevPageUrl;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public String toString() {
        return
                "Pagination{" +
                        "first_page_url = '" + firstPageUrl + '\'' +
                        ",path = '" + path + '\'' +
                        ",per_page = '" + perPage + '\'' +
                        ",total = '" + total + '\'' +
                        ",data = '" + data + '\'' +
                        ",last_page = '" + lastPage + '\'' +
                        ",last_page_url = '" + lastPageUrl + '\'' +
                        ",next_page_url = '" + nextPageUrl + '\'' +
                        ",from = '" + from + '\'' +
                        ",to = '" + to + '\'' +
                        ",prev_page_url = '" + prevPageUrl + '\'' +
                        ",current_page = '" + currentPage + '\'' +
                        "}";
    }
}
