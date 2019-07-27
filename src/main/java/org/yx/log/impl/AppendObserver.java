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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.yx.conf.AppInfo;
import org.yx.conf.SystemConfig;
import org.yx.util.CollectionUtil;

public class AppendObserver implements Consumer<SystemConfig> {

	@Override
	public void accept(SystemConfig info) {
		Appenders.setConsole();
		Map<String, String> newAppenders = AppInfo.subMap(Appenders.LOG_APPENDER);
		for (LogAppender append : Appenders.logAppenders) {
			newAppenders.remove(append.name());
			String v = AppInfo.getLatin(Appenders.LOG_APPENDER + append.name());
			if (v == null || v.isEmpty()) {
				append.config(null);
				continue;
			}
			Map<String, String> map = CollectionUtil.loadMapFromText(v, ";", ":");
			append.config(map);
		}

		if (newAppenders.isEmpty()) {
			return;
		}
		List<LogAppender> appends = new ArrayList<>();
		for (LogAppender append : Appenders.logAppenders) {
			appends.add(append);
		}
		if (Appenders.isStarted()) {
			Appenders.consoleLog.info("find new appends:{}", newAppenders);
		}
		newAppenders.forEach((k, p) -> {
			LogAppender appender = Appenders.startAppender(k, p);
			if (appender != null) {
				appends.add(appender);
			}

		});
		Appenders.logAppenders = appends.toArray(new LogAppender[appends.size()]);
	}

}
