package ex05.pyrmont.core;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import org.apache.catalina.Container;
import org.apache.catalina.Loader;
import org.apache.catalina.DefaultContext;

//容器中加载 servlet 的任务被分配给了 Loader 实现。在该程序中 SimpleLoader
//就是一个 Loader 实现。它知道如何定位一个 servlet，并且通过 getClassLoader
//获得一个 java.lang.ClassLoader 实例用来查找 servlet 类位置。SimpleLoader
//定义了 3 个变量，第一个是 WEB_ROOT 用来指明在哪里查找 servlet 类。
public class SimpleLoader implements Loader {

  public static final String WEB_ROOT =
    System.getProperty("user.dir") + File.separator  + "webroot";

  ClassLoader classLoader = null;
  Container container = null;

  public SimpleLoader() {
    try {
      URL[] urls = new URL[1];
      URLStreamHandler streamHandler = null;
      File classPath = new File(WEB_ROOT);
      String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString() ;
      urls[0] = new URL(null, repository, streamHandler);//指向指定的类库
      classLoader = new URLClassLoader(urls);
    }
    catch (IOException e) {
      System.out.println(e.toString() );
    }


  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public Container getContainer() {
    return container;
  }

  public void setContainer(Container container) {
    this.container = container;
  }

  public DefaultContext getDefaultContext() {
    return null;
  }

  public void setDefaultContext(DefaultContext defaultContext) {
  }

  public boolean getDelegate() {
    return false;
  }

  public void setDelegate(boolean delegate) {
  }

  public String getInfo() {
    return "A simple loader";
  }

  public boolean getReloadable() {
    return false;
  }

  public void setReloadable(boolean reloadable) {
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
  }

  public void addRepository(String repository) {
  }

  public String[] findRepositories() {
    return null;
  }

  public boolean modified() {
    return false;
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
  }

}