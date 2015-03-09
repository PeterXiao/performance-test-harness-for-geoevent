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
package com.esri.geoevent.test.performance.jaxb;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.ObjectUtils;

public abstract class AbstractTest implements Test
{
	private TestType type;
	
	@XmlTransient
	public TestType getType()
	{
		return type;
	}
	public void setType(TestType type)
	{
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof AbstractTest))
      return false;
		
		AbstractTest test = (AbstractTest) obj;
    if (!ObjectUtils.equals(getType(), test.getType()))
      return false;
    
    return true;
	}
}
