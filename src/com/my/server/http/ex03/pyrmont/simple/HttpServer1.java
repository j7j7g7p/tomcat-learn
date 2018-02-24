package com.my.server.http.ex03.pyrmont.simple;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 该类表示一个web服务器
 * @author 罗尚林
 *
 */
public class HttpServer1
{
	//web 项目的路径
	public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";

	//shutdown command
	private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
	
	//the shutdown command received
	private static boolean shutdown = false;
	
	public void await(){
		ServerSocket serverSocket = null;
		int port = 8080;
		try {
			serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);//非 0 的状态码表示异常终止
		}
		//loop wait for a request
		while(!shutdown){
			Socket socket = null;
			InputStream input =null;
			OutputStream output =null;
			try {
				socket = serverSocket.accept();
				input = socket.getInputStream();
				output = socket.getOutputStream();
				//create a request Object and parse
				Request request = new Request(input);
				request.parse();
				//create a response Object
				Response response = new Response(output);
				response.setRequest(request);
				//check if this is a request for a servlet or
				//a static resource
				//a request for servlet begin with "/servlet/"
				if (request.getUri()!=null && request.getUri().startsWith("/servlet/")) {
					ServletProcessor1 processor = new ServletProcessor1();
					processor.process(request, response);
				}else {
					StaticResourceProcessor1 processor=  new StaticResourceProcessor1();
					processor.process(request, response);
				}
				//close socket
				socket.close();
				//check the previous uri is a shutdown command
				shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	
	public static void main(String[] args)
	{
		System.out.println(WEB_ROOT);
		HttpServer1 httpServer = new HttpServer1();
		httpServer.await();
	}
}
