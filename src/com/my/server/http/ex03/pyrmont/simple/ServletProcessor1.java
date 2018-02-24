package com.my.server.http.ex03.pyrmont.simple;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.my.server.utils.Constants;

public class ServletProcessor1
{

	public void process(Request request, Response response)
	{
		String uri = request.getUri();// uri形式/servlet/servletName
		String servletName = uri.substring(uri.lastIndexOf("/")+1);
		URLClassLoader loader = null;
		try {
			// 创建URL加载器
			URL[] urls = new URL[1];
			URLStreamHandler streamHandler = null;
			File classpath = new File(Constants.WEB_ROOT);
			// the forming of repository is taken from the
			// createClassLoader method in
			// org.apache.catalina.startup.ClassLoaderFactory
			String respository = (new URL("file", null, classpath.getCanonicalPath() + File.separator)).toString();
			// servlet容器里边能找到servlet的地方叫respository
			// //getCanonicalPath获取绝对路径的规范路径会将../这样的东西翻译出来
			// the code for forming the URL is taken from
			// the addRepository method in
			// org.apache.catalina.loader.StandardClassLoader
			urls[0] = new URL(null, respository, streamHandler);
			loader = new URLClassLoader(urls);

		} catch (Exception e) {
			e.printStackTrace();
		}
		Class myClass = null;
		try {
			myClass = loader.loadClass(servletName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Servlet servlet = null;
		try {
			servlet = (Servlet) myClass.newInstance();
			servlet.service((ServletRequest) request, (ServletResponse) response);
		} catch (Exception e) {
		}

	}

}
