
# wordcount本地运行模式源码分析

## 提交流程

```java
    // 判断state状态，如果在运行wait之后进行conf的设置时会出现running的错误
  if (state == JobState.DEFINE) {
      submit();
    }
```
  