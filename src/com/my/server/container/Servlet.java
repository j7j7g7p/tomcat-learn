package com.my.server.container;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public interface Servlet
{
	public abstract void init(ServletConfig paramServletConfig) throws ServletException;

	public abstract ServletConfig getServletConfig();

	public abstract void service(ServletRequest paramServletRequest, ServletResponse paramServletResponse) throws ServletException, IOException;

	public abstract String getServletInfo();

	public abstract void destroy();
}
