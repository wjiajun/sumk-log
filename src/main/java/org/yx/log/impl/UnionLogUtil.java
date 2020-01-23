/**
 * Copyright (C) 2016 - 2030 youtongluan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yx.log.impl;

import java.io.File;

import org.yx.conf.AppInfo;
import org.yx.log.ConsoleLog;

public class UnionLogUtil {

	private static File logRoot;
	static final String SELF_LOG_NAME = "sumk.log.union";

	private static final String LOGING = "loging";
	private static final String LOGED = "loged";
	private static final String ERROR = "error";

	public static void move2Loged(File logging) {
		if (logging == null) {
			return;
		}
		File dest = new File(getLogedPath(), logging.getName());
		File p = dest.getParentFile();
		if (!p.exists()) {
			p.mkdirs();
		}
		if (!logging.renameTo(dest)) {
			ConsoleLog.get(SELF_LOG_NAME).error(logging.getName() + " move to loged folder failed");
		}
	}

	public static void move2Error(File loged) {
		if (loged == null) {
			return;
		}
		File dest = new File(getErrorPath(), loged.getName());
		File p = dest.getParentFile();
		if (!p.exists()) {
			p.mkdirs();
		}
		if (!loged.renameTo(dest)) {
			ConsoleLog.get(SELF_LOG_NAME).error(loged.getName() + " move to error path failed");
		}
	}

	public static File getErrorPath() {
		return new File(getLogRoot(), ERROR);
	}

	public static File getLogedPath() {
		return new File(getLogRoot(), LOGED);
	}

	public static File getLogingPath() {
		return new File(getLogRoot(), LOGING);
	}

	private static File getLogRoot() {
		if (logRoot != null) {
			return logRoot;
		}
		logRoot = getDefaultLoginPath();
		ConsoleLog.get(SELF_LOG_NAME).info("logRoot:" + logRoot.getAbsolutePath());
		return logRoot;
	}

	private static File getDefaultLoginPath() {
		String path = AppInfo.get("sumk.log.union.path");
		if (path != null && path.length() > 2) {
			return new File(path);
		}
		if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
			File f = new File("D:");
			if (f.exists() && f.getFreeSpace() > 2000) {
				return new File(f, "log\\sumk");
			}
			return new File("C:\\log\\sumk");
		}
		return new File("/log/sumk");
	}

}
