package com.esri.ges.test.performance.jaxb;

import com.esri.ges.test.performance.jaxb.AbstractTest.TestType;

public interface Test
{
	TestType getType();

	void setType(TestType type);
}
