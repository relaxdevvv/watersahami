package ir.nimcode.dolphin.model;

import java.io.Serializable;

/**
 * Created by saeed on 5/9/18.
 */

public class DeviceInfo implements Serializable {

    public String device_os;
    public String device_version;
    public String device_model;
    public String device_id;

    public DeviceInfo(String device_os, String device_version, String device_model, String device_id) {
        this.device_os = device_os;
        this.device_version = device_version;
        this.device_model = device_model;
        this.device_id = device_id;
    }
}
