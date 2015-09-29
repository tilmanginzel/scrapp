package de.medieninf.mobcomp.scrapp.rest;

import android.content.Context;
import android.content.SharedPreferences;

import de.medieninf.mobcomp.scrapp.util.Config;
import retrofit.RequestInterceptor;

/**
 * Interceptor gets called before each Request and adds
 * Identity-Token to Header if necessary.
 */
class IdentityTokenRequestInterceptor implements RequestInterceptor {

    // cache identity token
    private String identityToken;
    private Context context;

    public IdentityTokenRequestInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public void intercept(RequestFacade request) {
        if (identityToken == null) {
            setIdentityToken();
        }

        request.addHeader("Identity-Token", identityToken);
    }

    /**
     * Loads the identity token from shared preferences.
     */
    private void setIdentityToken() {
        SharedPreferences prefs = context.getSharedPreferences(Config.USER_PREFERENCES, 0);
        identityToken = prefs.getString(Config.PREF_KEY_IDENTITY_TOKEN, null);
    }
}
