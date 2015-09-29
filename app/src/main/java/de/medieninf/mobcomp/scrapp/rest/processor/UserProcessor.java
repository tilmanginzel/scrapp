package de.medieninf.mobcomp.scrapp.rest.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.Date;

import de.medieninf.mobcomp.scrapp.rest.model.User;
import de.medieninf.mobcomp.scrapp.util.Config;
import retrofit.RetrofitError;

/**
 * This processor is responsible for the user resource.
 * It makes rest calls and required inserts / updates to the local database.
 *
 * Additionally it requests a GCM token to receive future GCM messages.
 */
public class UserProcessor extends RestProcessor {
    private static final String TAG = UserProcessor.class.getSimpleName();

    public UserProcessor(Context context) {
        super(context);
    }

    /**
     * Creates a new user and makes a synchronous rest call.
     * Afterwards the user will be stored in the shared preferences.
     */
    public void createUser() {
        String gcmToken = getGcmToken();

        if (gcmToken != null) {
            User user = new User();
            user.setGcmToken(gcmToken);

            try {
                // make synchronous rest call to get identity token
                user = restClient.getApiService().createUser(user);

                // add user to preferences
                addUserToPreferences(user);
            } catch (RetrofitError e) {
                Log.e(TAG, "RetrofitError: "+ e.getMessage());
            }
        }
    }

    /**
     * Adds a users gcm_token and identity_token to the shared preferences.
     *
     * @param user user object to add
     */
    private void addUserToPreferences(User user) {
        SharedPreferences prefs = context.getSharedPreferences(Config.USER_PREFERENCES, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Config.PREF_KEY_GCM_TOKEN, user.getGcmToken());
        editor.putString(Config.PREF_KEY_IDENTITY_TOKEN, user.getIdentitiyToken());
        editor.putLong(Config.PREF_KEY_REGISTERED_SINCE, new Date().getTime());
        editor.commit();
    }

    /**
     * Get the GCM token from google servers.
     *
     * @return gcm token
     */
    private String getGcmToken() {
        String gcmToken = null;
        try {
            InstanceID instanceID = InstanceID.getInstance(context);
            gcmToken = instanceID.getToken(Config.GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gcmToken;
    }
}
