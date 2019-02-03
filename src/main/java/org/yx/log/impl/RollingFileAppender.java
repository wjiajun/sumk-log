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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.yx.common.Deamon;
import org.yx.conf.AppInfo;
import org.yx.log.ConsoleLog;
import org.yx.log.SumkLogger;
import org.yx.main.SumkThreadPool;
import org.yx.util.SumkDate;

abstract class RollingFileAppender implements LogAppender, Deamon {

	protected final String name;

	protected boolean sync = AppInfo.getBoolean("sumk.log.file.sync", true);

	protected int interval = AppInfo.getInt("sumk.log.file.interval", 30000);

	protected int maxClearSize = AppInfo.getInt("sumk.log.file.clear.size", 10);

	private static final long DAY = 1000L * 3600 * 24;

	private long lastDeleteTime;

	public RollingFileAppender(String name) {
		this.name = name;
	}

	@Override
	public String name() {
		return this.name;
	}

	private Map<String, FileOutputStream> map = new HashMap<>();

	protected String pattern;
	protected File dir;
	public static final String SLOT = "#";
	protected final BlockingQueue<Message> queue = new LinkedBlockingQueue<>(
			Integer.getInteger("sumk.log.rolling.maxLog", 10000));
	private volatile boolean stoped;
	protected Collection<String> modules = Collections.emptyList();

	protected abstract boolean shouldDelete(String fileName);

	protected static boolean setup(RollingFileAppender appender, String fileName) {
		if (fileName.indexOf("#") < 0) {
			ConsoleLog.getLogger("sumk.log").error("{} should contain {}", appender.pattern, SLOT);
			return false;
		}
		if (fileName.indexOf("#") != fileName.lastIndexOf("#")) {
			ConsoleLog.getLogger("sumk.log").error("{} contain more than one {}", appender.pattern, SLOT);
			return false;
		}
		File file = new File(fileName);
		appender.pattern = file.getName();
		if (!appender.pattern.contains(SLOT)) {
			ConsoleLog.getLogger("sumk.log").error("{} should contain {}", appender.pattern, SLOT);
			return false;
		}
		appender.dir = file.getParentFile();
		if (!appender.dir.exists() && !appender.dir.mkdirs()) {
			ConsoleLog.getLogger("sumk.log").error("directory [{}{}] is not exists", appender.dir.getAbsolutePath(),
					File.pathSeparator);
			return false;
		}

		return true;
	}

	/**
	 * @param key
	 * @return 如果失败，就返回null
	 */
	private FileOutputStream getOutputStream(String date) {
		FileOutputStream out = this.map.get(date);
		if (out != null) {
			return out;
		}
		String fileName = pattern.replace(SLOT, date);
		try {
			File file = new File(dir, fileName);
			if (!file.exists() && !file.createNewFile()) {
				ConsoleLog.getLogger("sumk.log").error("{} create fail ", file.getAbsolutePath());
				return null;
			}
			out = new FileOutputStream(file, true);
			this.map.put(date, out);
			return out;
		} catch (Exception e) {
			ConsoleLog.get("sumk.log").warn("fail to create file " + fileName, e);
			return null;
		}
	}

	private void deleteHisLog() {
		long now = System.currentTimeMillis();
		if (now - this.lastDeleteTime < DAY) {
			return;
		}
		this.lastDeleteTime = now;
		String[] files = dir.list();
		if (files == null || files.length == 0) {
			return;
		}
		for (String f : files) {
			if (this.shouldDelete(f)) {
				try {
					File log = new File(dir, f);
					log.delete();
				} catch (Exception e) {
				}
			}
		}
	}

	private void clearHisOutput() {
		if (map.size() < 2) {
			return;
		}
		SumkDate date = SumkDate.now();

		if (date.getHour() == 0 && date.getMinute() < 30) {
			return;
		}
		String d = this.toSubString(date);
		List<String> keys = new ArrayList<>(map.keySet());
		for (String key : keys) {
			if (key.equals(d)) {
				continue;
			}
			FileOutputStream out = map.remove(key);
			if (out != null) {
				close(out);
			}
		}
	}

	protected void clean() {
		this.clearHisOutput();
		this.deleteHisLog();
	}

	protected abstract String toSubString(SumkDate date);

	@Override
	public boolean run() throws Exception {
		List<Message> list = new ArrayList<>();
		Message message = queue.poll(interval, TimeUnit.MILLISECONDS);
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
	public void start() {
		ConsoleLog.get("sumk.log").debug("{} started",
				this.getClass().getSimpleName().replace("RollingFileAppender", "Log"));
		SumkThreadPool.runDeamon(this, this.getClass().getName());
	}

	/**
	 * @param msgs
	 *            这个不能为空
	 * @throws IOException
	 */
	protected void output(List<Message> msgs) throws IOException {
		FileChannel fc = null;
		while (fc == null) {
			String date = toSubString(msgs.get(0).date);
			FileOutputStream out = this.getOutputStream(date);
			if (out == null) {
				return;
			}
			fc = out.getChannel();
			if (!fc.isOpen()) {
				map.remove(date);
				close(out);
				fc = null;
			}
		}
		int size = 0;
		List<byte[]> datas = new ArrayList<>(msgs.size());
		for (Message m : msgs) {
			byte[] b = m.msg.getBytes();
			datas.add(b);
			size += b.length;
		}
		ByteBuffer buf = ByteBuffer.allocate(size);
		for (byte[] b : datas) {
			buf.put(b);
		}
		buf.flip();

		do {
			fc.write(buf);
		} while (buf.hasRemaining());
		if (this.sync) {
			fc.force(false);
		}
	}

	private void close(FileOutputStream out) {
		try {
			out.close();
		} catch (IOException e) {
			ConsoleLog.get("sumk.log").warn("log close error", e);
		}
	}

	@Override
	public void stop() {
		if (stoped) {
			return;
		}
		stoped = true;
	}

	@Override
	public boolean print(Message msg) {
		if (!checkModule(msg.log)) {
			return false;
		}
		return queue.offer(msg);
	}

	/**
	 * @param module
	 * @return
	 */
	private boolean checkModule(SumkLogger log) {
		String module = log.getName();
		for (String name : this.modules) {
			if (module.startsWith(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void modules(Collection<String> pps) {
		this.modules = pps;
	}

	@Override
	public void close() throws Exception {
		for (int i = 0; i < 3; i++) {
			List<Message> list = new ArrayList<>();
			queue.drainTo(list);
			if (list.size() > 0) {
				output(list);
			} else {
				LockSupport.parkNanos(10_000_000L);
			}
		}
		for (FileOutputStream out : this.map.values()) {
			close(out);
		}
		this.map = new HashMap<>();

	}

}
