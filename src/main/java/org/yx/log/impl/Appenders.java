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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.yx.conf.AppInfo;
import org.yx.log.ConsoleLog;
import org.yx.util.CollectionUtil;
import org.yx.util.StringUtil;

public class Appenders {
	static final String LOG_APPENDER = "s.log.";
	public static final String MODULE = "module";
	public static final String PATH = "path";
	static List<LogAppender> list = Collections.emptyList();
	private static boolean started;

	static boolean console = true;
	static final Logger consoleLog = ConsoleLog.getLogger("sumk.log");

	static void setConsole() {
		console = AppInfo.getBoolean("sumk.log.console", true);
	}

	static LogAppender startAppender(String name, String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		value = StringUtil.toLatin(value);
		try {
			return AppenderFactory.start(name, CollectionUtil.loadMapFromText(value, ";", ":"));
		} catch (Throwable e) {
			consoleLog.error("appender [{}] = {} create failed", name, value);
			return null;
		}
	}

	static synchronized void init() {
		if (started) {
			return;
		}
		started = true;
		try {
			AppenderFactory.init();
			setConsole();
			Map<String, String> appenders = AppInfo.subMap(LOG_APPENDER);
			List<LogAppender> temp = new ArrayList<>(2);
			appenders.forEach((k, p) -> {
				LogAppender appender = startAppender(k, p);
				if (appender != null) {
					temp.add(appender);
				}

			});
			list = temp;

			AppInfo.addObserver(new AppendObserver());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static boolean offer(LogObject logObject) {
		boolean output = false;
		for (LogAppender log : list) {
			if (log.offer(logObject)) {
				output = true;
			}
		}
		return output;
	}
}
