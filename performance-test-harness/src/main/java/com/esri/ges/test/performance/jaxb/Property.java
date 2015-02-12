package com.esri.ges.test.performance.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = "Property")
public class Property
{
	private String name;
	private String value;
	
	public Property()
	{
	}
	
	public Property(String name, String value)
	{
		this.name = name;
		this.value = value;
	}
	
	@XmlAttribute(name = "name")
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	@XmlValue
	public String getValue()
	{
		return value;
	}
	public void setValue(String value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof Property))
      return false;
		
		Property property = (Property) obj;
    if (!ObjectUtils.equals(getName(), property.getName()))
      return false;
    if (!ObjectUtils.equals(getValue(), property.getValue()))
      return false;
    
    return true;
	}
	
}
