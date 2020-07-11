package org.test;

import org.junit.Test;
import org.yx.log.Log;
import org.yx.log.impl.DefaultUnionLog;
import org.yx.log.impl.UnionLogDao;
import org.yx.log.impl.UnionLogs;

public class UnionDemo {

	@Test
	public void test() throws InterruptedException {
		DefaultUnionLog unionLog=(DefaultUnionLog) UnionLogs.getUnionLog();
		unionLog.setDao(new UnionLogDao(){

			@Override
			public void flush(boolean idle) {
			}

			@Override
			public void store(String msg, int recordSize) {
				System.out.println("统一日志（"+recordSize+"条）"+msg);
			}
		});
		UnionLogs.start();//启动统一日志
		
//		app.propertis中配置了unionTest写入到统一日志
		Log.get("unionTest").info("abc");
		Log.get("unionTest").info("222");
		Thread.sleep(1000);
	}

}
