/*
 Copyright 1995-2015 Esri

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 For additional information, contact:
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */
package com.esri.geoevent.test.tools;

import com.esri.arcgis.discovery.util.KeyUtil;



/**
 *
 * @author davi5017
 */
public class DecryptBDSPassword {
    
    /**
     * 
     * @param args Encrypted Password (e.g. {crypt}VovBzQGONbtJnPw02rmmag==)
     */
    public static void main(String[] args) {
        int numArgs = args.length;
        
        if (numArgs != 1) {
            System.out.println("Command requires one command line argument. The encrypted password. (e.g. {crypt}VovBzQGONbtJnPw02rmmag==)");
            System.exit(1);
        }
        
        System.out.println(KeyUtil.decryptKey(args[0]));
        
    }
    
}
