package com.ahsailabs.alutils;

import android.content.Context;
import android.net.http.SslError;
import android.os.Build;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.util.Base64;
import android.webkit.SslErrorHandler;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ahmad s on 10/7/2015.
 * Edited by ahmad s on 12/2/2016.
 */


public class HttpClientUtil {
    public static int DATA_DEFAULT_CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    public static int DATA_DEFAULT_READ_TIMEOUT_MILLIS = 30 * 1000; // 30s
    public static int DATA_DEFAULT_WRITE_TIMEOUT_MILLIS = 30 * 1000; // 30s

    public static int IMAGE_DEFAULT_CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    public static int IMAGE_DEFAULT_READ_TIMEOUT_MILLIS = 30 * 1000; // 30s
    public static int IMAGE_DEFAULT_WRITE_TIMEOUT_MILLIS = 30 * 1000; // 30s

    public static int UPLOAD_DEFAULT_CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    public static int UPLOAD_DEFAULT_READ_TIMEOUT_MILLIS = 60 * 1000; // 60s
    public static int UPLOAD_DEFAULT_WRITE_TIMEOUT_MILLIS = 60 * 1000; // 60s

    private static String androidId = null;
    private static String randomUUID = null;

    private static volatile OkHttpClient singletonClient = null;
    private static volatile OkHttpClient singletonUploadClient = null;
    private static volatile OkHttpClient singletonUnsafeClient = null;
    private static volatile OkHttpClient singletonUploadUnsafeClient = null;
    private static volatile BuilderConfig singletonBuilderConfig = null;

    public enum AuthType {
        APIKEY,
        BASIC,
        BEARER,
        NONE

    }

    public static ArrayList<String> getHeaderList(boolean isMeid, boolean isAndroidID, boolean isRandomUUID, boolean isUserAgent, AuthType authType){
        ArrayList<String> headerList = new ArrayList<>();

        if(authType != null) {
            switch (authType) {
                case APIKEY:
                    headerList.add("x-apikey");
                    break;
                case BASIC:
                    headerList.add("x-basic");
                    break;
                case BEARER:
                    headerList.add("x-bearer");
                    break;
                default:
                    break;
            }
        }

        headerList.add("x-screensize");
        headerList.add("x-model");
        if(isMeid) {
            headerList.add("x-meid");
        }
        headerList.add("x-packagename");
        headerList.add("x-versionname");
        headerList.add("x-versioncode");
        headerList.add("x-lang");
        headerList.add("x-platform");
        headerList.add("x-os");
        headerList.add("x-token");
        if(isAndroidID) {
            headerList.add("x-androidid");
        }
        if(isRandomUUID) {
            headerList.add("x-randomuuid");
        }
        if(isUserAgent) {
            headerList.add("x-useragent");
            headerList.add("User-Agent");
        }
        return headerList;
    }

    public static HashMap<String, String> getHeaderMap(Context context, List<String> headerList){
        HashMap<String, String> headerMap = new HashMap<>();
        String userAgent = "";
        if(headerList != null){
            for (String header : headerList){
                switch (header){
                    case "x-basic" :
                        headerMap.put("Authorization", HttpClientUtil.getAuthBasic());
                        break;
                    case "x-bearer" :
                        headerMap.put("Authorization", HttpClientUtil.getBearerToken());
                        break;
                    case "x-apikey" :
                        headerMap.put("Authorization", HttpClientUtil.getAuthAPIKey());
                        break;
                    case "x-screensize":
                        headerMap.put("x-screensize", CommonUtil.getDisplayMetricsDensityDPIInString(context));
                        break;
                    case "x-model":
                        headerMap.put("x-model", getModelNumberInUrlEncode());
                        break;
                    case "x-meid":
                        headerMap.put("x-meid", CommonUtil.getMeid(context));
                        break;
                    case "x-packagename":
                        headerMap.put("x-packagename", context.getPackageName());
                        break;
                    case "x-versionname":
                        headerMap.put("x-versionname", CommonUtil.getVersionName(context));
                        break;
                    case "x-versioncode":
                        headerMap.put("x-versioncode", ""+ CommonUtil.getVersionCode(context)+"");
                        break;
                    case "x-lang":
                        headerMap.put("x-lang", CommonUtil.getCurrentDeviceLanguage(context));
                        break;
                    case "x-platform":
                        headerMap.put("x-platform", "android");
                        break;
                    case "x-os":
                        String osVersion = "";
                        try {
                            osVersion = CommonUtil.urlEncode(Build.VERSION.RELEASE);
                        } catch (UnsupportedEncodingException e) {
                            ////e.printStackTrace();
                        }
                        headerMap.put("x-os", osVersion);
                        break;
                    case "x-token":
                        headerMap.put("x-token", PrefsData.getToken());
                        break;
                    case "x-androidid":
                        if(TextUtils.isEmpty(androidId)){
                            androidId = CommonUtil.getAndroidID(context);
                        }
                        headerMap.put("x-androidid", androidId);
                        break;
                    case "x-randomuuid":
                        if(TextUtils.isEmpty(randomUUID)){
                            randomUUID = CommonUtil.getRandomUUID(context);
                        }
                        headerMap.put("x-randomuuid", randomUUID);
                        break;
                    case "x-useragent":
                        try {
                            userAgent = CommonUtil.urlEncode(System.getProperty("http.agent"));
                        } catch (UnsupportedEncodingException e) {
                            ////e.printStackTrace();
                        }
                        headerMap.put("x-useragent", userAgent);
                        break;
                    case "User-Agent":
                        try {
                            userAgent = CommonUtil.urlEncode(System.getProperty("http.agent"));
                        } catch (UnsupportedEncodingException e) {
                            ////e.printStackTrace();
                        }
                        headerMap.put("User-Agent", userAgent);
                        break;

                }
            }
        }
        return headerMap;
    }

    private static String getBearerToken() {
        String credentials = "Bearer "+ PrefsData.getToken();
        String result="";
        result = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        return result;
    }


    public static void setSingletonBuilderConfig(BuilderConfig builderConfig){
        if (builderConfig == null) {
            throw new IllegalArgumentException("CUstomOkHttpBuilder must not be null.");
        }
        synchronized (HttpClientUtil.class) {
            if (singletonBuilderConfig != null) {
                throw new IllegalStateException("Singleton instance already exists.");
            }
            singletonBuilderConfig = builderConfig;
        }
    }

    public static void setSingletonClient(OkHttpClient okHttpClient){
        if (okHttpClient == null) {
            throw new IllegalArgumentException("OkHttpClient must not be null.");
        }
        synchronized (HttpClientUtil.class) {
            if (singletonClient != null) {
                throw new IllegalStateException("Singleton instance already exists.");
            }
            singletonClient = okHttpClient;
        }
    }

    public static void setSingletonUploadClient(OkHttpClient okHttpClient){
        if (okHttpClient == null) {
            throw new IllegalArgumentException("OkHttpClient must not be null.");
        }
        synchronized (HttpClientUtil.class) {
            if (singletonUploadClient != null) {
                throw new IllegalStateException("Singleton instance already exists.");
            }
            singletonUploadClient = okHttpClient;
        }
    }

    public static void setSingletonUnsafeClient(OkHttpClient unsafeOkHttpClient){
        if (unsafeOkHttpClient == null) {
            throw new IllegalArgumentException("OkHttpClient must not be null.");
        }
        synchronized (HttpClientUtil.class) {
            if (singletonUnsafeClient != null) {
                throw new IllegalStateException("Singleton instance already exists.");
            }
            singletonUnsafeClient = unsafeOkHttpClient;
        }
    }

    public static void setSingletonUploadUnsafeClient(OkHttpClient unsafeOkHttpClient){
        if (unsafeOkHttpClient == null) {
            throw new IllegalArgumentException("OkHttpClient must not be null.");
        }
        synchronized (HttpClientUtil.class) {
            if (singletonUploadUnsafeClient != null) {
                throw new IllegalStateException("Singleton instance already exists.");
            }
            singletonUploadUnsafeClient = unsafeOkHttpClient;
        }
    }

    public static OkHttpClient getHTTPClient(final Context context, String apiVersion){
        return getHTTPClient(context,apiVersion, AuthType.BEARER);
    }

    public static OkHttpClient getHTTPClient(final Context context, String apiVersion, AuthType authType){
        return getHTTPClient(context,apiVersion,authType, false);
    }

    public static OkHttpClient getHTTPClient(final Context context, String apiVersion, AuthType authType, boolean isMeid){
        return getHTTPClient(context,apiVersion,authType, isMeid, false);
    }

    public static OkHttpClient getHTTPClient(final Context context, String apiVersion, AuthType authType, boolean isMeid, boolean isUpload){
        return getHTTPClient(context,apiVersion, authType, isMeid, isUpload, null);
    }

    public static OkHttpClient getHTTPClient(final Context context, String apiVersion, AuthType authType, boolean isMeid, boolean isUpload, @Nullable Map<String, String> headerMap){
        return getHTTPClient(context,apiVersion, authType, isMeid, isUpload, headerMap, null);
    }

    public static OkHttpClient getHTTPClient(final Context context, String apiVersion, AuthType authType, boolean isMeid, boolean isUpload, @Nullable Map<String, String> headerMap, BuilderConfig builderConfig){
        List<String> headerList = getHeaderList(isMeid, isMeid, isMeid, isMeid, authType);
        Map<String, String> resultMap = getHeaderMap(context, headerList);
        if(headerMap != null){
            resultMap.putAll(headerMap);
        }
        if(isUpload){
            if(singletonUploadClient != null){
                return singletonUploadClient;
            }
        } else {
            if (singletonClient != null) {
                return singletonClient;
            }
        }

        OkHttpClient client = null;
        if(context!= null) {
            Interceptor interceptor = getInterceptor(resultMap, apiVersion);
            // Add the interceptor to OkHttpClient
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(isUpload ? HttpClientUtil.UPLOAD_DEFAULT_CONNECT_TIMEOUT_MILLIS : HttpClientUtil.DATA_DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                    .readTimeout(isUpload ? HttpClientUtil.UPLOAD_DEFAULT_READ_TIMEOUT_MILLIS : HttpClientUtil.DATA_DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                    .writeTimeout(isUpload ? HttpClientUtil.UPLOAD_DEFAULT_WRITE_TIMEOUT_MILLIS : HttpClientUtil.DATA_DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                    .addInterceptor(interceptor);


            TLSSocketFactory tlsSocketFactory= null;
            try {
                tlsSocketFactory = new TLSSocketFactory();
                if (tlsSocketFactory.getTrustManager()!=null) {
                    builder.sslSocketFactory(tlsSocketFactory, tlsSocketFactory.getTrustManager());
                }
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }


            if(singletonBuilderConfig != null){
                singletonBuilderConfig.configure(builder);
            }

            if (builderConfig != null) {
                builderConfig.configure(builder);
            }

            client = builder.build();
        }
        return client;
    }


    public static OkHttpClient getUnsafeHTTPClient(final Context context, Map<String, String> headerMap, String apiVersion, BuilderConfig builderConfig){
        return getUnsafeHTTPClient(context, headerMap, apiVersion,false, builderConfig);
    }

    public static OkHttpClient getUnsafeHTTPClient(final Context context, Map<String, String> headerMap, String apiVersion, boolean isUpload, BuilderConfig builderConfig) {
        if(isUpload){
            if(singletonUploadUnsafeClient != null){
                return singletonUploadUnsafeClient;
            }
        } else {
            if (singletonUnsafeClient != null) {
                return singletonUnsafeClient;
            }
        }

        try {
            OkHttpClient client = null;
            if(context!= null) {
                // Create a trust manager that does not validate certificate chains
                final TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };

                // Install the all-trusting trust manager
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();


                Interceptor interceptor = getInterceptor(headerMap, apiVersion);
                // Add the interceptor to OkHttpClient
                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .connectTimeout(isUpload ? HttpClientUtil.UPLOAD_DEFAULT_CONNECT_TIMEOUT_MILLIS : HttpClientUtil.DATA_DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .readTimeout(isUpload ? HttpClientUtil.UPLOAD_DEFAULT_READ_TIMEOUT_MILLIS : HttpClientUtil.DATA_DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .writeTimeout(isUpload ? HttpClientUtil.UPLOAD_DEFAULT_WRITE_TIMEOUT_MILLIS : HttpClientUtil.DATA_DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .addInterceptor(interceptor)
                        .sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0])
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        });

                if(singletonBuilderConfig != null){
                    singletonBuilderConfig.configure(builder);
                }

                if (builderConfig != null) {
                    builderConfig.configure(builder);
                }

                client = builder.build();
            }

            return client;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static OkHttpClient getCertificatePinnerHTTPClient(final Context context, Map<String, String> headerMap, String apiVersion, @IdRes int certRawResId, BuilderConfig builderConfig){
        return getCertificatePinnerHTTPClient(context,headerMap, apiVersion, false, certRawResId, builderConfig);
    }

    public static OkHttpClient getCertificatePinnerHTTPClient(final Context context, Map<String, String> headerMap, String apiVersion, boolean isUpload,  @IdRes int certRawResId, BuilderConfig builderConfig){
        OkHttpClient client = null;
        if(context!= null) {
            CustomTrust customTrust = new CustomTrust(context,headerMap,apiVersion, isUpload,certRawResId, builderConfig);
            client = customTrust.getClient();
        }
        return client;
    }


    public interface BuilderConfig {
        void configure(OkHttpClient.Builder builder);
    }



    public final static class CustomTrust {
        private final OkHttpClient client;
        private final Context context;
        private final int certRawResId;

        public CustomTrust(final Context context, Map<String, String> headerMap,String apiVersion, boolean isUpload, @IdRes int certRawResId, BuilderConfig builderConfig) {
            this.context = context;
            this.certRawResId = certRawResId;
            X509TrustManager trustManager;
            SSLSocketFactory sslSocketFactory;
            try {
                trustManager = trustManagerForCertificates(trustedCertificatesInputStream());
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustManager}, null);
                sslSocketFactory = sslContext.getSocketFactory();
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }

            Interceptor interceptor = getInterceptor(headerMap, apiVersion);
            // Add the interceptor to OkHttpClient
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(isUpload ? HttpClientUtil.UPLOAD_DEFAULT_CONNECT_TIMEOUT_MILLIS : HttpClientUtil.DATA_DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                    .readTimeout(isUpload ? HttpClientUtil.UPLOAD_DEFAULT_READ_TIMEOUT_MILLIS : HttpClientUtil.DATA_DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                    .writeTimeout(isUpload ? HttpClientUtil.UPLOAD_DEFAULT_WRITE_TIMEOUT_MILLIS : HttpClientUtil.DATA_DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                    .addInterceptor(interceptor)
                    .sslSocketFactory(sslSocketFactory, trustManager);
                    //.protocols(Arrays.asList(Protocol.HTTP_1_1))

            if(singletonBuilderConfig != null){
                singletonBuilderConfig.configure(builder);
            }

            if (builderConfig != null) {
                builderConfig.configure(builder);
            }

            client = builder.build();
        }

        public OkHttpClient getClient() {
            return client;
        }

        /**
         * Returns an input stream containing one or more certificate PEM files. This implementation just
         * embeds the PEM files in Java strings; most applications will instead read this from a resource
         * file that gets bundled with the application.
         */
        private InputStream trustedCertificatesInputStream() {
            return context.getResources().openRawResource(certRawResId);
        }

        /**
         * Returns a trust manager that trusts {@code certificates} and none other. HTTPS services whose
         * certificates have not been signed by these certificates will fail with a {@code
         * SSLHandshakeException}.
         *
         * <p>This can be used to replace the host platform's built-in trusted certificates with a custom
         * set. This is useful in development where certificate authority-trusted certificates aren't
         * available. Or in production, to avoid reliance on third-party certificate authorities.
         *
         * <p>See also {@link //CertificatePinner}, which can limit trusted certificates while still using
         * the host platform's built-in trust store.
         *
         * <h3>Warning: Customizing Trusted Certificates is Dangerous!</h3>
         *
         * <p>Relying on your own trusted certificates limits your server team's ability to update their
         * TLS certificates. By installing a specific set of trusted certificates, you take on additional
         * operational complexity and limit your ability to migrate between certificate authorities. Do
         * not use custom trusted certificates in production without the blessing of your server's TLS
         * administrator.
         */
        private X509TrustManager trustManagerForCertificates(InputStream in)
                throws GeneralSecurityException {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
            if (certificates.isEmpty()) {
                throw new IllegalArgumentException("expected non-empty set of trusted certificates");
            }

            // Put the certificates a key store.
            char[] password = "password".toCharArray(); // Any password will work.
            KeyStore keyStore = newEmptyKeyStore(password);
            int index = 0;
            for (Certificate certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificate);
            }

            // Use it to build an X509 trust manager.
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        }

        private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
            try {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                InputStream in = null; // By convention, 'null' creates an empty key store.
                keyStore.load(in, password);
                return keyStore;
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

    }




    public static String getModelNumberInUrlEncode(){
        String model = "";
        try {
            model = CommonUtil.urlEncode(CommonUtil.getModelNumber());
        } catch (UnsupportedEncodingException e) {
            model = CommonUtil.getModelNumber();
        }
        return model;
    }

    private static Interceptor getInterceptor(final Map<String, String> headerMap, final String apiVersion){
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();

                for (Map.Entry<String, String> header : headerMap.entrySet()){
                    builder.addHeader(header.getKey(), header.getValue());
                }

                if(!TextUtils.isEmpty(apiVersion)) {
                    builder.addHeader("x-api", apiVersion);
                }

                Request newRequest = builder.build();
                return chain.proceed(newRequest);
            }
        };
        return interceptor;
    }


    public static void setCustomPicassoSingletoneInstance(Context context, Map<String, String> headerMap, String apiVersion, BuilderConfig builderConfig){
        Picasso.setSingletonInstance(getPicassoInstance(context,headerMap, apiVersion, builderConfig));
    }


    private static Picasso getPicassoInstance(Context context, Map<String, String> headerMap, String apiVersion, BuilderConfig builderConfig){
        Interceptor interceptor = getInterceptor(headerMap, apiVersion);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(HttpClientUtil.IMAGE_DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .readTimeout(HttpClientUtil.IMAGE_DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .writeTimeout(HttpClientUtil.IMAGE_DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .addInterceptor(interceptor);

        if(singletonBuilderConfig != null){
            singletonBuilderConfig.configure(builder);
        }

        if (builderConfig != null) {
            builderConfig.configure(builder);
        }

        OkHttpClient client =  builder.build();
        Picasso picasso = new Picasso.Builder(context).downloader(new OkHttp3Downloader(client)).build();
        return picasso;
    }



    public static String getAuthBasic(){
        String credentials = PrefsData.getUserID()+":"+ PrefsData.getSecret();
        String result="";
        result = "Basic "+ Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        return result;
    }

    public static String getAuthAPIKey(){
        String credentials = "APIKEY="+ PrefsData.getApiKey();
        String result="";
        result = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        return result;
    }

    public static void handleWebviewSSLError(Context context, final SslErrorHandler handler, SslError error){
        String sslMessage = "";
        switch(error.getPrimaryError()) {
            case SslError.SSL_DATE_INVALID:
                sslMessage = context.getText(R.string.zlcore_notification_error_ssl_date_invalid).toString();
                break;
            case SslError.SSL_EXPIRED:
                sslMessage = context.getText(R.string.zlcore_notification_error_ssl_expired).toString();
                break;
            case SslError.SSL_IDMISMATCH:
                sslMessage = context.getText(R.string.zlcore_notification_error_ssl_idmismatch).toString();
                break;
            case SslError.SSL_INVALID:
                sslMessage = context.getText(R.string.zlcore_notification_error_ssl_invalid).toString();
                break;
            case SslError.SSL_NOTYETVALID:
                sslMessage = context.getText(R.string.zlcore_notification_error_ssl_not_yet_valid).toString();
                break;
            case SslError.SSL_UNTRUSTED:
                sslMessage = context.getText(R.string.zlcore_notification_error_ssl_untrusted).toString();
                break;
            default:
                sslMessage = context.getText(R.string.zlcore_notification_error_ssl_cert_invalid).toString();
        }

        CommonUtil.showDialog2Option(context, context.getText(R.string.zlcore_notification_error_ssl_title).toString(), sslMessage,
                context.getText(R.string.zlcore_notification_error_ssl_continue_text).toString(), new Runnable() {
                    @Override
                    public void run() {
                        handler.proceed();
                    }
                }, context.getText(R.string.zlcore_notification_error_ssl_cancel_text).toString(), new Runnable() {
                    @Override
                    public void run() {
                        handler.cancel();
                    }
                });
    }



    public static class TLSSocketFactory extends SSLSocketFactory {

        private SSLSocketFactory delegate;
        private TrustManager[] trustManagers;

        public TLSSocketFactory() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
            generateTrustManagers();
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, null);
            delegate = context.getSocketFactory();
        }

        private void generateTrustManagers() throws KeyStoreException, NoSuchAlgorithmException {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }

            this.trustManagers = trustManagers;
        }


        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket() throws IOException {
            return enableTLSOnSocket(delegate.createSocket());
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(s, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return enableTLSOnSocket(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
            return enableTLSOnSocket(delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return enableTLSOnSocket(delegate.createSocket(address, port, localAddress, localPort));
        }

        private Socket enableTLSOnSocket(Socket socket) {
            if(socket != null && (socket instanceof SSLSocket)) {
                ((SSLSocket)socket).setEnabledProtocols(new String[] {"TLSv1.1", "TLSv1.2"});
            }
            return socket;
        }

        @Nullable
        public X509TrustManager getTrustManager() {
            return  (X509TrustManager) trustManagers[0];
        }

    }
}
