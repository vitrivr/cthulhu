# cthulhu
Distributed job-scheduler for feature extraction.

## Running the Cthulhu Scheduler
The Cthulhu runner should also have access to a properties file with some basic configuration options. Specially,
the properties file must contain a `workspace` property which is a directory that can be manipulated by the Cthulhu
process (or it will default to /workspace, next to /ui).

```
java -jar build/libs/cthulhu.jar --help
usage: Cthulhu [-a <arg>] [-C] [-c <arg>] [-h] [-ha <arg>] [-hp <arg>] [-p
       <arg>] [-W]
Run the Cthulhu task scheduler

 -a,--address <arg>        Address of the local host (DNS? IP?) -
                           overrides properties file
 -C,--coordinator          Run as coordinator
 -c,--capacity <arg>       Capacity, or number of jobs that can run
                           simultaneously [for workers]
 -h,--help                 Display help menu
 -ha,--hostAddress <arg>   Address of coordinator host [for workers] -
                           overrides properties file
 -hp,--hostPort <arg>      Port of coordinator host [for workers] -
                           overrides properties file
 -p,--port <arg>           Port in which to listen to - overrides
                           properties file
 -W,--worker               Run as worker

Please report issues at http://github.com/vitrivr/cthulhu/issues
```

### Existing job types

* `BashJob` - This job represents a bash script to be executed.

### Adding new job types
The code is designed so that adding new jobs is an easy process. To add a new job type, you must follow these steps:

1. Create a new class in the package `org.vitrivr.cthulhu.jobs` that extends the `Job` abstract class. Specifically
you must implement the `execute` method, and the `cleanup` method if you need any cleanup to be done.

2. In the web user interface, you must add the option to create a job of the appropriate type. This can be done by
adding a new `option` in the `typeLst` datalist around [this location](https://github.com/vitrivr/cthulhu/blob/master/resources/ui/index.html#L117).
