package ir.nimcode.dolphin.model;

import java.io.Serializable;

/**
 * Created by saeed on 5/9/18.
 */

public class User implements Serializable {

    public String token;
    public String fullname;
    public String username;
    public Supervisor supervisor;
    public String mobile;
    public String email;
    public String play_id;


}


