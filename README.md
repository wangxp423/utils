# 豆豆电话本开发总结
<br/>
##这个项目是我在开发"豆豆电话本"时的一些总结. 以后会继续整理更新. 先提交简单的.<br/>
<br/>
1. xlog 日志类. <br/>
  例子:　<br/>
  ```java
　　　　xlog.d("Hello", argString, argInt, argArrray, argMap, argList, argObject);
  ```<br/>
  可以打印任何类型, 异常会打印堆栈.<br/>
  对集合类打印规则做了处理, 比如List和Array输出类似[1,2,3], 而Map输出{1:"a",2:"b",3:"c"}<br/>
  可以***自定义输出类***, 支持输出到logcat, 文件, stream<br/>
  处于性能考虑, 对打印规则做了处理, 每N秒才会flush到输出, 默认输出到logcat.<br/>
  可以同时使用多个输出, 比如, 同时使用Logcat和文件输出.<br/>
  之所以使用小写类名xlog, 是因为"XLog"键盘敲起来不舒服, 仅此而已.<br/>
<br/>

2. TaskHandle类, 后台队列执行类<br/>
  例子:<br/>
  ```java
      TaskHandler taskHandler = new TaskHandler();<br/>
      taskHandler.fore(new Runable(){<br/>
          xlog.d("在主线程运行");<br/>
      });<br/>
      taskHandler.back(new Runable(){<br/>
          xlog.d("在后台线程运行");<br/>
      });<br/>
  ``` <br/>
  其他方法, backFore现在后台线程运行一个方法onBac, 然后在主线程执行方法onFore.  foreBack类似.<br/>
<br/>
3. TaskUtil, 后台并发执行类, 跟TaskHandler类似, 不同的是, back方法在线程池中执行,是并发的.<br/>
  另外,TaskUtil支持repeat方法, 按规则重复执行一个回调, 这在有些特定场景会有用, 比如注册时, 等待短信验证码到来. 

4. RunTask类, 支持取消任务的Runnable,  用于TaskHandle和TaskUtil,  也可以用于其他需要Runnable的地方<br/>
  如果给RunTask一个组名, 支持取消整个组的任务.<br/>
  
5. MsgCenter和Msg, 进程内的消息广播.<br/>
  例子<br/>
  ```java
  class MyActivity extends Activity implement MsgListener{  <br/>
      void onCreate(){  <br/>
          MsgCenter.addListener(this, "广播消息ID");  <br/>
      }  <br/>
      void onMsg(Msg msg){  <br/>
          if(msg.is("广播消息ID")){  <br/>
              xlog.d("收到了广播");  <br/>
          }  <br/>
      }  <br/>
      void onDestory(){  <br/>
          MsgCenter.remove(this);  <br/>
      }  <br/>
  }  <br/>
  ``` <br/>
  
  可以在其他地方发出广播:<br/>
  ```java
    MsgCenter.fire("广播消息ID");
  ``` <br/>
  或者:<br/>
  ```java
    Msg.msg("广播消息ID").fire();
  ``` <br/>
  Msg对象可以携带参数, 也可以搜集返回值. <br/>
  <br/>
  由于MsgCenter使用了静态数据结构来存储广播接收器, 因此, 使用完成后要注意及时注销监听, 以避免内存泄漏.<br/>
  建议在Activity或Fragment的onCreate中注册监听,  在onDestroy中注销监听. 或者onResume/onPause等.<br/>
  <br/>
6. 其他待续<br/>
