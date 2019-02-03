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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AppenderFactory {

	public static Map<String, Method> map = new HashMap<>(0);

	public static LogAppender create(String type, String name, String path) throws Exception {
		switch (type.toLowerCase()) {
		case "day":
			return DayRollingFileAppender.create(name, path);
		case "month":
			return MonthRollingFileAppender.create(name, path);
		default:
			Method m = map.get(type);
			if (m == null) {
				return null;
			}
			return (LogAppender) m.invoke(name, path);
		}
	}
}
