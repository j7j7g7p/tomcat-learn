/* explains Tomcat's default container */
package com.my.server.http.ex04.pyrmont.startup;
import org.apache.catalina.connector.http.HttpConnector;

import com.my.server.http.ex04.pyrmont.core.SimpleContainer;

public final class Bootstrap {
  public static void main(String[] args) {
    HttpConnector connector = new HttpConnector();
    SimpleContainer container = new SimpleContainer();
    connector.setContainer(container);
    try {
      connector.initialize();//初始化HttpConnector的关联套接字
      
      connector.start();//线程开启之后会运行线程的run方法
      //连接器的线程开始之后会维护一堆线程处理器池
      //connector运行run方法时候会获取适当的处理器(栈内或新建的处理器)来分配套接字(不进行处理)

      // make the application wait until we press any key.
      System.in.read();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}