package testbed.orosys.com.switch2;

import android.util.Log;

/**
 * Created by oro on 2017. 2. 17..
 */

public class Application extends android.app.Application {
    private static final String TAG = Application.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
    }
}
