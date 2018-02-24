package org.apache.catalina.core;

import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;
import org.apache.catalina.ValveContext;
import org.apache.catalina.util.StringManager;

public final class StandardValveContext implements ValveContext {
	protected static StringManager sm = StringManager.getManager(Constants.Package);
	protected String info = "org.apache.catalina.core.StandardValveContext/1.0";
	protected int stage = 0;
	protected Valve basic = null;
	protected Valve valves[] = null;

	public String getInfo() {
		return info;
	}

	public final void invokeNext(Request request, Response response) throws IOException, ServletException {
		int subscript = stage;
		stage = stage + 1;
		// Invoke the requested Valve for the current request thread
		if (subscript < valves.length) {
			valves[subscript].invoke(request, response, this);
		} else if ((subscript == valves.length) && (basic != null)) {
			basic.invoke(request, response, this);
		} else {
			throw new ServletException(sm.getString("standardPipeline.noValve"));
		}
	}

	void set(Valve basic, Valve valves[]) {
		stage = 0;
		this.basic = basic;
		this.valves = valves;
	}
}