package com.esri.geoevent.test.performance.jaxb;

public interface Test extends Appliable<Test>
{
	TestType getType();

	void setType(TestType type);
	
	Test copy();
}
