package org.test;

import org.junit.Test;
import org.yx.log.Log;
import org.yx.main.SumkServer;

public class Starter {

	@Test
	public void test() {
		Log.get(this.getClass()).info("{} 只是个测试类",this.getClass().getSimpleName());
		org.apache.log4j.Logger.getLogger(this.getClass()).info("这是log4j的测试类");
		org.apache.logging.log4j.LogManager.getLogger(this.getClass()).info("这是log4j 2.x的测试类");
		org.apache.logging.log4j.LogManager.getLogger(this.getClass()).info("如果使用log4j 2，请引入log4j-to-slf4j");
		SumkServer.stop();
	}
	
}
