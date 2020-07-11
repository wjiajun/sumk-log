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

public interface UnionLogDao {

	/**
	 * 一段时间触发,间隔约为interval
	 * 
	 * @param idle
	 *            true表示本次任务没有日志
	 */
	void flush(boolean idle);

	/**
	 * 输出日志对象
	 * 
	 * @param msg
	 *            一般不止一条记录,它以分隔符结束，所以不需要再添加分隔符
	 * @param recordSize
	 *            实际记录数
	 */
	void store(String msg, int recordSize);
}
