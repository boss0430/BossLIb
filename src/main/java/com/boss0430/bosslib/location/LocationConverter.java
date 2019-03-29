package com.boss0430.bosslib.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.boss0430.bosslib.utils.Dlog;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Location Coverter.
 * Use with this phase. LocationAsyncTask > LocationTaskInformer > onTaskDone.result
 * @since 2019 Mar 27
 * @author boss0430
 */
public class LocationConverter {

    private Context mContext;
    private Locale mLocale;

    private String separator = "|";

    private final String TAG = "LocationConverter";

    /**
     * Default Constructor. locale will be set to default (KOREA)
     * @param _context
     */
    public LocationConverter(Context _context) {
        this.mContext = _context;
    }

    /**
     * Constructor. input locale your own.
     * @param _context
     * @param _localeValue java.util.Locale.XXXX
     */
    public LocationConverter(Context _context, Locale _localeValue) {
        this.mContext = _context;
        this.mLocale = _localeValue;
    }

    /**
     * call this function when you want to change locale manually.
     * @param _localeValue java.util.Locale.XXXX
     */
    public void setLocale(Locale _localeValue) {
        this.mLocale = _localeValue;
    }

    /**
     * When you don't want to use default separator (PIPE), use this function to change it.
     * @param _separator
     */
    public void changeSeparator(String _separator) {
        this.separator = _separator;
    }

    /**
     * Get Address from result string of LocationAsyncTask. Just pass result as it was.
     * @param _locationAsyncTaskResult
     * @return
     */
    public String getAddressFromResult(String _locationAsyncTaskResult) {

        Dlog.out(TAG, "LOCATION. FN_getAddressFromResult.input : " + _locationAsyncTaskResult, Dlog.d);

        String rtnStr = "";
        String[] separated  = _locationAsyncTaskResult.split("\\" + separator);

        if (separated.length >= 2) {
            Dlog.out(TAG, "LOCATION : Result to Separated coordinates success", Dlog.i);
            Dlog.out(TAG, "LOCATION : separated[0]:" + separated[0] + ",separated[1]:" + separated[1], Dlog.d);

            try {
                double latitude = Double.parseDouble(separated[0]);
                double longitude = Double.parseDouble(separated[1]);

                Dlog.out(TAG, "LOCATION : latitude(double):" + latitude, Dlog.i);
                Dlog.out(TAG, "LOCATION : longitude(double):" + longitude, Dlog.i);

                rtnStr = getAddressFromCoordinates(latitude, longitude);
            } catch (Exception e) {
                Dlog.out(TAG, "LOCATION : Exception has occurred : " + e, Dlog.e);
            }
        } else {
            Dlog.out(TAG, "LOCATION : result separation failed", Dlog.e);
        }
        return rtnStr;
    }

    /**
     * Get Address from coordinates value.
     * @param latitude
     * @param longitude
     * @return
     */
    public String getAddressFromCoordinates(double latitude, double longitude) {

        Dlog.out(TAG, "LOCATION. FN_getAddressFromCoordinates", Dlog.d);

        String rtnAddress = "";

        if (mLocale == null) mLocale = Locale.KOREA;
        Geocoder gc = new Geocoder(mContext, mLocale);

        try {
            List<Address> addressList = gc.getFromLocation(latitude, longitude, 1);

            if (addressList.size() > 0) {
                String countryCode = addressList.get(0).getCountryCode();
                String defaultCountry = Locale.getDefault().getCountry();

                // erase nation info from address when locale is Korea.
                String address = addressList.get(0).getAddressLine(0);
                if (mLocale == Locale.KOREA) {
                    address = address.replace("대한민국", "")
                            .replace("대한 민국", "")
                            .replace("남한", "");
                }

                if (countryCode.equalsIgnoreCase((defaultCountry))) {
                    rtnAddress = address;
                }

            }

        } catch (IOException e) {
            Dlog.out(TAG, "LOCATION : get location failed : " + e, Dlog.e);
        }

        return rtnAddress;
    }

}
