package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "KafkaHost")
public class KafkaHost
{
  private String brokerList;
  private String topic;
  private String acks;
  
  @XmlValue
  public String getBrokerList()
  {
    return brokerList;
  }
  public void setBrokerList(String brokerList)
  {
    this.brokerList = brokerList;
  }
  
  @XmlAttribute
  public String getTopic()
  {
    return topic;
  }
  public void setTopic(String topic)
  {
    this.topic = topic;
  }
  
  @XmlAttribute
  public String getAcks()
  {
    return acks;
  }
  public void setAcks(String acks)
  {
    this.acks = acks;
  }
  
  @Override
  public String toString()
  {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
