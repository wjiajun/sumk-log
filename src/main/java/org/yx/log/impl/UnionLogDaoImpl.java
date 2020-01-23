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
import java.util.ArrayList;
import java.util.List;

import org.yx.conf.AppInfo;
import org.yx.log.ConsoleLog;
import org.yx.util.UUIDSeed;

public class UnionLogDaoImpl implements UnionLogDao {
	private final long MAX_FILE_LENGTH = AppInfo.getInt("sumk.log.union.max_file_length", 100 * 1024 * 1024);
	private final int MAX_RECORD_SIZE = AppInfo.getInt("sumk.log.union.max_record_size", 200);
	private final long ALIVE_TIME = AppInfo.getLong("sumk.log.union.alive_time", 15000);

	private long createTime = -1;

	private int fileLength;
	private List<byte[]> buffer;

	public UnionLogDaoImpl() {
		this.buffer = new ArrayList<>(MAX_RECORD_SIZE);
	}

	private File createLogingFile() {
		for (int i = 0; i < 5; i++) {
			try {
				String name = AppInfo.pid().concat("_").concat(UUIDSeed.seq18());
				File f = new File(UnionLogUtil.getLogingPath(), name);
				if (!f.getParentFile().exists()) {
					File parent = f.getParentFile();
					if (!parent.mkdirs()) {
						ConsoleLog.get(UnionLogUtil.SELF_LOG_NAME)
								.error("create folder " + parent.getAbsolutePath() + " failed!!!");
						return null;
					}
				}
				if (!f.createNewFile()) {
					ConsoleLog.get(UnionLogUtil.SELF_LOG_NAME)
							.error("create file " + f.getAbsolutePath() + " failed!!!");
					return null;
				}
				return f;
			} catch (Exception e) {
				ConsoleLog.get(UnionLogUtil.SELF_LOG_NAME).error(e.getMessage(), e);
			}
		}
		return null;
	}

	@Override
	public void append(String text) throws IOException {
		if (text == null || text.isEmpty()) {
			return;
		}
		byte[] bs = text.getBytes(AppInfo.UTF8);
		buffer.add(bs);
		this.fileLength += bs.length;
		if (buffer.size() >= MAX_RECORD_SIZE || System.currentTimeMillis() - createTime >= ALIVE_TIME
				|| this.fileLength > MAX_FILE_LENGTH) {
			reset();
		}
	}

	@Override
	public void start() {
		File loging = UnionLogUtil.getLogingPath();
		ConsoleLog.get(UnionLogUtil.SELF_LOG_NAME).info("loging path:{}", loging.getAbsolutePath());
	}

	@Override
	public void reset() throws IOException {
		List<byte[]> datas = this.buffer;
		this.buffer = new ArrayList<>(MAX_RECORD_SIZE);

		if (datas.isEmpty()) {
			return;
		}
		byte[] temp = new byte[fileLength + datas.size()];
		int index = 0;
		for (byte[] log : datas) {
			System.arraycopy(log, 0, temp, index, log.length);
			index += log.length;
			temp[index] = LogObject.LN;
			index++;
		}
		File logFile = createLogingFile();
		FileOutputStream out = new FileOutputStream(logFile);
		createTime = System.currentTimeMillis();
		out.write(temp, 0, index);
		out.flush();
		out.getFD().sync();
		out.close();
		UnionLogUtil.move2Loged(logFile);
		this.fileLength = 0;
	}

}
