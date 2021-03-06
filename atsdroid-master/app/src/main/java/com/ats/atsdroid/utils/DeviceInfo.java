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

package com.ats.atsdroid.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.ats.atsdroid.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import androidx.test.platform.app.InstrumentationRegistry;

public class DeviceInfo {

    //----------------------------------------------------------------------------------
    // Singleton declaration
    //----------------------------------------------------------------------------------

    private static DeviceInfo instance;

    public static DeviceInfo getInstance() {
        if(instance == null){
            instance  = new DeviceInfo();
        }
        return instance;
    }

    private DeviceInfo() {
        AtsAutomation.sendLogs("Get bluetooth adapter name\n");
        BluetoothAdapter btDevice = BluetoothAdapter.getDefaultAdapter();
        if (btDevice != null) {
            btAdapter = btDevice.getName();
        }


        hostName = tryGetHostname();
        systemName = getAndroidVersion();
    }

    //----------------------------------------------------------------------------------
    // Utils
    //----------------------------------------------------------------------------------

    private String getAndroidVersion(){
        double release = Double.parseDouble(Build.VERSION.RELEASE.replaceAll("(\\d+[.]\\d+)(.*)","$1"));
        String codeName="undefined";//below Jelly bean OR above Oreo
        if(release>=4.1 && release<4.4)codeName="Jelly Bean";
        else if(release<5)codeName="Kit Kat";
        else if(release<6)codeName="Lollipop";
        else if(release<7)codeName="Marshmallow";
        else if(release<8)codeName="Nougat";
        else if(release<9)codeName="Oreo";
        else if(release<10)codeName="Pie";
        else if(release<11)codeName="Android 10";
        return codeName+" v"+release+", API Level: "+Build.VERSION.SDK_INT;
    }

    public static String tryGetHostname() {
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        AtsAutomation.sendLogs("getting IP address" + inetAddress.getHostName() + "\n");
                        return inetAddress.getHostName();
                    }
                }
            }
        } catch (SocketException e) {
            return "error -> " + e;
        }
        return "undefined";
    }



    //----------------------------------------------------------------------------------
    // Instance access
    //----------------------------------------------------------------------------------
    
    enum PropertyName {
        // airplaneModeEnabled,
        // nightModeEnabled,
        wifiEnabled,
        bluetoothEnabled,
        lockOrientationEnabled,
        orientation,
        // brightness,
        volume;
    
        public static String[] getNames() {
            PropertyName[] states = values();
            String[] names = new String[states.length];
    
            for (int i = 0; i < states.length; i++) {
                names[i] = states[i].name();
            }
    
            return names;
        }
    }
    
    private int port;

    private int deviceWidth;
    private int deviceHeight;
    private int channelWidth;
    private int channelHeight;

    private Matrix matrix;

    private final String systemName;
    private final String deviceId = Build.ID;
    private final String model = Build.MODEL;
    private final String manufacturer = Build.MANUFACTURER;
    private final String brand = Build.BRAND;
    private final String version = Build.VERSION.RELEASE;
    private final String build = Build.VERSION.INCREMENTAL;
    private final String driverVersion = BuildConfig.VERSION_NAME;
    private String hostName;
    private String btAdapter;
    
    public void initDevice(int p, String ipAddress){
        hostName = ipAddress;
        port = p;
        setupScreenInformation();
    }

    public void setupScreenInformation() {
        WindowManager manager = (WindowManager) InstrumentationRegistry.getInstrumentation().getContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        manager.getDefaultDisplay().getRealSize(point);
        
        channelHeight = point.y;
        channelWidth = point.x;
    
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        deviceWidth = Math.round((float)channelWidth / metrics.scaledDensity);
        deviceHeight = Math.round((float)channelHeight / metrics.scaledDensity);

        matrix = new Matrix();
        matrix.preScale((float)deviceWidth / (float)channelWidth, (float)deviceHeight / (float)channelHeight);
        
    }
    
    private int deadZoneHeight(int height) {
        return channelHeight - height - navBarHeight();
    }
    
    
    private int navBarHeight() {
        Resources resources = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public Matrix getMatrix(){
        return matrix;
    }

    public void driverInfoBase(JSONObject obj, int height) throws JSONException {
        setupScreenInformation();
        obj.put("os", "android");
        obj.put("driverVersion", driverVersion);
        obj.put("systemName", systemName);
        obj.put("deviceWidth", deviceWidth);
        obj.put("deviceHeight", deviceHeight - (navBarHeight() / Resources.getSystem().getDisplayMetrics().scaledDensity));
        obj.put("channelWidth", channelWidth);
        obj.put("channelHeight", getChannelHeight());
        obj.put("osBuild", build);
        obj.put("mobileUser", "");
        obj.put("deadZoneY", height);
        obj.put("deadZoneHeight", deadZoneHeight(height));
        
        String bluetoothName = Settings.Secure.getString( InstrumentationRegistry.getInstrumentation().getContext().getContentResolver(), "bluetooth_name");
        obj.put("mobileName", bluetoothName == null ? "" : bluetoothName);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            obj.put("country",  InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getConfiguration().getLocales().get(0).getCountry());
        } else {
            obj.put("country",  InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getConfiguration().locale.getCountry());
        }
        
        obj.put("systemProperties", new JSONArray(PropertyName.getNames()));
        obj.put("systemButtons", new JSONArray(SysButton.ButtonType.getNames()));
    }
    
    public String getSystemName(){ return systemName; }
    public String getDeviceId() {
        return deviceId;
    }
    public String getModel() {
        return model;
    }
    public String getManufacturer() {
        return manufacturer;
    }
    public String getBrand() {
        return brand;
    }
    public String getVersion() {
        return version;
    }
    public String getHostName() {
        return hostName;
    }
    public String getBtAdapter() {
        return btAdapter;
    }
    public int getPort(){ return port; }
    public int getDeviceWidth() { return deviceWidth; }
    public int getDeviceHeight() { return deviceHeight; }
    public int getChannelWidth() {return channelWidth; }
    public int getChannelHeight() {
        return channelHeight - navBarHeight();
    }
}