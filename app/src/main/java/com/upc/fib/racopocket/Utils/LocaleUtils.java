package com.upc.fib.racopocket.Utils;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleUtils {

    private Context context;

    public LocaleUtils(Context context) {
        this.context = context;
    }

    public void setLocale() {
        String localeCode = PreferencesUtils.recoverStringPreference(this.context, "language");
        if (localeCode.equals(""))
            localeCode = "ca";

        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        this.context.getResources().updateConfiguration(configuration, this.context.getResources().getDisplayMetrics());
    }

    public void setAndStoreLocale(String localeCode) {
        PreferencesUtils.storeStringPreference(this.context, "language", localeCode);
        setLocale();
    }

}
