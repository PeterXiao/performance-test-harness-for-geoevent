package com.esri.geoevent.test.performance.db;

import java.io.Closeable;

public interface DBClient extends Closeable
{
	 void truncate();
	  
	 DBResult queryForLastWriteTimes();
}
