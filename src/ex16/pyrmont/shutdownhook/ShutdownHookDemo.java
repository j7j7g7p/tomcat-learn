package ex16.pyrmont.shutdownhook;

public class ShutdownHookDemo {
	public void start() {
		System.out.println("Runtime.getRuntime().addShutdownHook(hook)");
		ShutDownHook hook = new ShutDownHook();
		Runtime.getRuntime().addShutdownHook(hook);
	}

	public static void main(String[] args) {
		ShutdownHookDemo demo = new ShutdownHookDemo();
		demo.start();
		try {
			System.in.read();
		} catch (Exception e) {
		}
	}
}

class ShutDownHook extends Thread {

	@Override
	public void run() {
		System.out.println("ShutDown...");
	}

}
