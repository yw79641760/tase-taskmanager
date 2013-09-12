package com.softsec.tase.task.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author long.ou
 */
public class FileUtil {

	private static Logger LOGGER = Logger.getLogger(FileUtil.class);

	/**
	 * delete all files in certain directory
	 * 
	 * @param filepath
	 *            file's path of directory
	 */
	public static void delAllFile(String filepath) {
		File file = new File(filepath);
		File[] files = file.listFiles();
		String path = null;
		if (files != null) {
			for (File f : files) {
				if (f.isFile()) {
					if (!f.delete()) {
						LOGGER.error("deleting" + f.getName() + "failed.");
					} else if (f.isDirectory()) {
						path = f.getAbsolutePath();
						delAllFile(path);
					}
				}
			}
		}
		if (file.delete()) {
			LOGGER.info(filepath + " has been deleted.");
		} else {
			LOGGER.warn(filepath + " deleting --> failed.");
		}
	}

	public synchronized static void move(String from, String to) throws FileNotFoundException {
		File toFile = new File(to);
		InputStream in = null;
		try {
			in = new FileInputStream(new File(from));
			delAllFile(to);
			Directory.ensureDir(toFile.getParentFile());
			IoTool.writeStream(in, to);
		} catch (Exception e) {
			LOGGER.warn("Move file[" + from +"] to [" + to + "] Error : " + e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(in);
		}
		delAllFile(from);
	}

	public static void copy(String from, String to) throws FileNotFoundException {
		File toFile = new File(to);
		InputStream in = null;
		try {
			in = new FileInputStream(new File(from));
			Directory.ensureDir(toFile.getParentFile());
			IoTool.writeStream(in, to);
		} catch (Exception e) {
			LOGGER.warn("Copy file[" + from +"] to [" + to + "] Error : " + e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public static String newestFile(List<String> files, String regx) {
		List<String> names = new ArrayList<String>();
		Matcher matcher = null;
		Map<String, String> map = new HashMap<String, String>();
		for (String filename : files) {
			matcher = Pattern.compile(regx).matcher(filename);
			while (matcher.find()) {
				names.add(matcher.group());
				map.put(matcher.group(), filename);
			}
		}
		if (!names.isEmpty()) {
			Collections.sort(names);
			return map.get(names.get(names.size() - 1));
		} else {
			return null;
		}
	}

	public static String formatFileSeparator(String fileAbsolutePath) {
		return fileAbsolutePath.replaceAll("\\\\", "/");
	}
}
