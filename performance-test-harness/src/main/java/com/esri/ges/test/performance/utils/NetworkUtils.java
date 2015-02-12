package com.esri.ges.test.performance.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkUtils
{
	public static boolean isLocal(String hostName)
	{
		try
		{
			//get the address
			InetAddress address = InetAddress.getByName(hostName);
			
			// Check if the address is a valid special local or loop back
			if (address.isAnyLocalAddress() || address.isLoopbackAddress())
				return true;
	
			// Check if the address is defined on any interface
			try
			{
				return NetworkInterface.getByInetAddress(address) != null;
			}
			catch (SocketException e)
			{
				return false;
			}
		} catch(UnknownHostException error)
		{
			return false;
		}
	}
	
}
