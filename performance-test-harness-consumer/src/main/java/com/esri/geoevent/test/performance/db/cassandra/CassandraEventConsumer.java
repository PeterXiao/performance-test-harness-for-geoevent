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
package com.esri.geoevent.test.performance.db.cassandra;

import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.db.DBClient;
import com.esri.geoevent.test.performance.db.DBConsumerBase;
import com.esri.geoevent.test.performance.jaxb.Config;

public class CassandraEventConsumer extends DBConsumerBase {

    // member vars

    private String nodeName;
    private String keyspace;
    private String tableName;
    private String columnName;

    @Override
    public void init(Config config) throws TestException {
        super.init(config);

        this.nodeName = config.getPropertyValue("nodeName");
        this.keyspace = config.getPropertyValue("keyspace");
        this.tableName = config.getPropertyValue("tableName");
        this.columnName = config.getPropertyValue("columnName");
    }

    @Override
    public void validate() throws TestException {
        if (StringUtils.isEmpty(nodeName)) {
            throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "nodeName"));
        }
        if (StringUtils.isEmpty(keyspace)) {
            throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "keyspace"));
        }
        if (StringUtils.isEmpty(tableName)) {
            throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "tableName"));
        }
        if (StringUtils.isEmpty(columnName)) {
            throw new TestException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "columnName"));
        }

        // check if we can connect
        try (DBClient client = getDBClient()) {
            ;
        } catch (Exception error) {
            throw new TestException(ImplMessages.getMessage("PROVISIONER_CANNOT_CONNECT", "nodeName"));
        }
    }

    @Override
    public DBClient getDBClient() {
        return new CassandraClient(nodeName, keyspace, tableName, columnName);
    }
}
