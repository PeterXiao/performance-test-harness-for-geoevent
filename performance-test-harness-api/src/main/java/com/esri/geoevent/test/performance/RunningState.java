package com.esri.geoevent.test.performance;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class RunningState
{
	private RunningStateType type;
	private String connectionString;
	
	public RunningState()
	{
	}
	
	public RunningState(RunningStateType type)
	{
		this.type = type;
	}
	
	public RunningState(RunningStateType type, String connectionString)
	{
		this.type = type;
		this.connectionString = connectionString;
	}
	
	public RunningStateType getType()
	{
		return type;
	}
	public void setType(RunningStateType type)
	{
		this.type = type;
	}
	
	public String getConnectionString()
	{
		return connectionString;
	}
	public void setConnectionString(String connectionString)
	{
		this.connectionString = connectionString;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof RunningState))
      return false;
		
		RunningState runningState = (RunningState) obj;
    if (!ObjectUtils.equals(getType(), runningState.getType()))
      return false;
    if (!ObjectUtils.equals(getConnectionString(), runningState.getConnectionString()))
      return false;
    
		return super.equals(obj);
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
