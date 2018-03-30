package com.galaxyyao.yuri_dbtoy.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigUtil {
	private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

	public static Config getConfig() {
		Config conf = null;
		try {
			String basePath = getBasePath();
			logger.info("Current base path:" + basePath);
			String configPath = basePath + File.separator + "application.conf";
			File file = new File(configPath);
			if (file.exists()) {
				conf = ConfigFactory.parseFile(file);
				return conf;
			}
		} catch (IOException | URISyntaxException e) {
			conf = ConfigFactory.load();
			return conf;
		}
		conf = ConfigFactory.load();
		return conf;
	}

	private static String getBasePath() throws IOException, URISyntaxException {
		// 对于非静态类使用this.getClass()
		// CodeSource codeSource =
		// this.getClass().getProtectionDomain().getCodeSource();
		CodeSource codeSource = ConfigUtil.class.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			logger.error("codeSource is null");
		}
		URL location = codeSource.getLocation();
		if (location == null) {
			logger.error("location is null");
		}
		File source = null;
		URLConnection connection = location.openConnection();
		if (connection instanceof JarURLConnection) {
			JarFile jarFile = ((JarURLConnection) connection).getJarFile();
			String name = jarFile.getName();
			// name类似于:/project-name/target/project-name-1.0.0.jar!/BOOT-INF/classes
			int separator = name.indexOf("!/");
			if (separator > 0) {
				name = name.substring(0, separator);
			}
			source = new File(name);
		} else {
			source = new File(location.toURI().getPath());
		}
		if (source == null || !source.exists()) {
			logger.error("source is null");
		}
		String path = source.getParentFile().getAbsoluteFile().getPath();
		return path;
	}
}
