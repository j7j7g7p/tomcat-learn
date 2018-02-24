package com.my.server.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketTest
{
	public static void main(String[] args) throws IOException
	{
		try {
			ServerSocket serverSocket = new ServerSocket(8080);
			Socket accept = serverSocket.accept();
			System.out.println("获得请求：" + accept.getInetAddress().toString() + ":" + accept.getLocalPort());
			BufferedReader in = new BufferedReader(new InputStreamReader(accept.getInputStream()));
			String readString = null;
			while ((readString = in.readLine()) != null) {
				System.out.println(readString);
			}
			accept.shutdownInput();
			OutputStream out = accept.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
			String response = "serverSocket got your message!\n\r";
			writer.write(response);
			writer.flush();
			System.out.println("回馈：" + response);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// /**
		// * 基于TCP协议的Socket通信，实现用户登录，服务端
		// */
		// // 1、创建一个服务器端Socket，即ServerSocket，指定绑定的端口，并监听此端口
		// ServerSocket serverSocket = new ServerSocket(10086);//
		// 1024-65535的某个端口
		// // 2、调用accept()方法开始监听，等待客户端的连接
		// Socket socket = serverSocket.accept();
		// // 3、获取输入流，并读取客户端信息
		// InputStream is = socket.getInputStream();
		// InputStreamReader isr = new InputStreamReader(is);
		// BufferedReader br = new BufferedReader(isr);
		// String info = null;
		// while ((info = br.readLine()) != null) {
		// System.out.println("我是服务器，客户端说：" + info);
		// }
		// socket.shutdownInput();// 关闭输入流
		// // 4、获取输出流，响应客户端的请求
		// OutputStream os = socket.getOutputStream();
		// PrintWriter pw = new PrintWriter(os);
		// pw.write("欢迎您！");
		// pw.flush();
		//
		// // 5、关闭资源
		// pw.close();
		// os.close();
		// br.close();
		// isr.close();
		// is.close();
		// socket.close();
		// serverSocket.close();
	}
}
