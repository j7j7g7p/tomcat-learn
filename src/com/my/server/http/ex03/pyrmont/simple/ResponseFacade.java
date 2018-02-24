package com.my.server.http.ex03.pyrmont.simple;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

public class ResponseFacade implements ServletResponse
{
	private ServletResponse response = null;
	
	public ResponseFacade(Response response)
	{
		this.response = response;
	}
	@Override
	public void flushBuffer() throws IOException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getBufferSize()
	{
		return response.getBufferSize();
	}

	@Override
	public String getCharacterEncoding()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Locale getLocale()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCommitted()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetBuffer()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBufferSize(int paramInt)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentLength(int paramInt)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentType(String paramString)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocale(Locale paramLocale)
	{
		// TODO Auto-generated method stub
		
	}
	
}
