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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.yx.common.ClassScaner;

public class AppenderFactory {

	public static synchronized void init()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Collection<Class<? extends LogAppender>> clzs = ClassScaner.listSubClassesInSamePackage(LogAppender.class);
		for (Class<? extends LogAppender> clz : clzs) {
			LogAppender append = clz.newInstance();
			map.put(append.name().toLowerCase(), append);
			Appenders.consoleLog.trace("logger {} found", append.name());
		}
	}

	static final Map<String, LogAppender> map = new HashMap<>();

	public static synchronized LogAppender start(String name, Map<String, String> configMap) throws Exception {
		name = name.toLowerCase();
		LogAppender append = map.get(name);
		if (append == null) {
			Appenders.consoleLog.error("{} cannot find appender", name);
			return null;
		}
		if (!append.start(configMap)) {
			Appenders.consoleLog.error("{} started failed,value is {}", name, configMap);
			return null;
		}
		return append;
	}

	public static synchronized void registeAppender(LogAppender m) {
		map.put(m.name().toLowerCase(), m);
	}

	public static synchronized void unRegisteAppender(String name) {
		map.remove(name.toLowerCase());
	}
}
