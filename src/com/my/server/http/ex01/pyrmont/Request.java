package com.my.server.http.ex01.pyrmont;

import java.io.IOException;
import java.io.InputStream;

public class Request
{
	private InputStream input;
	private String uri;
	
	public Request(){}
	public Request(InputStream input)
	{
		this.input = input;
	}
	
	public String getUri()
	{
		return uri;
	}

	private String parseUri(String str)
	{
		int index1,index2;
		index1 = str.indexOf(" ");
		if (index1 != -1) {
			index2 = str.indexOf(" ", index1+1);
			if (index2 > index1) {
				return str.substring(index1+1, index2);
			}
		}
		return null;
	}
	
	public void parse()
	{
		//Read a set of characters from the socket
		StringBuffer request = new StringBuffer(2048);
		int i;
		byte[] buffer = new byte[2048];
		try {
			i= input.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
			i=-1;
		}
		for (int j = 0; j < i; j++) {
			request.append((char)buffer[j]);
		}
		System.out.println(request.toString());
		uri = parseUri(request.toString());
	}
	
}
