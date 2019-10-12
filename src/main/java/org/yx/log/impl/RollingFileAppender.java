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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;

import org.yx.conf.AppInfo;
import org.yx.util.SumkDate;

public abstract class RollingFileAppender extends FileAppender {

	private static final long DAY_MILS = 1000L * 3600 * 24;

	public static final String SLOT = "#";

	protected boolean showAttach;

	protected static boolean setup(RollingFileAppender appender, String fileName) {
		Objects.requireNonNull(fileName, "log path cannot be null!!!");
		if (fileName.indexOf(SLOT) < 0) {
			Appenders.consoleLog.error("{} should contain {}", appender.filePattern, SLOT);
			return false;
		}
		if (fileName.indexOf(SLOT) != fileName.lastIndexOf(SLOT)) {
			Appenders.consoleLog.error("{} contain more than one {}", appender.filePattern, SLOT);
			return false;
		}
		File file = new File(fileName);
		appender.filePattern = file.getName();
		if (!appender.filePattern.contains(SLOT)) {
			Appenders.consoleLog.error("{} should contain {}", appender.filePattern, SLOT);
			return false;
		}
		appender.dir = file.getParentFile();
		if (!appender.dir.exists() && !appender.dir.mkdirs()) {
			Appenders.consoleLog.error("directory [{}{}] is not exists, and cannot create!!!",
					appender.dir.getAbsolutePath(), File.pathSeparator);
			return false;
		}

		return true;
	}

	protected boolean sync = AppInfo.getBoolean("sumk.log.file.sync", true);

	private long lastDeleteTime;

	private Map<String, FileOutputStream> map = new HashMap<>();

	protected String filePattern;
	protected File dir;

	public RollingFileAppender(String name) {
		super(name);
		this.showAttach = AppInfo.getBoolean("sumk.log.attach.show", true);
	}

	@Override
	protected void clean() {
		this.clearHisOutput();
		this.deleteHisLog();
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

	@Override
	public void close() throws Exception {
		super.close();
		for (int i = 0; i < 3; i++) {
			List<LogObject> list = new ArrayList<>();
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
		Appenders.consoleLog.info(SumkDate.now().to_yyyy_MM_dd_HH_mm_ss() + "日志停止了");
	}

	private void close(FileOutputStream out) {
		try {
			out.close();
		} catch (IOException e) {
			Appenders.consoleLog.warn("log close error", e);
		}
	}

	private void deleteHisLog() {
		long now = System.currentTimeMillis();
		if (now - this.lastDeleteTime < DAY_MILS) {
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

	private FileOutputStream getOutputStream(String date) {
		FileOutputStream out = this.map.get(date);
		if (out != null) {
			return out;
		}
		String fileName = filePattern.replace(SLOT, date);
		try {
			File file = new File(dir, fileName);
			if (!file.exists() && !file.createNewFile()) {
				Appenders.consoleLog.error("{} create fail ", file.getAbsolutePath());
				return null;
			}
			out = new FileOutputStream(file, true);
			this.map.put(date, out);
			return out;
		} catch (Exception e) {
			Appenders.consoleLog.warn("fail to create file " + fileName, e);
			return null;
		}
	}

	@Override
	protected void output(List<LogObject> msgs) throws IOException {
		FileChannel fc = null;
		while (fc == null) {
			String date = toSubString(msgs.get(0).logDate);
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
		for (LogObject m : msgs) {
			byte[] b = toBytes(m);
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
			try {
				fc.force(false);
			} catch (Exception e) {
				Appenders.consoleLog.warn(this.name + " -- " + e.toString());
			}
		}
	}

	protected abstract boolean shouldDelete(String fileName);

	protected byte[] toBytes(LogObject logObject) {
		return LogObjectHelper.plainMessage(logObject, this.showAttach).getBytes(LogObject.CHARSET);
	}

	protected abstract String toSubString(SumkDate date);

	protected boolean onStart(Map<String, String> map) {
		String path = map.get(Appenders.PATH);
		String module = map.get(Appenders.MODULE);
		Appenders.consoleLog.debug(name + " = path:{} , module:{}", path, module);
		if (!setup(this, path)) {
			return false;
		}
		config(map);
		return true;
	}

	public String getFilePattern() {
		return filePattern;
	}

	public File getDir() {
		return dir;
	}

}
