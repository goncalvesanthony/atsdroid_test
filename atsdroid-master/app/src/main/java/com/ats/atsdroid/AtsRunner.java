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

package com.ats.atsdroid;

import android.graphics.Rect;
import com.ats.atsdroid.utils.AtsAutomation;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4ClassRunner.class)
public class AtsRunner {
    
    protected static final int DEFAULT_PORT = 8080;
    protected AtsAutomation automation;
    protected volatile boolean running = true;
    protected int port = DEFAULT_PORT;
    
    public void stop(){
        running = false;
    }
    
    @Test
    public void testMain() {
        
        try {
            port = Integer.parseInt(Objects.requireNonNull(InstrumentationRegistry.getArguments().getString("atsPort")));
            
            Boolean usbMode = Boolean.parseBoolean(InstrumentationRegistry.getArguments().getString("usbMode"));
            
            String ipAddress = InstrumentationRegistry.getArguments().getString("ipAddress");
    
            String rootBound = InstrumentationRegistry.getArguments().getString("rootBounds");
            
            automation = new AtsAutomation(port, this, ipAddress, usbMode, stringToRect(rootBound));
            
        } catch (Exception ignored) {
        
        }
    }
    
    private Rect stringToRect(String a) {
        final String[] rect = a.split(",");
        return new Rect(Integer.parseInt(rect[0]), Integer.parseInt(rect[1]), Integer.parseInt(rect[2]), Integer.parseInt(rect[3]));
    }
}