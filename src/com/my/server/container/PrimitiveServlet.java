package com.my.server.container;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class PrimitiveServlet implements Servlet
{

	@Override
	public void init(ServletConfig paramServletConfig) throws ServletException
	{
		System.out.println("init");
	}

	@Override
	public ServletConfig getServletConfig()
	{
		return null;
	}

	@Override
	public void service(ServletRequest paramServletRequest, ServletResponse paramServletResponse) throws ServletException, IOException
	{
		System.out.println("from service");
//		String target = "Rose are red.";
//		ServletOutputStream outputStream = paramServletResponse.getOutputStream();
//		outputStream.write(target.getBytes());
		PrintWriter out = paramServletResponse.getWriter();
		out.println("Rose are red.");
		out.println("Violets are blue.");
	}

	@Override
	public String getServletInfo()
	{
		return null;
	}

	@Override
	public void destroy()
	{
		System.out.println("destroy");
	}

}
