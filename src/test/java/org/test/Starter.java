package org.test;

import org.junit.Assert;
import org.junit.Test;
import org.yx.conf.MapConfig;
import org.yx.conf.SystemConfigHolder;
import org.yx.log.Log;
import org.yx.log.LogLevel;
import org.yx.log.Loggers;
import org.yx.log.impl.SumkLoggerImpl;
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
	
	@Test
	public void level() throws Exception {
		for(int i=0;i<5;i++){
			MapConfig conf=MapConfig.create();
			conf.putKV("sumk.log.level=debug,com.abc:error,com.aaa:info");
			SystemConfigHolder.setSystemConfig(conf);
			Loggers logs=Loggers.create("a"+i);
			Assert.assertEquals(LogLevel.DEBUG, logs.getLevel((SumkLoggerImpl) Log.get("com")));
			Assert.assertEquals(LogLevel.DEBUG, logs.getLevel((SumkLoggerImpl) Log.get("com.abcd")));
			Assert.assertEquals(LogLevel.ERROR, logs.getLevel((SumkLoggerImpl) Log.get("com.abc")));
			Assert.assertEquals(LogLevel.ERROR, logs.getLevel((SumkLoggerImpl) Log.get("com.abc.a")));
		}
	}

}
