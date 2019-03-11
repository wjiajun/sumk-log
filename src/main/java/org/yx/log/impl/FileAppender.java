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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.yx.common.Daemon;
import org.yx.common.matcher.MatcherFactory;
import org.yx.common.matcher.TextMatcher;
import org.yx.conf.AppInfo;
import org.yx.log.ConsoleLog;
import org.yx.log.SumkLogger;
import org.yx.main.SumkThreadPool;

public abstract class FileAppender implements LogAppender, Daemon {

	protected final String name;

	protected int interval;

	protected int maxClearSize;

	protected final BlockingQueue<LogObject> queue = new LinkedBlockingQueue<>(
			Integer.getInteger("sumk.log.queue.size", 10000));

	protected TextMatcher matcher;

	public FileAppender(String name) {
		this.name = name;
		this.maxClearSize = AppInfo.getInt("sumk.log.file.clear.size", 10);
		this.interval = AppInfo.getInt("sumk.log.file.interval", 30000);
	}

	private boolean checkModule(SumkLogger log) {
		String module = log.getName();
		return this.matcher != null && this.matcher.match(module);
	}

	protected abstract void clean() throws Exception;

	@Override
	public final void config(Map<String, String> configMap) {
		String patterns = configMap == null ? null : configMap.get(Appenders.MODULE);
		this.matcher = MatcherFactory.createWildcardMatcher(patterns, 1);
		ConsoleLog.get("sumk.log").debug("{} set matcher ：{}", this.name, this.matcher);
	}

	@Override
	public final String name() {
		return this.name;
	}

	@Override
	public boolean offer(LogObject logObject) {
		if (!checkModule(logObject.logger)) {
			return false;
		}
		return queue.offer(logObject);
	}

	protected abstract void output(List<LogObject> list) throws Exception;

	@Override
	public boolean run() throws Exception {
		List<LogObject> list = new ArrayList<>();
		LogObject message = queue.poll(interval, TimeUnit.MILLISECONDS);
		if (message == null) {
			clean();
			return true;
		}
		list.add(message);
		queue.drainTo(list);
		output(list);
		if (list.size() <= this.maxClearSize) {
			clean();
		}
		return true;
	}

	@Override
	public boolean start(Map<String, String> map) {
		if (!onStart(map)) {
			return false;
		}
		Appenders.consoleLog.debug("{} started by {}", this.toString(), map);
		SumkThreadPool.runDeamon(this, this.name + "-logger");
		return afterStarted();
	}

	protected boolean afterStarted() {
		return true;
	}

	protected abstract boolean onStart(Map<String, String> configMap);

	@Override
	public void stop() {
	}

	@Override
	public String toString() {
		return this.name + " [queue size:" + queue.size() + ",matcher:" + matcher + "]";
	}
}
