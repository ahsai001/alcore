package com.ahsailabs.alcore.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.ahsailabs.alcore.R;
import com.ahsailabs.alcore.api.APIConstant;
import com.ahsailabs.alcore.api.APIResponse;
import com.ahsailabs.alcore.core.BaseActivity;
import com.ahsailabs.alcore.models.CheckVersionModel;
import com.ahsailabs.alutils.CommonUtil;
import com.ahsailabs.alutils.HttpClientUtil;
import com.ahsailabs.alutils.PermissionUtil;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;

import org.json.JSONObject;


/**
 * Created by ahmad s on 8/31/2015.
 */

public abstract class BaseSplashActivity extends BaseActivity {
    public static final String PARAM_CHECK_VERSION_URL = "param_check_version_url";
    private RelativeLayout backgroundPane;
    private TextView titleTextView;
    private ImageView iconView;
    private TextView bottomTextView;
    private String checkVersionUrl;
    private boolean isContinueNextPage = false;
    private PermissionUtil permissionUtil;

    public static void showSplashScreen(Context context, String checkVersionUrl, Class splashClass){
        Intent splashIntent = new Intent(context, splashClass);
        //splashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //splashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        splashIntent.putExtra(PARAM_CHECK_VERSION_URL, checkVersionUrl);
        context.startActivity(splashIntent);
    }

    protected abstract String getCheckVersionUrl();

    protected abstract boolean doNextAction();

    protected abstract boolean isMeidIncluded();

    protected abstract HttpClientUtil.AuthType getAuthType();

    protected abstract int getMinimumSplashTimeInMS();


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        checkVersionUrl = CommonUtil.getStringIntent(getIntent(),PARAM_CHECK_VERSION_URL,null);
        if(TextUtils.isEmpty(checkVersionUrl)) {
            checkVersionUrl = getCheckVersionUrl();
        }

        setContentView(R.layout.activity_splash_screen);

        backgroundPane = findViewById(R.id.splashscreen_background);
        iconView = findViewById(R.id.splashscreen_icon);
        titleTextView = findViewById(R.id.splashscreen_title);
        bottomTextView = findViewById(R.id.splashscreen_bottom_text);

        if(!TextUtils.isEmpty(checkVersionUrl)) {
            if(isMeidIncluded()) {
                permissionUtil = PermissionUtil.checkPermissionAndGo(this, 1041, new Runnable() {
                    @Override
                    public void run() {
                        pushyMeInit();
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        CommonUtil.showToast(BaseSplashActivity.this, "Please give permission to run this application");
                        finish();
                    }
                }, Manifest.permission.READ_PHONE_STATE);
            } else {
                pushyMeInit();
            }
        } else {
            pushyMeInit();
        }
    }
    CheckVersionModel checkVersionModel = null;

    private void doLogicWithUpdater(){
        if(checkVersionModel == null){
            readyDoNextAction();
        } else {
            if (checkVersionModel.getStatus() == APIResponse.GENERIC_RESPONSE.OK) {
                readyDoNextAction();
            } else if (checkVersionModel.getStatus() == APIResponse.GENERIC_RESPONSE.NEED_UPDATE) {
                CommonUtil.showDialog3Option(BaseSplashActivity.this,
                        checkVersionModel.getTitle(),
                        checkVersionModel.getMessage(),
                        getString(R.string.zlcore_download_option_dialog_init), new Runnable() {
                            @Override
                            public void run() {
                                CommonUtil.openBrowser(BaseSplashActivity.this, checkVersionModel.getDetail());
                                finish();
                            }
                        }, getString(R.string.zlcore_close_option_dialog_init), new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, getString(R.string.zlcore_use_existing_option_dialog_init), new Runnable() {
                            @Override
                            public void run() {
                                readyDoNextAction();
                            }
                        });
            } else if (checkVersionModel.getStatus() == APIResponse.GENERIC_RESPONSE.NEED_SHOW_MESSAGE) {
                CommonUtil.showInfo(BaseSplashActivity.this,
                        checkVersionModel.getTitle(), checkVersionModel.getMessage(),
                        new Runnable() {
                            @Override
                            public void run() {
                                readyDoNextAction();
                            }
                        });
            } else {
                readyDoNextAction();
            }
        }
    }

    public void pushyMeInit(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isContinueNextPage) {
                    doLogicWithUpdater();
                } else {
                    isContinueNextPage = true;
                }
            }
        }, getMinimumSplashTimeInMS());

        if(!TextUtils.isEmpty(checkVersionUrl)) {
            AndroidNetworking.post(checkVersionUrl)
                    .setOkHttpClient(HttpClientUtil.getHTTPClient(BaseSplashActivity.this, APIConstant.API_VERSION, getAuthType(), isMeidIncluded()))
                    .addBodyParameter("appid", APIConstant.API_APPID)
                    .setPriority(Priority.HIGH)
                    .setTag("checkversion")
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Gson gson = new Gson();
                            checkVersionModel = gson.fromJson(response.toString(), CheckVersionModel.class);

                            if(isContinueNextPage){
                                doLogicWithUpdater();
                            } else {
                                isContinueNextPage = true;
                            }
                        }

                        @Override
                        public void onError(ANError error) {
                            if(isContinueNextPage){
                                doLogicWithUpdater();
                            } else {
                                isContinueNextPage = true;
                            }
                        }
                    });
        } else {
            isContinueNextPage = true;
        }

    }

    private void readyDoNextAction(){
        if(doNextAction()) {
            finish();
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        AndroidNetworking.cancel("checkversion");
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(permissionUtil != null) {
            permissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected void setBackgroundPaneImage(int resid) {
        backgroundPane.setBackgroundResource(resid);
    }

    protected void setBackgroundPaneColor(int resIdColor) {
        backgroundPane.setBackgroundColor(ContextCompat.getColor(this, resIdColor));
    }

    protected void setImageIcon(int resid) {
        iconView.setVisibility(View.VISIBLE);
        iconView.setImageResource(resid);
    }

    protected void setTitleTextView(String title, int resIdColor) {
        titleTextView.setVisibility(View.VISIBLE);
        titleTextView.setText(title);
        titleTextView.setTextColor(ContextCompat.getColor(this, resIdColor));
    }

    protected void setBottomTextView(String bottomText, int resIdColor) {
        bottomTextView.setVisibility(View.VISIBLE);
        bottomTextView.setText(bottomText);
        bottomTextView.setTextColor(ContextCompat.getColor(this, resIdColor));
    }



}
