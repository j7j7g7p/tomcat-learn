package com.my.server.http.ex03.pyrmont.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.Servlet;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

public class Response implements ServletResponse
{
	private static final int BUFFER_SIZE = 1024;
	
	Request request;
	OutputStream output;
	public Response(){}

	public Response(OutputStream output)
	{
		this.output = output;
	}

	public void setRequest(Request request)
	{
		this.request =request;
	}

	public void sendStaticResource() throws IOException
	{
		byte[] bytes = new byte[BUFFER_SIZE];
		FileInputStream fis =null;
		try {
			System.out.println(request.getUri());
			String uri = request.getUri();
			if ("/".equals(request.getUri())) {
				uri = "/index.html";
			}
			File file = new File(HttpServer1.WEB_ROOT,uri.substring(uri.lastIndexOf("/")+1));
			if (file.exists()) {
				fis = new FileInputStream(file);
				int ch = fis.read(bytes, 0, BUFFER_SIZE);
				while(ch!=-1){
					output.write(bytes, 0, ch);
					ch = fis.read(bytes, 0, BUFFER_SIZE);
				}
//				output.write("Content-Type: text/html\r\n".getBytes());
			}
			else {
				//file not found
				String errorMessage = "HTTP/1.1 404 File Not Found\r\n"+
				"Content-Type: text/html\r\n"+
				"Content_Length: 23\r\n"+
				"\r\n"+
				"<h1>File Not Found</h1>";
				output.write(errorMessage.getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if (fis!=null) {
				fis.close();
			}
		}
	}
	@Override
	public PrintWriter getWriter() throws IOException
	{
		return null;
	}

	@Override
	public void flushBuffer() throws IOException
	{
	}

	@Override
	public int getBufferSize()
	{
		return 0;
	}

	@Override
	public String getCharacterEncoding()
	{
		return null;
	}

	@Override
	public Locale getLocale()
	{
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException
	{
		return null;
	}


	@Override
	public boolean isCommitted()
	{
		return false;
	}

	@Override
	public void reset()
	{
		
	}

	@Override
	public void resetBuffer()
	{
	}

	@Override
	public void setBufferSize(int arg0)
	{
	}

	@Override
	public void setContentLength(int arg0)
	{
	}

	@Override
	public void setContentType(String arg0)
	{
	}

	@Override
	public void setLocale(Locale arg0)
	{
	}
}
