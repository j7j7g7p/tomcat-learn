package com.my.server.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketTest
{
	public static void main(String[] args) throws UnknownHostException, IOException
	{
		try {
			System.out.println("开始发请求！");
			Socket socket = new Socket("127.0.0.1", 8080);
			OutputStream os = socket.getOutputStream();
			boolean autoFlush = true;
			PrintWriter out = new PrintWriter(os, autoFlush);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// send an HTTP request to the web server
			out.println("GET /index.jsp HTTP/1.1");
			out.println("HOST :localhost:8080");
			out.println("Connection:Close");
			out.println();
			out.flush();
			// read the response
//			in.readLine();
			boolean loop = true;
			StringBuffer sb = new StringBuffer(8096);
			while (loop) {
				if (in.ready()) {
					int i = 0;
					while (i != -1) {
						i = in.read();
						sb.append((char) i);
					}
					loop = false;
				}
				Thread.sleep(50);
			}
			//display the response to console
			System.out.println(sb.toString());
			socket.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
//	    //客户端 
//	    //1、创建客户端Socket，指定服务器地址和端口 
//	    Socket socket =new Socket("localhost",10086); 
//	    //2、获取输出流，向服务器端发送信息 
//	    OutputStream os = socket.getOutputStream();//字节输出流 
//	    PrintWriter pw =new PrintWriter(os);//将输出流包装成打印流 
//	    pw.write("用户名：admin；密码：123"); 
//	    pw.flush(); 
//	    socket.shutdownOutput(); 
//	    //3、获取输入流，并读取服务器端的响应信息 
//	    InputStream is = socket.getInputStream(); 
//	    BufferedReader br = new BufferedReader(new InputStreamReader(is)); 
//	    String info = null; 
//	    while((info=br.readLine())!=null){ 
//	     System.out.println("我是客户端，服务器说："+info); 
//	    } 
//	     
//	    //4、关闭资源 
//	    br.close(); 
//	    is.close(); 
//	    pw.close(); 
//	    os.close(); 
//	    socket.close(); 
	}
}
