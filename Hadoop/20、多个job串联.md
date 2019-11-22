# 多个Job串联

```java
 //基于job构建ControlledJob
    ControlledJob controlledJob1 = new ControlledJob(job1.getConfiguration());
    ControlledJob controlledJob2 = new ControlledJob(job2.getConfiguration());

    controlledJob2.addDependingJob(controlledJob1);
    JobControl jobControl = new JobControl("friend");

    jobControl.addJob(controlledJob1);
    jobControl.addJob(controlledJob2);

    Thread jobControlTread = new Thread(jobControl);

    jobControlTread.setDaemon(true);
    jobControlTread.start();

    while (true){
    if(jobControl.allFinished()){
        List<ControlledJob> successfulJobList = jobControl.getSuccessfulJobList();

        break;
        }
    }

```

还要注意的是在设置Job2的输入目录时，应设置为job1的输出目录。JobControlTread时一个线程，如果不设置为Daemon，这个线程不会停止。
