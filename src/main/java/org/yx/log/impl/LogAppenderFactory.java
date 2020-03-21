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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.yx.bean.Loader;
import org.yx.common.scaner.ClassScaner;

public final class LogAppenderFactory {

	private static final ConcurrentMap<String, LogAppender> map = new ConcurrentHashMap<>();

	static synchronized void init() throws Exception {
		Collection<Class<? extends LogAppender>> clzs = ClassScaner.subClassesInSameOrSubPackage(LogAppender.class);
		for (Class<? extends LogAppender> clz : clzs) {
			try {
				LogAppender append = Loader.newInstance(clz);
				map.put(append.name(), append);
				LogAppenders.consoleLog.trace("logger {} found", append.name());
			} catch (Exception e) {
				LogAppenders.consoleLog.error(clz.getName() + " init failed.It must have a non parameter constructor",
						e);
			}
		}
	}

	static LogAppender start(String name, Map<String, String> configMap) throws Exception {
		LogAppender append = map.get(name);
		if (append == null) {
			LogAppenders.consoleLog.error("{} cannot find appender", name);
			return null;
		}
		if (!append.start(configMap)) {
			LogAppenders.consoleLog.error("{} started failed,value is {}", name, configMap);
			return null;
		}
		return append;
	}

	public static void registeAppender(LogAppender m) {
		if (m == null) {
			return;
		}
		map.put(m.name(), m);
	}

	public static void remove(String name) {
		if (name == null || name.isEmpty()) {
			return;
		}
		map.remove(name);
	}

	public static LogAppender getAppender(String name) {
		return map.get(name);
	}

	public static Set<String> appenderNames() {
		return Collections.unmodifiableSet(map.keySet());
	}
}
