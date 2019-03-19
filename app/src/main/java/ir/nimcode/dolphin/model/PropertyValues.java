package ir.nimcode.dolphin.model;


import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by saeed on 2/17/18.
 */

public class PropertyValues implements Serializable {

    @Expose
    private String val;

    @Expose
    private byte chk;

    @Expose(serialize = false)
    private long pid;

    public PropertyValues(String val, boolean chk) {
        this.val = val;
        this.chk = (byte) (chk ? 1 : 0);
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public boolean isChk() {
        return chk == 1;
    }

    public byte getChk() {
        return chk;
    }

    public void setChk(byte chk) {
        this.chk = chk;
    }

    public void setChk(boolean chk) {
        this.chk = (byte) (chk ? 1 : 0);
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }
}
