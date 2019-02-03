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

import org.yx.log.ConsoleLog;

public class LogHelper {

	public static String realContext(String text, String pattern, String slot) {
		String[] fs = pattern.split(slot, 2);
		if (fs.length != 2) {
			ConsoleLog.getLogger("sumk.log").error("{} should contain and only contain one {}", pattern, slot);
			return null;
		}
		if (fs[0].length() + fs[1].length() > text.length()) {
			return null;
		}
		int end = text.length() - fs[1].length();
		if (!fs[0].equals(text.substring(0, fs[0].length())) || !fs[1].equals(text.substring(end))) {
			return null;
		}
		return text.substring(fs[0].length(), end);
	}
}
