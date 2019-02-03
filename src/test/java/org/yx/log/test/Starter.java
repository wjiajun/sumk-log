package org.yx.log.test;

import org.junit.Test;
import org.yx.log.Log;
import org.yx.main.SumkServer;

public class Starter {

	@Test
	public void test() {
		SumkServer.start();
		Log.get(this.getClass()).info("{} 只是个测试类",this.getClass().getSimpleName());
	}

}
