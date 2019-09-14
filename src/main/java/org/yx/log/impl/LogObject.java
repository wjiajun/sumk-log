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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Marker;
import org.yx.common.context.ActionContext;
import org.yx.common.context.LogContext;
import org.yx.conf.AppInfo;
import org.yx.log.LogKits;
import org.yx.log.LogLevel;
import org.yx.log.SumkLogger;
import org.yx.util.SumkDate;

public final class LogObject {
	public static final char LN = '\n';
	public static final Charset CHARSET = StandardCharsets.UTF_8;

	final SumkDate logDate;

	final LogLevel methodLevel;

	final String body;

	final String threadName;

	final Throwable exception;

	final SumkLogger logger;

	final CodeLine codeLine;

	final LogContext logContext;

	public LogObject(Marker marker, LogLevel methodLevel, String message, Throwable e, SumkLogger logger) {
		this.methodLevel = methodLevel;
		this.body = LogKits.clipIfNecessary(message);
		this.exception = e;
		this.logDate = SumkDate.now();
		this.logger = logger;
		this.logContext = ActionContext.get().logContext();
		threadName = Thread.currentThread().getName();
		if (AppInfo.getBoolean("sumk.log.codeline", true)) {
			this.codeLine = CodeLineKit.parse(marker, logger.getName());
		} else {
			this.codeLine = null;
		}
	}

	public String spanId() {
		return logContext.spanId;
	}

	public String traceId() {
		return logContext.traceId;
	}

	public String userId() {
		return logContext.userId;
	}

	public boolean isTest() {
		return logContext.test;
	}

	public Map<String, String> attachments() {
		return logContext.unmodifiedAttachs();
	}

	public SumkDate getLogDate() {
		return logDate;
	}

	public LogLevel getMethodLevel() {
		return methodLevel;
	}

	public String getBody() {
		return body;
	}

	public String getThreadName() {
		return threadName;
	}

	public Throwable getException() {
		return exception;
	}

	public SumkLogger getLogger() {
		return logger;
	}

	public CodeLine getCodeLine() {
		return codeLine;
	}

	public LogContext getLogContext() {
		return logContext;
	}

}
