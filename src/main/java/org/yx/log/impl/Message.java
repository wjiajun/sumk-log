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

import org.yx.log.SumkLogger;
import org.yx.util.SumkDate;

public class Message {

	public final String msg;
	public final SumkDate date;

	public final SumkLogger log;

	Message(String msg, SumkDate date, SumkLogger log) {
		super();
		this.msg = msg;
		this.date = date;
		this.log = log;
	}

}
