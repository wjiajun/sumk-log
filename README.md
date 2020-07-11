# sumk-log
&emsp;&emsp;sumk-log是一款基于slf4j标准的日志系统。目前支持日志的控制台输出以及文件格式输出。也能记录用户自定义的日志，其java api类似于logback等其它slf4j产品。但是它能记录userid等信息。它可以使用slf4j标准的方式书写，也可以使用Log.get(**)的方式获取slf4j对象。

### 引入sumk-log.jar
```
<dependency>
    <groupId>com.github.youtongluan</groupId>
    <artifactId>sumk-log</artifactId>
    <version>2.8.1</version>
</dependency>
```
最新版本请查看maven中央库。

### 配置说明：


#### sumk.log.level=info,XX:debug,X.X:error
前面表示名称，后面是日志级别。如果没有名称,就表示是全局的。默认级别是INFO

#### sumk.log.console=1 启用控制台输出

#### s.log.日志类型=path:日志存放路径;module:com.test,a.*
* 日志类型现有day、month、level三种，支持扩展。统一日志的配置不在这里
* 日志存放路径中要有一个#,比如/log/daylog-#.log
* module指定该日志所对应的日志名。支持头尾出现*作为通配符。这个配置是可选的
* level类型日志跟day类型相似，差异是level日志除了module外，还增加了个level属性，它可以设置只输出摸个级别以上的日志

### 统一日志
* union是统一日志系统，它跟普通类型有点不同。它需要调用UnionLogs.start()开启。
* sumk.unionlog.module 方式配置统一日志允许打印的模块。
* 通过UnionLogs.getUnionLog().directOffer()方式输出的日志，不受module配置影响，无法关闭。这是因为统一日志需要具备审查的功能，不允许开发者随意关闭，所以采用代码和配置相结合的方式
* 统一日志默认也是输出到文件，可以自己定义输出位置。参见test目录下的UnionDemo

### 其它参数
* sumk.log.day.max day或level类型日志的日志文件保存个数，默认30
* sumk.log.month.max month类型的日志的日志文件保存个数，默认2
* 日志体达到某种大小后会被截短，这些参数的设置，参见[入门教程](https://github.com/youtongluan/sumk-server-demo)里的《sumk框架入门.docx》


#### 其它说明
* 将这个特性与统一配置相结合，就可以动态切换日志级别，也可以动态设置是否将某一类型日志发送到日志中心（统一日志）。
* sumk日志跟logback一个很大的不同在于，sumk日志的level是全局的，而logback的level是针对具体日志类型的。


