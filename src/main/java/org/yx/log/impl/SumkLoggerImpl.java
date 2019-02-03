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

import org.yx.common.ThreadContext;
import org.yx.log.LogLevel;
import org.yx.log.Loggers;
import org.yx.log.SumkLogger;
import org.yx.util.SumkDate;

public class SumkLoggerImpl extends SumkLogger {
	private static final long serialVersionUID = 1;
	private static final char LN = '\n';

	public SumkLoggerImpl(String module) {
		super(module);
	}

	@Override
	protected void output(LogLevel methodLevel, String msg) {
		this.output(methodLevel, msg, (Object[]) null);
	}

	@Override
	protected void output(LogLevel methodLevel, String format, Object... arguments) {
		SumkDate date = SumkDate.now();
		String msg = this.buildMessage(format, arguments);
		StringBuilder sb = createStringBuilder(date).append(methodLevel).append(" ").append(shorter(name)).append(" - ")
				.append(msg);
		sb.append(LN);
		String outMsg = sb.toString();
		if (!Appenders.print(new Message(outMsg, date, this)) || Appenders.console) {
			System.out.print(outMsg);
		}
	}

	private StringBuilder createStringBuilder(SumkDate date) {
		StringBuilder sb = new StringBuilder();
		String sn = ThreadContext.get().userIdOrContextSN();
		sb.append(date.to_yyyy_MM_dd_HH_mm_ss());
		if (ThreadContext.get().isTest()) {
			sb.append(" -TEST- ");
		}
		if (sn != null) {
			return sb.append(" [").append(sn).append("] ");
		}
		return sb.append(" {").append(Thread.currentThread().getName()).append("} ");
	}

	@Override
	protected void output(LogLevel methodLevel, String msg, Throwable e) {
		SumkDate date = SumkDate.now();
		StringBuilder sb = createStringBuilder(date).append(methodLevel).append(" ").append(shorter(name)).append(" - ")
				.append(msg).append('\n');
		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);
		w.print(sb.toString());
		e.printStackTrace(w);
		w.print(LN);
		String errorMsg = sw.toString();
		if (!Appenders.print(new Message(errorMsg, date, this)) || Appenders.console) {
			System.err.print(errorMsg);
		}
	}

	@Override
	protected Loggers loggers() {
		return SumkLoggerFactory.loggers;
	}

}
