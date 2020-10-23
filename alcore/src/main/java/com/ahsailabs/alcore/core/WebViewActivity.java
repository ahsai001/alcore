package com.ahsailabs.alcore.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import com.ahsailabs.alcore.R;
import com.ahsailabs.alcore.constants.AlCoreConstanta;
import com.ahsailabs.alcore.fragments.GeneralWebViewFragment;
import com.ahsailabs.alcore.fragments.InfoFragment;
import com.ahsailabs.alutils.CommonUtil;
import com.ahsailabs.alutils.HttpClientUtil;

import java.util.ArrayList;


/**
 * Created by ahmad s on 3/17/2016.
 */
public class WebViewActivity extends BaseActivity {
    private WebViewFragment newFragment;
    private String baseUrl;
    private String url;
    private String title;
    private int bgColor;
    private String defaultMessage;
    private String pageTag;

    public static final String PARAM_BASE_URL = "param_base_url";
    public static final String PARAM_URL = "param_url";
    public static final String PARAM_TITLE = "param_title";
    public static final String PARAM_BG_COLOR = "param_bg_color";
    public static final String PARAM_DEFAULT_MESSAGE = "param_default_message";
    public static final String PARAM_PAGE_TAG = "param_page_tag";
    public static final String PARAM_IS_MEID = InfoFragment.PARAM_IS_MEID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_webview);


        baseUrl = CommonUtil.getStringIntent(getIntent(),PARAM_BASE_URL,null);
        url = CommonUtil.getStringIntent(getIntent(),PARAM_URL,null);
        title = CommonUtil.getStringIntent(getIntent(),PARAM_TITLE,null);
        bgColor = CommonUtil.getIntIntent(getIntent(),PARAM_BG_COLOR,-1);
        defaultMessage = CommonUtil.getStringIntent(getIntent(),PARAM_DEFAULT_MESSAGE,null);
        pageTag = CommonUtil.getStringIntent(getIntent(),PARAM_PAGE_TAG,null);

        Toolbar toolbar =  (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        enableUpNavigation();
        getSupportActionBar().setTitle(title);

        String usedTag = TextUtils.isEmpty(pageTag)? GeneralWebViewFragment.FRAGMENT_TAG:pageTag;

        WebViewFragment oldFragment = (WebViewFragment)getSupportFragmentManager().findFragmentByTag(usedTag);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(oldFragment != null){
            transaction.remove(oldFragment);
        }
        transaction.commit();
        transaction = null;

        transaction = getSupportFragmentManager().beginTransaction();
        newFragment = new WebViewFragment();

        boolean isMeid = CommonUtil.getBooleanIntent(getIntent(), PARAM_IS_MEID, false);
        ArrayList<String> headerList = HttpClientUtil.getHeaderList(isMeid, true, true, true, HttpClientUtil.AuthType.NONE);
        newFragment.setArg(this,1, baseUrl, url, defaultMessage, null, headerList);

        transaction.replace(R.id.webview_main_fragment, newFragment, usedTag);
        transaction.commit();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                if(newFragment != null && newFragment.navigateBack()){
                    return true;
                }
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(newFragment != null && newFragment.onKeyDown(keyCode,event)){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        newFragment = null;
        super.onDestroy();
    }


    public static class WebViewFragment extends GeneralWebViewFragment{

        @Override
        protected View getCustomProgressBar() {
            return null;
        }

        @Override
        protected View getCustomInfoView() {
            return null;
        }

        @Override
        protected int getCustomInfoTextView() {
            return 0;
        }


        @Override
        public void setupWebview(WebView webView) {
            super.setupWebview(webView);
            webView.addJavascriptInterface(new WebViewFragment.WebAppInterface(this.getActivity()), getString(R.string.app_name).replace(" ","").toLowerCase());
        }

        @Override
        protected boolean isPreserveHistoryInsteadOfUsingHeaderWhenFollowLink() {
            return false;
        }

        @Override
        protected boolean handleCustomInternalLink(WebView view, WebResourceRequest request) {
            return false;
        }

        @Override
        protected boolean handleCustomInternalLink(WebView view, String url) {
            return false;
        }

        @Override
        protected boolean handleCustomExternalLink(WebView view, WebResourceRequest request) {
            return handleCustomLink(view.getContext(), request.getUrl().toString());
        }

        @Override
        protected boolean handleCustomExternalLink(WebView view, String url) {
            return handleCustomLink(view.getContext(), url);
        }

        @Override
        protected boolean onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            return false;
        }


        private boolean handleCustomLink(Context context, String url){
            if(url.startsWith(AlCoreConstanta.TELEPHONE_SCHEMA)){
                String number = url.replace(AlCoreConstanta.TELEPHONE_SCHEMA, "");
                return CommonUtil.callNumber(context, number);
            } else if(url.startsWith(AlCoreConstanta.MAILTO_SCHEMA)){
                MailTo mailTo = MailTo.parse(url);
                CommonUtil.sendEmail(context, "send email", mailTo.getSubject(), mailTo.getBody(), new String[]{mailTo.getTo()}, new String[]{mailTo.getCc()});
            }
            return false;
        }

        private class WebAppInterface {
            Activity activity;

            /** Instantiate the interface and set the context */
            WebAppInterface(Activity c) {
                activity = c;
            }

            /** Show a toast from the web page */
            @JavascriptInterface
            public void showToast(String toast) {
                CommonUtil.showToast(activity,toast);
            }


            @JavascriptInterface
            public void showInfo(String title, String info) {
                CommonUtil.showInfo(activity,title,info);
            }

            @JavascriptInterface
            public void webDescription(String desc) {
                //Toast.makeText(activity.getBaseContext(), desc, Toast.LENGTH_SHORT).show();
            }

            @JavascriptInterface
            public void showActionBar(String title) {
                if(!TextUtils.isEmpty(title)){
                    ((BaseActivity)activity).getSupportActionBar().setTitle(title);
                }
                ((BaseActivity)activity).getSupportActionBar().show();
            }

            @JavascriptInterface
            public void reload(){
            }



        }

    }

    public static void start(Context context, String urlOrHtmlContent, String title, String defaultMessage, int bgColor,String pageTag, boolean isMeid){
        start(context, "", urlOrHtmlContent, title, defaultMessage, bgColor, pageTag, isMeid);
    }

    public static void start(Context context, String baseUrl, String urlOrHtmlContent, String title, String defaultMessage, int bgColor,String pageTag, boolean isMeid){
        Intent webviewIntent = new Intent(context, WebViewActivity.class);
        webviewIntent.putExtra(PARAM_BASE_URL, baseUrl);
        webviewIntent.putExtra(PARAM_URL, urlOrHtmlContent);
        webviewIntent.putExtra(PARAM_TITLE, title);
        webviewIntent.putExtra(PARAM_BG_COLOR, bgColor);
        webviewIntent.putExtra(PARAM_DEFAULT_MESSAGE, defaultMessage);
        webviewIntent.putExtra(PARAM_PAGE_TAG, pageTag);
        webviewIntent.putExtra(PARAM_IS_MEID, isMeid);
        context.startActivity(webviewIntent);
    }
}
