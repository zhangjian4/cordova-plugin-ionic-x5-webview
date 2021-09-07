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
import android.util.Log;

import org.apache.cordova.*;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.utils.TbsLog;

public class MainActivity extends CordovaActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // enable Cordova apps to be started in the background
    Bundle extras = getIntent().getExtras();
    if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
      moveTaskToBack(true);
    }
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
      initX5();
    } else {
      // Set by <content src="index.html" /> in config.xml
      loadUrl(launchUrl);
    }

  }

  public void initX5() {
    TbsLog.setWriteLogJIT(false);
    if (QbSdk.canLoadX5(getApplicationContext())) {
      Log.i("TBS_X5", "已安装好，直接显示");
    } else {
      Log.i("TBS_X5", "新安装");
      boolean ok = QbSdk.preinstallStaticTbs(getApplicationContext());
      Log.i("TBS_X5", "安装成功：" + ok);
    }
    loadUrl(launchUrl);
  }
}
