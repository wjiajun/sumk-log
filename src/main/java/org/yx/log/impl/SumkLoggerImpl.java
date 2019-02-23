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

import org.yx.log.LogLevel;
import org.yx.log.Loggers;
import org.yx.log.SumkLogger;

public class SumkLoggerImpl extends SumkLogger {
	private static final long serialVersionUID = 1;

	public SumkLoggerImpl(String module) {
		super(module);
	}

	@Override
	protected void output(LogLevel methodLevel, String format, Object... arguments) {
		try {
			String msg = this.buildMessage(format, arguments);
			LogObject logObject = new LogObject(methodLevel, msg, null, this);

			if (!Appenders.offer(logObject) || Appenders.console) {
				System.out.print(LogObjectUtil.plainMessage(logObject));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void output(LogLevel methodLevel, String msg, Throwable e) {
		try {
			LogObject logObject = new LogObject(methodLevel, msg, e, this);
			if (!Appenders.offer(logObject) || Appenders.console) {
				System.err.print(LogObjectUtil.plainMessage(logObject));
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	@Override
	protected Loggers loggers() {
		return SumkLoggerFactory.loggers;
	}

}
