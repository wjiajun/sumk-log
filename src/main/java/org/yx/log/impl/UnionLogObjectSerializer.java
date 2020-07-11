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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.yx.common.sumk.UnsafeStringWriter;
import org.yx.conf.AppInfo;
import org.yx.exception.CodeException;
import org.yx.util.ExceptionUtil;
import org.yx.util.UUIDSeed;

import com.google.gson.stream.JsonWriter;

public class UnionLogObjectSerializer implements Function<List<LogObject>, String> {

	private final String appId;
	private Function<String, String> logNameParser;
	private static final String LINE_SPLIT = "\n";

	public void setLogNameParser(Function<String, String> logNameParser) {
		this.logNameParser = Objects.requireNonNull(logNameParser);
	}

	public UnionLogObjectSerializer() {
		this.appId = AppInfo.appId(null);
	}

	public Function<String, String> getLogNameParser() {
		return logNameParser;
	}

	@Override
	public String apply(List<LogObject> logs) {
		StringBuilder sb = new StringBuilder(128);
		for (LogObject log : logs) {
			try {
				String logName = log.loggerName;
				if (logNameParser != null) {
					logName = logNameParser.apply(logName);
				}

				sb.append(logName.replace('#', '$')).append('#');
				this.appendLogObject(sb, log, appId);
				sb.append(LINE_SPLIT);
			} catch (IOException e) {
				LogAppenders.consoleLog.error("数据解析出错", e);
			}
			;
		}
		return sb.toString();
	}

	protected void appendLogObject(StringBuilder sb, LogObject log, String appId) throws IOException {
		UnsafeStringWriter stringWriter = new UnsafeStringWriter(sb);
		JsonWriter writer = new JsonWriter(stringWriter);
		writer.setSerializeNulls(false);
		writer.beginObject();
		writer.name("logDate").value(log.logDate.to_yyyy_MM_dd_HH_mm_ss_SSS());
		writer.name("logName").value(log.loggerName);
		writer.name("_id").value(UUIDSeed.seq18());
		writer.name("userId").value(log.userId());
		writer.name("traceId").value(log.traceId());
		writer.name("spanId").value(log.spanId());
		writer.name("test").value(log.isTest() ? 1 : 0);
		String body = log.body;
		writer.name("body").value(body);
		writer.name("threadName").value(log.threadName);
		writer.name("level").value(log.methodLevel.name());
		writer.name("host").value(AppInfo.getLocalIp());
		if (appId != null) {
			writer.name("appId").value(appId);
		}
		writer.name("pid").value(AppInfo.pid());
		if (log.exception != null) {
			writer.name("exception").value(log.exception.getClass().getName());
			writer.name("exceptiondetail");
			ExceptionUtil.printStackTrace(sb, log.exception);
			if (CodeException.class.isInstance(log.exception)) {
				writer.name("exceptioncode").value(CodeException.class.cast(log.exception).getCode());
			}
		}
		if (log.codeLine != null) {
			writer.name("className").value(log.codeLine.className);
			writer.name("methodName").value(log.codeLine.methodName);
			writer.name("lineNumber").value(log.codeLine.lineNumber);
		}
		Map<String, String> attachs = log.attachments();
		if (attachs != null) {
			for (Map.Entry<String, String> en : attachs.entrySet()) {
				writer.name("u_" + en.getKey()).value(en.getValue());
			}
		}
		writer.endObject();
		writer.flush();
		writer.close();
	}
}
