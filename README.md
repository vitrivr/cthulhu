# cthulhu
Distributed job-scheduler for video feature extraction. Easily extendible to run other workloads that you may need.
If you'd like to use the cthulhu scheduler in your project, check out the section on [Adding new job types](adding-new-job-types),
or feel free to get in touch with the maintainer of the project (currently @pabloem).

## Building the cthulhu scheduler
The repository of the cthulhu scheduler is structured as a project that can be developed and compiled with gradle. Gradle
builds the cthulhu scheduler into a fat JAR packaged with all its dependencies. The project is coded in Java 8; so the basic
dependencies to build are *gradle* and *Java 8*.

```
$> gradle build
```

Note: The `gradle build` command also runs unit tests of the project.

## Running the Cthulhu Scheduler
To run a Cthulhu cluster, it is necessary to first start a coordinator process, which can be started using option
-C. The default port for the coordinator to listen in is 8082, and it can be set in a properties file, or as an argument
when the program is executed (see help menu below).

```
$> java -jar build/libs/cthulhu.jar -C [-p 8082 -a localhost]
```

* Note: When a cthulhu worker starts up, it will register with the coordinator. If the worker can not find and register
with the coordinator, it will finish execution without running.

Once the coordinator has been started, cthulhu runner instances can be started by using the option -W. A runner instance
must also receive the -hp and -ha argument options to find the coordinator to register with. The runner instance
should have access to a properties file with some basic configuration options. Specially, the properties file must
 contain a `workspace` property which is a directory that can be manipulated by the Cthulhu process (or it will
 default to /workspace, next to /ui).

```
$> java -jar build/libs/cthulhu.jar -W -hp 8082 -ha coord.ip [-p 8081] [-c 10]
```

The following is the menu printed by the help option of the cthulhu JAR:

```
java -jar build/libs/cthulhu.jar --help
sage: Cthulhu [-a <arg>] [-C] [-c <arg>] [-h] [-ha <arg>] [-hp <arg>] [-p
       <arg>] [-r <arg>] [-sf <arg>] [-W]
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
 -r,--restore <arg>        Restore instance from status file
 -sf,--staticFiles <arg>   Directory where the static files will be read
                           from (worker)
 -W,--worker               Run as worker
 
Please report issues at http://github.com/vitrivr/cthulhu/issues    
```

### Restoring a coordinator
The cthulhu coordinator process contains code to restore itself in the eventuality that it is killed while running. To
restore a coordinator process, it must be started with the -r option, and be passed the bookkeeping file of the previous
coordinator run, which usually is the file `.cthulhuStatus` in the running directory.

```
$> java -jar build/libs/cthulhu.jar -r .cthulhuStatus
```

* Note: If a coordinator is started without the -r option, *it will overwrite* the `.cthulhuStatus` bookkeeping file,
and the information of the previous run *will be lost*. Make sure to use the -r option when you need it.

* Note: The cthulhu worker process does not restore itself. If a worker has been killed, the coordinator will assume that
its running job has failed, and it will reschedule it on another worker.

## Existing job types

* `BashJob` - This job represents a bash script to be executed. The action field of the job contains the script that is
to run. The job collects all standard output and standard error streams - so extremely large output jobs may be troublesome.

* `FeatureExtractionJob` - This job runs the cineast executable to extract the information retrieval-related features of
a video file. The config field is a JSON field that contains the video file and subtitle file information.

### Adding new job types
The code is designed so that adding new jobs is an easy process. To add a new job type, you must follow these steps:

1. Create a new class in the package `org.vitrivr.cthulhu.jobs` that extends the `Job` abstract class. Specifically
you must implement the `execute` method, and the `cleanup` method if you need any cleanup to be done.

2. In the web user interface, you must add the option to create a job of the appropriate type. This can be done by
adding a new `option` in the `typeLst` datalist around [this location](https://github.com/vitrivr/cthulhu/blob/master/resources/ui/index.html#L117).
