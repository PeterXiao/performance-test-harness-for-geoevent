package com.esri.geoevent.test.performance.kafka;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

public class SimplePartitioner implements Partitioner
{
  public SimplePartitioner (VerifiableProperties props) {
    
  }

  public int partition(Object key, int a_numPartitions) {
     return Integer.parseInt((String)key) % a_numPartitions;
  }
}
