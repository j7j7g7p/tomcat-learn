package com.my.server.http.ex03.pyrmont.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;

public class RequestFacade implements ServletRequest
{
	private ServletRequest request = null;
	
	public RequestFacade(Request request) {
		this.request = request;
	}
	
	@Override
	public Object getAttribute(String arg0)
	{
		return request.getAttribute(arg0);
	}

	@Override
	public Enumeration getAttributeNames()
	{
		return request.getAttributeNames();
	}

	@Override
	public String getCharacterEncoding()
	{
		return null;
	}

	@Override
	public int getContentLength()
	{
		return 0;
	}

	@Override
	public String getContentType()
	{
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException
	{
		return null;
	}

	@Override
	public Locale getLocale()
	{
		return null;
	}

	@Override
	public Enumeration getLocales()
	{
		return null;
	}

	@Override
	public String getParameter(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map getParameterMap()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration getParameterNames()
	{
		return null;
	}

	@Override
	public String[] getParameterValues(String arg0)
	{
		return null;
	}

	@Override
	public String getProtocol()
	{
		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException
	{
		return null;
	}

	@Override
	public String getRealPath(String arg0)
	{
		return null;
	}

	@Override
	public String getRemoteAddr()
	{
		return null;
	}

	@Override
	public String getRemoteHost()
	{
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0)
	{
		return null;
	}

	@Override
	public String getScheme()
	{
		return null;
	}

	@Override
	public String getServerName()
	{
		return null;
	}

	@Override
	public int getServerPort()
	{
		return 0;
	}

	@Override
	public boolean isSecure()
	{
		return false;
	}

	@Override
	public void removeAttribute(String arg0)
	{
		
	}

	@Override
	public void setAttribute(String arg0, Object arg1)
	{
		
	}

	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException
	{
		
	}
	
}
