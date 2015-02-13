package com.esri.geoevent.test.performance.jaxb;

import com.esri.geoevent.test.performance.jaxb.AbstractTest.TestType;

public interface Test extends Appliable<Test>
{
	TestType getType();

	void setType(TestType type);
	
	Test copy();
}
