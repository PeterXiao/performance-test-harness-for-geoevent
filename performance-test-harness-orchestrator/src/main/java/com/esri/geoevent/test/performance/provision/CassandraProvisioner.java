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
package com.esri.geoevent.test.performance.provision;

import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.db.DBClient;
import com.esri.geoevent.test.performance.db.cassandra.CassandraClient;
import com.esri.geoevent.test.performance.jaxb.Config;

public class CassandraProvisioner implements Provisioner {

    private String nodeName;
    private String keyspace;
    private String tableName;

    private static final String NAME = "Cassandra";

    @Override
    public void init(Config config) throws ProvisionException {
        if (config == null) {
            throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_INIT_ERROR", getClass().getSimpleName()));
        }

        this.nodeName = config.getPropertyValue("nodeName");
        this.keyspace = config.getPropertyValue("keyspace");
        this.tableName = config.getPropertyValue("tableName");

        validate();
    }

    @Override
    public void provision() throws ProvisionException {
        try {
            System.out.println("-------------------------------------------------------");
            System.out.println(ImplMessages.getMessage("PROVISIONER_START_MSG", NAME));
            //System.out.println("-------------------------------------------------------");
            truncateTable();
            System.out.println(ImplMessages.getMessage("PROVISIONER_FINISH_MSG", NAME));
        } catch (Exception ex) {
            throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_ERROR", NAME, ex.getMessage()));
        }
    }

    private void validate() throws ProvisionException {
        if (StringUtils.isEmpty(nodeName)) {
            throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "nodeName"));
        }
        if (StringUtils.isEmpty(keyspace)) {
            throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "keyspace"));
        }
        if (StringUtils.isEmpty(tableName)) {
            throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_PROPERTY_VALIDATION", "tableName"));
        }

        // check if we can connect
        try (DBClient client = new CassandraClient(nodeName, keyspace, tableName, null)) {
            ;
        } catch (Exception error) {
            throw new ProvisionException(ImplMessages.getMessage("PROVISIONER_CANNOT_CONNECT", "nodeName", error.getMessage()), error);
        }
    }

    private void truncateTable() {
        try (DBClient client = new CassandraClient(nodeName, keyspace, tableName, null)) {
            client.truncate();
        } catch (Exception error) {
            System.err.println(ImplMessages.getMessage("PROVISIONER_TRUNCATE_FAILED", keyspace, tableName, error.getMessage()));
            error.printStackTrace();
        }
    }
}
