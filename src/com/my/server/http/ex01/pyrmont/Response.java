package com.my.server.http.ex01.pyrmont;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.Servlet;

public class Response
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
			File file = new File(HttpServer.WEB_ROOT,uri);
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
}
