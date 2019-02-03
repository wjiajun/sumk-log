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

import org.yx.conf.AppInfo;
import org.yx.log.ConsoleLog;

public class Appenders {
	private static final String LOG_APPENDER = "sumk.log.appender.";
	private static final String LOG_MODULE = "sumk.log.module.";
	private static List<LogAppender> list = new ArrayList<>(2);

	static boolean console;

	static void init() {
		Map<String, String> appenders = AppInfo.subMap(LOG_APPENDER);
		appenders.forEach((k, p) -> {
			if (p == null || p.isEmpty()) {
				return;
			}
			if (LogAppender.CONSOLE.equals(k)) {
				console = "1".equals(p) || "true".equals(p.toLowerCase());
				return;
			}
			String[] ps = p.split(",", 2);
			if (ps.length != 2) {
				ConsoleLog.getLogger("sumk.log").error("appender [{}] = {} is not valid", k, p);
				return;
			}
			try {
				LogAppender appender = AppenderFactory.create(ps[0], k, ps[1]);
				if (appender == null) {
					return;
				}
				String v = AppInfo.get(LOG_MODULE + appender.name(), null);
				if (v == null) {
					ConsoleLog.getLogger("sumk.log").error("you need config {}=***", LOG_MODULE + appender.name());
					return;
				}
				String[] packs = v.replace('，', ',').split(",");
				List<String> pps = new ArrayList<>();
				for (String pack : packs) {
					pack = pack.trim();

					if ("*".equals(pack) || "root".equals(pack.toLowerCase())) {
						pps = Collections.singletonList("");
						break;
					}
					pack = pack.replace('*', ' ').trim();
					if (pack.length() > 0) {
						pps.add(pack);
					}
				}
				appender.modules(pps);
				appender.start();
				list.add(appender);
			} catch (Throwable e) {
				ConsoleLog.getLogger("sumk.log").error("appender [{}] = {} create failed", k, p);
				return;
			}

		});

	}

	public static boolean print(Message message) {
		boolean output = false;
		for (LogAppender log : list) {
			if (log.print(message)) {
				output = true;
			}
		}
		return output;
	}
}
