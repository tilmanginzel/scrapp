package de.medieninf.mobcomp.scrapp.rest.model;

import com.google.gson.annotations.SerializedName;

/**
 * POJO for User.
 */
public class User {
    @SerializedName("identity_token")
    private String identitiyToken;

    @SerializedName("gcm_token")
    private String gcmToken;

    public String getIdentitiyToken() {
        return identitiyToken;
    }

    public void setIdentitiyToken(String identitiyToken) {
        this.identitiyToken = identitiyToken;
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }
}
