# cthulhu
Distributed job-scheduler for feature extraction.

### Existing job types

* `BashJob` - This job represents a bash script to be executed.

### Adding new job types
The code is designed so that adding new jobs is an easy process. To add a new job type, you must follow these steps:

1. Create a new class in the package `ch.unibas.cs.dbis.cthulhu` that extends the `Job` abstract class. Specifically
you must implement the `execute` method, and the `cleanup` method.

2. In the `JobFactory` class, you must add the option to create a job of the appropriate type. This can be done by
adding a new `case` in the `buildJob` method.
