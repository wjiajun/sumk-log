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

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.yx.common.matcher.Matchers;
import org.yx.conf.AppInfo;

public class UnionMatcherSupplier implements Supplier<Predicate<String>> {

	@Override
	public Predicate<String> get() {
		return Matchers.includeAndExclude(AppInfo.get("sumk.unionlog.module", null),
				AppInfo.get("sumk.unionlog.exclude", null));
	}

}
