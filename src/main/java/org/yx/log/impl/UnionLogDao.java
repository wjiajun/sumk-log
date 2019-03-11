package org.yx.log.impl;

import java.io.IOException;

public interface UnionLogDao {

	void append(String text) throws IOException;

	void start();

	void reset() throws IOException;

}