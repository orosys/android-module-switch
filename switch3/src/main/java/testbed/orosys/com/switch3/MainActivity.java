package testbed.orosys.com.switch3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.orosys.sdk.proximity.sw.SwitchInfo;
import com.orosys.sdk.proximity.sw.util.SwitchHelper;
import com.orosys.sdk.proximity.sw.util.SwitchUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView sdCardPermissionText;
    private TextView positionPermissionText;
    private TextView userInfoAgreePermissionText;
    private TextView commonLayerPermissionText;
    private TextView moduleEnableText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SwitchHelper.launchApp(this);
        SwitchHelper.runSDK(this);

        sdCardPermissionText = (TextView) findViewById(R.id.txt_sd_card_permission);
        positionPermissionText = (TextView) findViewById(R.id.txt_position_permission);
        userInfoAgreePermissionText = (TextView) findViewById(R.id.txt_user_info_agree);
        commonLayerPermissionText = (TextView) findViewById(R.id.txt_common_layer_version);
        moduleEnableText = (TextView) findViewById(R.id.txt_enable);

        updateInfo();

        findViewById(R.id.common_layer_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> versions = new ArrayList<Integer>();
                for (int i = 0; i < 10; i++) {
                    versions.add(i);
                }

                new MaterialDialog.Builder(MainActivity.this)
                        .title("Select Version")
                        .items(versions)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                SwitchHelper.setInfo(MainActivity.this, new SwitchInfo.Builder()
                                        .setCommonLayerVersion(which)
                                        .build());
                                Toast.makeText(MainActivity.this, "Common Layer Version is " + which, Toast.LENGTH_SHORT).show();
                                updateInfo();
                            }
                        })
                        .show();
            }
        });
        findViewById(R.id.module_enable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title("Select Module enable")
                        .positiveText("ENABLE")
                        .negativeText("DISABLE")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                SwitchHelper.setInfo(MainActivity.this, new SwitchInfo.Builder()
                                        .setEnable(1)
                                        .build());
                                Toast.makeText(MainActivity.this, "Module is Enabled", Toast.LENGTH_SHORT).show();
                                updateInfo();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                SwitchHelper.setInfo(MainActivity.this, new SwitchInfo.Builder()
                                        .setEnable(0)
                                        .build());
                                Toast.makeText(MainActivity.this, "Module is Disabled", Toast.LENGTH_SHORT).show();
                                updateInfo();
                            }
                        })
                        .show();
            }
        });
        findViewById(R.id.user_info_agree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title("Select User Info Agree")
                        .positiveText("GRANT")
                        .negativeText("DENIAL")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                SwitchHelper.setInfo(MainActivity.this, new SwitchInfo.Builder()
                                        .setUserInfoAgree(1)
                                        .build());
                                Toast.makeText(MainActivity.this, "User Info Agreen is GRANTED", Toast.LENGTH_SHORT).show();
                                updateInfo();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                SwitchHelper.setInfo(MainActivity.this, new SwitchInfo.Builder()
                                        .setUserInfoAgree(0)
                                        .build());
                                Toast.makeText(MainActivity.this, "User Info Agreen is DENIED", Toast.LENGTH_SHORT).show();
                                updateInfo();
                            }
                        })
                        .show();
            }
        });
        findViewById(R.id.position_permission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPositionPermission();
            }
        });
        findViewById(R.id.sd_card_permission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSDCardPermission();
            }
        });
    }

    private void updateInfo() {
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                SwitchInfo info = SwitchUtil.getCurrentSDKInfo(MainActivity.this);
                sdCardPermissionText.setText(SwitchUtil.checkExternalStoragePermission(MainActivity.this) ? "GRANTED" : "DENIED");
                positionPermissionText.setText(SwitchUtil.checkPositionPermission(MainActivity.this) ? "GRANTED" : "DENIED");
                userInfoAgreePermissionText.setText(info.getUserInfoAgree() == 1 ? "GRANTED" : "DENIED");
                commonLayerPermissionText.setText("" + info.getCommonLayerVersion());
                moduleEnableText.setText("" + info.getSwitchEnable());
                return false;
            }
        }).sendEmptyMessageDelayed(0, 500);
    }

    private void checkPositionPermission() {
        if (!SwitchUtil.checkPositionPermission(this)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
        } else {
            SwitchHelper.setInfo(MainActivity.this, new SwitchInfo.Builder()
                    .setPositionPermission(1)
                    .build());
            Toast.makeText(MainActivity.this, "Position Permission GRANTED", Toast.LENGTH_SHORT).show();
            updateInfo();
        }
    }

    private void checkSDCardPermission() {
        if (!SwitchUtil.checkExternalStoragePermission(this)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
        } else {
            Toast.makeText(MainActivity.this, "SD Card Permission GRANTED", Toast.LENGTH_SHORT).show();
            updateInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Position permission
        if (10 == requestCode) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SwitchHelper.setInfo(MainActivity.this, new SwitchInfo.Builder()
                        .setPositionPermission(1)
                        .build());
                Toast.makeText(MainActivity.this, "Position Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                SwitchHelper.setInfo(MainActivity.this, new SwitchInfo.Builder()
                        .setPositionPermission(0)
                        .build());
                Toast.makeText(MainActivity.this, "Position Permission DENIED", Toast.LENGTH_SHORT).show();
            }
            updateInfo();
        }
        // SD Card permission
        else if (11 == requestCode) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "SD Card Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "SD Card Permission DENIED", Toast.LENGTH_SHORT).show();
            }
            updateInfo();
        }
    }
}
