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

import org.slf4j.Marker;
import org.yx.log.LogLevel;
import org.yx.log.LogSettings;
import org.yx.log.SumkLogger;

public class SumkLoggerImpl extends SumkLogger {

	public SumkLoggerImpl(String module) {
		super(module);
	}

	@Override
	protected void output(Marker marker, LogLevel methodLevel, String format, Object... arguments) {
		try {
			String msg = this.buildMessage(format, arguments);
			LogObject logObject = LogObject.create(marker, methodLevel, msg, null, this);

			if (!LogAppenders.offer(logObject) || LogSettings.consoleEnable()) {
				System.out.print(LogObjectHelper.plainMessage(logObject, LogSettings.showAttach()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void output(Marker marker, LogLevel methodLevel, String msg, Throwable e) {
		try {
			LogObject logObject = LogObject.create(marker, methodLevel, msg, e, this);
			if (!LogAppenders.offer(logObject) || LogSettings.consoleEnable()) {
				System.err.print(LogObjectHelper.plainMessage(logObject, LogSettings.showAttach()));
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

}
