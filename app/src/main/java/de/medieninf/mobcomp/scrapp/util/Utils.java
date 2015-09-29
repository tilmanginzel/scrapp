package de.medieninf.mobcomp.scrapp.util;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.simbio.encryption.Encryption;

/**
 * Utils class.
 */
public class Utils {

    /**
     * Returns a string describing 'time' as a time relative to 'now'.
     *
     * @param timestamp - timestamp with format: yyyy-MM-dd hh:mm:ss
     * @return string describing 'time' as a time relative to 'now'
     */
    public static String getRelativeTimeSpanString(String timestamp) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
        long milliseconds = 0;
        try {
            milliseconds = f.parse(timestamp).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        CharSequence prettyDate = DateUtils.getRelativeTimeSpanString(milliseconds, new Date().getTime(), DateUtils.MINUTE_IN_MILLIS);
        return prettyDate.toString();
    }

    /**
     * Converts a timestamp string from sqlite to java date object.
     *
     * @param timestamp - timestamp as string
     * @return date
     */
    public static Date convertStringToDate(String timestamp) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
        try {
            return f.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get an Encryption object which is used to encrypt and decrypt strings.
     *
     * @return encryption
     */
    public static Encryption getEncryption() {
        return Encryption.getDefault(Config.NOT_SO_SECRET_KEY, Config.NOT_SO_SECRET_SALT, Config.NOT_SO_SECRET_IV);
    }
}
