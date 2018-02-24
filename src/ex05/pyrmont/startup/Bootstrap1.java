package ex05.pyrmont.startup;

import ex05.pyrmont.core.SimpleLoader;
import ex05.pyrmont.core.SimpleWrapper;
import ex05.pyrmont.valves.ClientIPLoggerValve;
import ex05.pyrmont.valves.HeaderLoggerValve;
import org.apache.catalina.Loader;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.http.HttpConnector;

public final class Bootstrap1 {
  public static void main(String[] args) {

/* call by using http://localhost:8080/ModernServlet,
   but could be invoked by any name */

    HttpConnector connector = new HttpConnector();
    Wrapper wrapper = new SimpleWrapper();
    wrapper.setServletClass("ModernServlet");
    Loader loader = new SimpleLoader();
    Valve valve1 = new HeaderLoggerValve();
    Valve valve2 = new ClientIPLoggerValve();

    wrapper.setLoader(loader);
    ((Pipeline) wrapper).addValve(valve1);
    ((Pipeline) wrapper).addValve(valve2);
//    connector.setAddress("127.1.1.1");

    connector.setContainer(wrapper);

    try {
      connector.initialize();//建立套接字
      connector.start();//开启线程，创建处理器池
      //	|
      //	|
      //	|
      //run()等待接受Socket
      //	|
      //	|
      //	|
      //交给HttpProcessor处理（HttpProcessor栈池中弹出来）分配套接字assign（socket）
      //	|
      //	|
      //	|
      //HttpProcessor 组装request response 对象
      //	|
      //	|
      //	|
      //交给连接器的容器处理，connector.getContainer().invoke(request, response);
      //connector的容器是在主程序中实例的
      //	|
      //	|
      //	|
      //流水线调用pipeline.invoke(request, response);
      //	|
      //	|
      //	|
      //流水线阀门接头流水线内部类SimplePipelineValveContext调用 (new SimplePipelineValveContext()).invokeNext(request, response);
      //  	|
      //	|
      //	|
      //依次调用所有阀门基础阀门是流水实例的时候实例，其他阀门在wrapper实例之后添加依次添加，阀门先进后出
      
      // make the application wait until we press a key.
      System.in.read();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}