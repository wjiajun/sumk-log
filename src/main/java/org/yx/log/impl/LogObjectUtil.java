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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.yx.common.ThreadContext;
import org.yx.log.LogKits;

public class LogObjectUtil {

	public static String plainMessage(LogObject logObject, boolean showAttachs) {
		StringBuilder sb = createStringBuilder(logObject).append(logObject.methodLevel).append(" ");
		if (logObject.codeLine != null) {
			sb.append(LogKits.shorter(logObject.codeLine.className, logObject.logger.maxLogNameLength())).append(':')
					.append(logObject.codeLine.lineNumber);
		} else {
			sb.append(LogKits.shorter(logObject.logger.getName(), logObject.logger.maxLogNameLength()));
		}
		if (showAttachs && logObject.attachments != null) {
			sb.append(" #").append(Arrays.toString(logObject.attachments));
		}

		sb.append(" - ").append(logObject.body).append(LogObject.LN);
		if (logObject.exception != null) {
			StringWriter sw = new StringWriter();
			PrintWriter w = new PrintWriter(sw);
			logObject.exception.printStackTrace(w);
			sb.append(sw.toString()).append(LogObject.LN);
		}
		return sb.toString();
	}

	private static StringBuilder createStringBuilder(LogObject logObject) {
		StringBuilder sb = new StringBuilder();
		String sn = logObject.sn;
		sb.append(logObject.logDate.to_yyyy_MM_dd_HH_mm_ss());
		if (ThreadContext.get().isTest()) {
			sb.append(" -TEST- ");
		}
		if (sn != null) {
			return sb.append(" [").append(sn).append("] ");
		}
		return sb.append(" {").append(logObject.threadName).append("} ");
	}

	public static CodeLine extractCodeLine(String pre) {
		if (pre == null || pre.isEmpty()) {
			return null;
		}
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		if (stack != null && stack.length > 2) {
			for (int i = stack.length - 1; i > -1; i--) {
				String clzName = stack[i].getClassName();
				if (clzName.startsWith(pre)) {
					StackTraceElement s = stack[i + 1];
					if (s.getLineNumber() < 0 && i + 2 < stack.length) {
						s = stack[i + 2];
					}
					return new CodeLine(s.getClassName(), s.getMethodName(), s.getLineNumber());
				}
			}
		}
		return null;
	}
}
