/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package ${package};

import android.os.Build;
import android.os.Bundle;

import org.apache.cordova.*;

import java.util.HashMap;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;

public class MainActivity extends CordovaActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // enable Cordova apps to be started in the background
    Bundle extras = getIntent().getExtras();
    if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
      moveTaskToBack(true);
    }
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR) {
      initX5();
    } else {
      // Set by <content src="index.html" /> in config.xml
      loadUrl(launchUrl);
    }

  }

  public void initX5() {
    HashMap map = new HashMap();
    map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
    map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
    QbSdk.initTbsSettings(map);
    QbSdk.setDownloadWithoutWifi(true);
    QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

      @Override
      public void onViewInitFinished(boolean b) {
        loadUrl(launchUrl);
      }

      @Override
      public void onCoreInitFinished() {
      }
    };
    QbSdk.initX5Environment(getApplicationContext(), cb);
  }
}
