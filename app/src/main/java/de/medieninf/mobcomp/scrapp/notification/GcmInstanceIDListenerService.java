package de.medieninf.mobcomp.scrapp.notification;

import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Service to listens for com.google.android.gms.iid.InstanceID intent when the gcm token changes.
 */
public class GcmInstanceIDListenerService extends InstanceIDListenerService {
    private static final String TAG = GcmInstanceIDListenerService.class.getSimpleName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        Log.i(TAG, "Refresh GCM token. Ignored...");
        // TODO: Fetch updated Instance ID token and notify our app's server of any changes.

        // TODO: Bug: it gets called sometimes randomly (often on first app start).
        // Maybe ask an stackoverflow
    }
}
