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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.yx.conf.AppInfo;
import org.yx.exception.CodeException;

import com.google.gson.stream.JsonWriter;

public class UnionLogAppender extends FileAppender {

	private UnionLogDao dao = new UnionLogDao();
	private Function<String, String> logNameParser;

	public void setLogNameParser(Function<String, String> logNameParser) {
		this.logNameParser = logNameParser;
	}

	public UnionLogAppender() {
		super("union");
		this.maxClearSize = -1;
		this.interval = AppInfo.getInt("sumk.log.union.interval", 5000);
	}

	@Override
	public boolean afterStarted() {
		dao.start();
		return true;
	}

	@Override
	public void close() throws Exception {
		super.close();
		dao.reset();
	}

	@Override
	protected void clean() throws IOException {
		dao.reset();
	}

	@Override
	protected void output(List<LogObject> list) throws Exception {
		for (LogObject logObject : list) {
			dao.append(toUnionLog(logObject));
		}
	}

	protected String toUnionLog(LogObject log) throws Exception {
		StringWriter stringWriter = new StringWriter();
		JsonWriter writer = new JsonWriter(stringWriter);
		writer.setSerializeNulls(false);
		writer.beginObject();
		writer.name("sn").value(log.sn);
		writer.name("test").value(log.test ? 1 : 0);
		String body = log.body;
		writer.name("logBody").value(body);
		writer.name("threadName").value(log.threadName);
		writer.name("level").value(log.methodLevel.name());
		writer.name("host").value(AppInfo.getIp());
		writer.name("groupId").value(AppInfo.groupId());
		writer.name("appId").value(AppInfo.appId());
		writer.name("pid").value(AppInfo.pid());
		if (log.exception != null) {
			writer.name("exception").value(log.exception.getClass().getName());
			StringWriter sw = new StringWriter();
			PrintWriter w = new PrintWriter(sw);
			log.exception.printStackTrace(w);
			writer.name("exceptiondetail").value(sw.toString());
			if (CodeException.class.isInstance(log.exception)) {
				writer.name("exceptioncode").value(CodeException.class.cast(log.exception).getCode());
			}
		}
		if (log.codeLine != null) {
			writer.name("className").value(log.codeLine.className);
			writer.name("methodName").value(log.codeLine.methodName);
			writer.name("lineNumber").value(log.codeLine.lineNumber);
		}
		writer.endObject();
		writer.flush();
		writer.close();
		String json = stringWriter.toString();
		String logName = log.logger.getName();
		if (logNameParser != null) {
			logName = logNameParser.apply(logName);
		}

		StringBuilder sb = new StringBuilder().append(logName.replace('#', '$')).append('#')
				.append(log.logDate.toDate().getTime()).append('#').append(json);
		return sb.toString();
	}

	protected boolean onStart(Map<String, String> map) {
		Appenders.consoleLog.debug(name + " = {}", map);
		config(map);
		return true;
	}

}
