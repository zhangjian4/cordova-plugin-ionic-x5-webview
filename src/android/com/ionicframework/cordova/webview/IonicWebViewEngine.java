package com.ionicframework.cordova.webview;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import com.tencent.smtt.sdk.ServiceWorkerController;
import com.tencent.smtt.export.external.interfaces.ServiceWorkerClient;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.NativeToJsMessageQueue;
import org.apache.cordova.PluginManager;
import org.apache.cordova.x5engine.X5WebViewClient;
import org.apache.cordova.x5engine.X5WebViewEngine;
import org.apache.cordova.x5engine.X5WebView;

public class IonicWebViewEngine extends X5WebViewEngine {
  public static final String TAG = "IonicWebViewEngine";

  private WebViewLocalServer localServer;
  private String CDV_LOCAL_SERVER;
  private String scheme;
  private static final String LAST_BINARY_VERSION_CODE = "lastBinaryVersionCode";
  private static final String LAST_BINARY_VERSION_NAME = "lastBinaryVersionName";

  /**
   * Used when created via reflection.
   */
  public IonicWebViewEngine(Context context, CordovaPreferences preferences) {
    super(new X5WebView(context), preferences);
    Log.d(TAG, "Ionic Web View Engine Starting Right Up 1...");
  }

  public IonicWebViewEngine(X5WebView webView) {
    super(webView, null);
    Log.d(TAG, "Ionic Web View Engine Starting Right Up 2...");
  }

  public IonicWebViewEngine(X5WebView webView, CordovaPreferences preferences) {
    super(webView, preferences);
    Log.d(TAG, "Ionic Web View Engine Starting Right Up 3...");
  }

  @Override
  public void init(CordovaWebView parentWebView, CordovaInterface cordova, final CordovaWebViewEngine.Client client,
                   CordovaResourceApi resourceApi, PluginManager pluginManager,
                   NativeToJsMessageQueue nativeToJsMessageQueue) {
    ConfigXmlParser parser = new ConfigXmlParser();
    parser.parse(cordova.getActivity());

    String hostname = preferences.getString("Hostname", "localhost");
    scheme = preferences.getString("Scheme", "http");
    CDV_LOCAL_SERVER = scheme + "://" + hostname;

    localServer = new WebViewLocalServer(cordova.getActivity(), hostname, true, parser, scheme);
    localServer.hostAssets("www");

    webView.setWebViewClient(new ServerClient(this, parser));

    super.init(parentWebView, cordova, client, resourceApi, pluginManager, nativeToJsMessageQueue);
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      final WebSettings settings = webView.getSettings();
      int mode = preferences.getInteger("MixedContentMode", 0);
      settings.setMixedContentMode(mode);
    }
    SharedPreferences prefs = cordova.getActivity().getApplicationContext().getSharedPreferences(IonicWebView.WEBVIEW_PREFS_NAME, Activity.MODE_PRIVATE);
    String path = prefs.getString(IonicWebView.CDV_SERVER_PATH, null);
    if (!isDeployDisabled() && !isNewBinary() && path != null && !path.isEmpty()) {
      setServerBasePath(path);
    }

    boolean setAsServiceWorkerClient = preferences.getBoolean("ResolveServiceWorkerRequests", false);
    ServiceWorkerController controller = null;

    if (setAsServiceWorkerClient && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        controller = ServiceWorkerController.getInstance(cordova.getActivity().getApplicationContext());
        controller.setServiceWorkerClient(new ServiceWorkerClient(){
            @Override
            public WebResourceResponse shouldInterceptRequest(WebResourceRequest request) {
                return localServer.shouldInterceptRequest(request.getUrl(), request);
            }
        });
    }
  }

  private boolean isNewBinary() {
    String versionCode = "";
    String versionName = "";
    SharedPreferences prefs = cordova.getActivity().getApplicationContext().getSharedPreferences(IonicWebView.WEBVIEW_PREFS_NAME, Activity.MODE_PRIVATE);
    String lastVersionCode = prefs.getString(LAST_BINARY_VERSION_CODE, null);
    String lastVersionName = prefs.getString(LAST_BINARY_VERSION_NAME, null);

    try {
      PackageInfo pInfo = this.cordova.getActivity().getPackageManager().getPackageInfo(this.cordova.getActivity().getPackageName(), 0);
      versionCode = Integer.toString(pInfo.versionCode);
      versionName = pInfo.versionName;
    } catch(Exception ex) {
      Log.e(TAG, "Unable to get package info", ex);
    }

    if (!versionCode.equals(lastVersionCode) || !versionName.equals(lastVersionName)) {
      SharedPreferences.Editor editor = prefs.edit();
      editor.putString(LAST_BINARY_VERSION_CODE, versionCode);
      editor.putString(LAST_BINARY_VERSION_NAME, versionName);
      editor.putString(IonicWebView.CDV_SERVER_PATH, "");
      editor.apply();
      return true;
    }
    return false;
  }

  private boolean isDeployDisabled() {
    return preferences.getBoolean("DisableDeploy", false);
  }
  private class ServerClient extends X5WebViewClient {
    private ConfigXmlParser parser;

    public ServerClient(X5WebViewEngine parentEngine, ConfigXmlParser parser) {
      super(parentEngine);
      this.parser = parser;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
      return localServer.shouldInterceptRequest(request.getUrl(), request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);
      String launchUrl = parser.getLaunchUrl();
      if (!launchUrl.contains(WebViewLocalServer.httpsScheme) && !launchUrl.contains(WebViewLocalServer.httpScheme) && url.equals(launchUrl)) {
        view.stopLoading();
        // When using a custom scheme the app won't load if server start url doesn't end in /
        String startUrl = CDV_LOCAL_SERVER;
        if (!scheme.equalsIgnoreCase(WebViewLocalServer.httpsScheme) && !scheme.equalsIgnoreCase(WebViewLocalServer.httpScheme)) {
          startUrl += "/";
        }
        view.loadUrl(startUrl);
      }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
      view.loadUrl("javascript:(function() { " +
              "window.WEBVIEW_SERVER_URL = '" + CDV_LOCAL_SERVER + "';" +
              "})()");
    }
  }

  public void setServerBasePath(String path) {
    localServer.hostFiles(path);
    webView.loadUrl(CDV_LOCAL_SERVER);
  }

  public String getServerBasePath() {
    return this.localServer.getBasePath();
  }
}
