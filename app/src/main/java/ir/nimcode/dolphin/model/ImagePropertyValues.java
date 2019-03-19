package ir.nimcode.dolphin.model;

import java.io.Serializable;

/**
 * Created by saeed on 5/9/18.
 */

public class ImagePropertyValues implements Serializable {

    public boolean isUpload = false;
    public String localLink;
    public String serverLink;
    public long distanceFromSource = -1L;
    public double latitude;
    public double longitude;
}
