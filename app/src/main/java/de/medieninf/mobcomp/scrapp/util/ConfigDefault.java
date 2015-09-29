package de.medieninf.mobcomp.scrapp.util;

/**
 * Global default configuration file.
 */
class ConfigDefault {
    public static final String SERVER_URL = "http://localhost:5000/api/v1";
    public static final String GCM_SENDER_ID = "gcm_sender_id";

    public static final String USER_PREFERENCES = "user_preferences";
    public static final String PREF_KEY_IDENTITY_TOKEN = "identity_token";
    public static final String PREF_KEY_GCM_TOKEN = "gcm_token";
    public static final String PREF_KEY_REGISTERED_SINCE = "registered_since";

    public static final String NOT_SO_SECRET_KEY = "notSoSecretKey";
    public static final String NOT_SO_SECRET_SALT = "notSoSecretSalt";
    public static final byte[] NOT_SO_SECRET_IV = {-2, 48, 27, -61, 2, -102, 25, -3, -43, 26, -55, 97, 91, -94, 82, -28};

    public static final String BROWSER_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-gb; Nexus One Build/FRF50) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
}
