# sumk-log
&emsp;&emsp;sumk-tool是一款slf4j标准的本地日志系统。支持日志的控制台输出以及文件格式输出。也能记录用户自定义的日志，其java api类似于logback等其它slf4j产品。但是它能记录userid等信息。

### 引入sumk-log.jar
```
<dependency>
    <groupId>com.github.youtongluan</groupId>
    <artifactId>sumk-log</artifactId>
    <version>1.9.0</version>
</dependency>
```
最新版本请查看maven中央库。

### 作为简单工具使用
&emsp;&emsp;[org.yx.tools](https://github.com/youtongluan/sumk-tool/tree/master/src/test/java/org/yx/tools)包里面的测试用例，就是它的简单使用场景，主要是分为有使用redis和没有使用redis的情况<br>

### 跟spring mvc混合使用
&emsp;&emsp;在spring mvc中注入sumk：在web.xml中用org.yx.tools.ContextLoaderListener替代spring的ContextLoaderListener（spring mvc的Dispatcher不变）,然后在spring和mvc的配置文件里，将org.yx.tools.ContextLoaderListener添加为它们的bean（beanid最好改一下，免得跟spring的冲突）。


