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

### 配置说明：
#### sumk.log.type=console|slf4j
slf4j并不是sumk-log专用，而是一个通用的slf4j接口，sumk-log是slf4j的一个特别实现

#### sumk.log.level=**:debug,*.*:error,root:info
root表示根节点或没有包名都表示根节点。这个是console和slf4j两种模式都支持的

#### sumk.log.appender.日志名称=日志类型,日志存放路径
类型有day、month,不区分大小写
sumk.log.appender.console=1表示启用控制台输出

#### sumk.log.module.日志名称=a.b,a
如果不配置包名，默认为root。指定哪些目录下的日志会写入到该日志中。这里的目录不是package，而是日志名称
ory.yx可以匹配ory.yx.log，但不能匹配org.yxc(先判断时候以module开头，再判断下一个char是不是)


以上配置，只有leve和module支持动态变更，其它的不支持
sumk日志跟其他日志一个很大的不同在于，sumk日志的level是全局的，而logback的level是针对具体日志的


