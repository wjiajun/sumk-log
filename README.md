# sumk-log
&emsp;&emsp;sumk-log是一款基于sumk框架和slf4j标准的日志系统。目前支持日志的控制台输出以及文件格式输出。也能记录用户自定义的日志，其java api类似于logback等其它slf4j产品。但是它能记录userid等信息。它可以使用slf4j标准的方式书写，也可以使用Log.get(**)的方式获取slf4j对象。

### 引入sumk-log.jar
```
<dependency>
    <groupId>com.github.youtongluan</groupId>
    <artifactId>sumk-log</artifactId>
    <version>2.2.1</version>
</dependency>
```
最新版本请查看maven中央库。

### 配置说明：
#### sumk.log.type=console|slf4j
slf4j并不是sumk-log专用，而是一个通用的slf4j接口，sumk-log是slf4j的一个特别实现

#### sumk.log.level=*:debug,*.*:error,root:info
root表示根节点或没有包名都表示根节点。这个是console和slf4j两种模式都支持的

#### sumk.log.console=1表示启用控制台输出

#### s.log.日志类型=path:日志存放路径;module:com.test,a.*
* 日志类型现有day、month、union三种，支持扩展。
* 日志存放路径中要有一个#,比如/log/daylog-#.txt
* module指定该日志所对应的日志名。支持一个*作为通配符。

#### 其它说明
* 以上配置，level级别、console开关支持动态变更，日志类型支持动态增加和module动态变更。变更一般会在1分钟内生效。
* 将这个特性与统一配置相结合，就可以动态切换日志级别，也可以动态设置是否将某一类型日志发送到日志中心（统一日志）。
* sumk日志跟其他日志一个很大的不同在于，sumk日志的level是全局的，而logback的level是针对具体日志类型的。


