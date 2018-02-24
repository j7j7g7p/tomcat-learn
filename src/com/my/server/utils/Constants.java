package com.my.server.utils;

import java.io.File;

public class Constants
{
//	public static final String WEB_ROOT = "bin\\com\\my\\server\\container";
//	public static final String WEB_ROOT = "server\\com\\my\\server\\container";
	public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";
}
