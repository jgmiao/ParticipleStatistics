package com.secneo.participle.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class WriteFile {

	public static void write(String content, String path) {
		try {
			File file = new File(path);
			File route = null;
			String systemName = System.getProperties().getProperty("os.name");
			if (systemName.startsWith("Windows") || systemName.startsWith("windows")) {
				route = new File(path.substring(0, path.lastIndexOf("\\"))); 
			} else {
				route = new File(path.substring(0, path.lastIndexOf("/")));
			}
			if (!route.exists() && !route.isDirectory()) {
				route.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(path);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			osw.write(content);
			osw.flush();
			osw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}