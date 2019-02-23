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
import java.util.Observable;
import java.util.Observer;

import org.yx.conf.AppInfo;
import org.yx.util.CollectionUtil;

public class AppendObserver implements Observer {

	@Override
	public void update(Observable o, Object arg) {
		Appenders.setConsole();
		List<LogAppender> appends = Appenders.list;
		Map<String, String> newAppenders = AppInfo.subMap(Appenders.LOG_APPENDER);
		for (LogAppender append : appends) {
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
		List<LogAppender> appendList = new ArrayList<>(appends);
		Appenders.consoleLog.info("find new appends:{}", appendList);
		newAppenders.forEach((k, p) -> {
			LogAppender appender = Appenders.startAppender(k, p);
			if (appender != null) {
				appendList.add(appender);
			}

		});
		Appenders.list = appendList;
	}

}
