package myadmin;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.modeler.Registry;
//http://localhost:8080/myadmin/myAdmin?action=listAllContexts
public class MyAdminServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Registry registry;
	private MBeanServer mBeanServer;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("doGet...");
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		System.out.println("getWriter end ...");
		if (registry==null||mBeanServer==null) {
			out.print(" Registry or MBeanServer is not available ~");
			return;
		}
		out.println("<html><head></head><body>");
		String action = req.getParameter("action");
		System.out.println("action:"+action);
		if ("listAllManagedBeans".equals(action)) {
			listAllManagedBeans(out);
		}else if ("listAllContexts".equals(action)) {
			listAllContexts(out);
		}else if ("removeContext".equals(action)) {
			String contextObjectName = req.getParameter("contextObjectName");
			System.out.println("contextObjectName:"+contextObjectName);
			removeContext(contextObjectName,out);
		}else {
			out.println("Invalid command");
			
		}
	}
	private void removeContext(String contextObjectName, PrintWriter out) {
		try {
			ObjectName objectName = new ObjectName("Catalina:type=MBeanFactory");
			if (objectName!=null) {
				String operation = "removeContext";
				String[] params = new String[1];
				params[0] = contextObjectName;
				String[] signature = {"java.lang.String"};
				mBeanServer.invoke(objectName, operation, params, signature);
				out.print("context removed");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			out.println(e.toString());
		}
	}
	private void listAllContexts(PrintWriter out) {
		try {
			System.out.println("listAllContexts;开始");
			ObjectName objectName = new ObjectName("*:type=*");
			Set<ObjectName> set = mBeanServer.queryNames(objectName, null);
			Iterator<ObjectName> iterator = set.iterator();
			boolean hasNext = iterator.hasNext();
			System.out.println("迭代开始："+hasNext);
			while (hasNext) {
				ObjectName obj = iterator.next();
				out.print(obj+" <a href=?action=removeContext&contextObjectName="+URLEncoder.encode(obj.toString(),"UTF-8")+
						">remove</a></br>");
				System.out.println(URLEncoder.encode(obj.toString(),"UTF-8"));
				hasNext = iterator.hasNext();
			}
			
			System.out.println("迭代结束：");
		} catch (Exception e) {
			System.out.println("异常：");
			out.println(e.toString());
		}
	}
	private void listAllManagedBeans(PrintWriter out) {
		String[] findManagedBeans = registry.findManagedBeans();
		for (int i = 0; i < findManagedBeans.length; i++) {
			out.print(findManagedBeans[i]+"</br>");
		}
		out.print("</body></html>");
	}
	@Override
	public void init() throws ServletException {
		System.out.println("init");
		registry = (Registry) getServletContext().getAttribute("org.apache.catalina.Registry");
		if (registry==null) {
			System.out.println(" Registry is not available ~");
		}
		mBeanServer = (MBeanServer) getServletContext().getAttribute("org.apache.catalina.MBeanServer");
		if (mBeanServer==null) {
			System.out.println(" MBeanServer is not available ~");
			return;
		}
	}
	
	

}
