package ex06.pyrmont.startup;

import java.sql.Timestamp;

public class Test {
	public static void main(String[] args) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String time = timestamp.toString();
		System.out.println(time);
		System.out.println(timestamp.getTime());
				
	}
}
