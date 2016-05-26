(function () {
  /* The logic to prepare a job definition to be submitted:
   *  requiredFields - Is a dictionary with fields that are required
   *                   to create a job. If any is missing, the job is 
   *                   not ready to be created.
   *  readyJob - A boolean variable to keep track of whether we're ready
   *             to submit the job.
   *  getJobDef - The main function to retrieve the job definition from
   *              the input form.
   *  submitJobDef - The function that is in charge of submitting the job
   *                 to the master.
   */
  var requiredFields = {'name':true,
                        'type':true,
                        'action':true},
      readyJob = false,
      getJobDef = function() {
        var dict = {},
            missingFields = 0;
        $("#jobform .jobdata").each(function(i,e) {
          // If the field is required, but absent: problem!
          if (requiredFields[e.id] == true &&
              (e.value === undefined || e.value == "")) {
            console.log(e.id + " is a required field.");
            missingFields += 1;
          }
          dict[e.id] = e.value;
        });
        readyJob = true;
        if(missingFields > 0) readyJob = false;
        return dict;
      },
      refreshJobDef = function() {
        var dict = getJobDef();
        console.log(dict);
      },
      submitJobDef = function() {
        if(!readyJob) {
          console.log("Job can't be submitted. The definition is not ready.");
          return;
        }
        console.log("Posting job creation...");
        $.post("/jobs", JSON.stringify(getJobDef()),
               function(){alert("success");});
      }
  $("#jobform .jobdata").change(refreshJobDef);
  $("#createButton").click(submitJobDef);

  /* Code for extra job attributes (key:value pairs to add at job creation)
   */
  $("#addKeyValButton").click(function(){console.log("IMPLEMENT ADD K/V PAIR"););
}());
