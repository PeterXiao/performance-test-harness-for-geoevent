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
package com.esri.ges.test.performance.cassandra;

import org.junit.Test;

import com.esri.geoevent.test.performance.TestHarnessExecutor;

public class CassandraTestHarnessTest
{
	// ---------------------------------------------------------
	// Tests
	// ---------------------------------------------------------
	
	@Test
	public void testPerformanceTestHarnessMode_CASSANDRA()
	{		
		// setup consumer
		String args = "-m consumer -t cassandra -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// setup producer
		args = "-m producer -t kafka -p local";
		TestHarnessExecutor.main(args.split(" "));
		
		// run the test
		args = "-f src/test/resources/fixtures_cassandra.xml";
		TestHarnessExecutor.main(args.split(" "));
	}
}
