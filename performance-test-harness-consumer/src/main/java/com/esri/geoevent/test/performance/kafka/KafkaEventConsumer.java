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
package com.esri.geoevent.test.performance.kafka;

import java.util.UUID;

import com.esri.geoevent.test.performance.ConsumerBase;
import com.esri.geoevent.test.performance.ImplMessages;
import com.esri.geoevent.test.performance.TestException;
import com.esri.geoevent.test.performance.jaxb.Config;

public class KafkaEventConsumer extends ConsumerBase {

    private KafkaConsumerGroup kafkaConsumerGroup;
    private String zookeeperString = "localhost:2181";
    private String consumerGroupId = UUID.randomUUID().toString();
    private String topic;
    private int numThreads;
    private boolean receiveGeoEvent;

    @Override
    public void init(Config config) throws TestException {
        super.init(config);
        try {
            zookeeperString = config.getPropertyValue("zookeeper", String.valueOf(zookeeperString));
            consumerGroupId = config.getPropertyValue("consumergroup", String.valueOf(consumerGroupId));
            topic = config.getPropertyValue("topic");
            numThreads = Integer.parseInt(config.getPropertyValue("numthreads", String.valueOf(1)));
            receiveGeoEvent = Boolean.getBoolean(config.getPropertyValue("receiveGeoEvent", "true"));

            kafkaConsumerGroup = new KafkaConsumerGroup(zookeeperString, consumerGroupId, topic, receiveGeoEvent, message -> receive(message));
            kafkaConsumerGroup.run(numThreads);
        } catch (Throwable error) {
            throw new TestException(ImplMessages.getMessage("INIT_FAILURE", getClass().getName(), error.getMessage()), error);
        }
    }

    @Override
    public void validate() throws TestException {
        if (kafkaConsumerGroup == null) {
            throw new TestException("Kafka connection is not established. Please initialize KafkaEventConsumer before it starts collecting diagnostics.");
        }
    }

    @Override
    public void destroy() {
        if (kafkaConsumerGroup != null) {
            kafkaConsumerGroup.shutdown();
            kafkaConsumerGroup = null;
        }
    }
}
