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
import java.util.Map;
import java.util.Objects;

import org.yx.common.context.ActionContext;
import org.yx.conf.AppInfo;
import org.yx.log.LogKits;

public class PlainOutputImpl implements PlainOutput {
	private boolean showThreadName = true;
	private boolean showSN = true;

	@Override
	public void setShowThreadName(boolean showThreadName) {
		this.showThreadName = showThreadName;
	}

	@Override
	public void setShowSN(boolean showSN) {
		this.showSN = showSN;
	}

	public String plainMessage(LogObject logObject, boolean showAttachs) {
		StringBuilder sb = createStringBuilder(logObject).append(logObject.methodLevel).append(" ");
		if (logObject.codeLine != null) {
			String clzShortName = LogKits.shorterPrefix(logObject.codeLine.className,
					logObject.logger.maxLogNameLength());

			if (!Objects.equals(logObject.logger.getName(), logObject.codeLine.className)) {
				sb.append('(').append(logObject.logger.getName()).append(')');
			}
			sb.append(clzShortName).append(':').append(logObject.codeLine.lineNumber);
		} else {
			sb.append(logObject.logger.getName());
		}
		Map<String, String> attachs = logObject.attachments();
		if (showAttachs && attachs != null) {
			sb.append(" #").append(attachs);
		}

		sb.append(" - ").append(LogKits.shorterSubfix(logObject.body, AppInfo.getInt("sumk.log.body.maxlength", 1000)))
				.append(LogObject.LN);
		if (logObject.exception != null) {
			StringWriter sw = new StringWriter();
			PrintWriter w = new PrintWriter(sw);
			logObject.exception.printStackTrace(w);
			sb.append(sw.toString()).append(LogObject.LN);
		}
		return sb.toString();
	}

	protected StringBuilder createStringBuilder(LogObject logObject) {
		StringBuilder sb = new StringBuilder();
		StringBuilder sn = new StringBuilder();
		if (logObject.userId() != null) {
			sn.append(logObject.userId());
		}
		if (logObject.traceId() != null) {
			if (sn.length() > 0) {
				sn.append("@");
			}
			sn.append(logObject.traceId());
			if (logObject.spanId() != null) {
				sn.append("-").append(logObject.spanId());
			}
		}
		sb.append(logObject.logDate.to_yyyy_MM_dd_HH_mm_ss_SSS());
		if (ActionContext.get().isTest()) {
			sb.append(" -TEST- ");
		}
		if (showThreadName) {
			sb.append(" [").append(logObject.threadName).append("] ");
		}
		if (showSN && sn.length() > 0) {
			sb.append(" {").append(sn).append("} ");
		}
		return sb;
	}

}
