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
            missingFields += 1;
          }
          dict[e.id] = e.value;
        });
        var keys = $("#jobform .extrajobattr"),
            vals = $("#jobform .extrajobval");
        vals.each(function(i,v) {
          var k = keys[i];
          if(!k.value || k.value == "") return;
          dict[k.value] = v.value;
        });
        readyJob = true;
        if(missingFields > 0) readyJob = false;
        return dict;
      },
      refreshJobDef = function() {
        var dict = getJobDef();
        var node = new PrettyJSON.view.Node({
          data:dict,
          el:$("#createJobDefSpan")
          });
      },
      submitJobDef = function() {
        if(!readyJob) {
          console.log("Job can't be submitted. The definition is not ready.");
          return;
        }
        $.post("/jobs", JSON.stringify(getJobDef()),
               function(){alert("success");});
      }
  $("#jobform .jobdata").change(refreshJobDef);
  $("#createButton").click(submitJobDef);
  $("#resetButton").click(function(){$("#jobform .jobdata").val(""); 
                                     $("#jobform .extrajobdata").remove();
                                     refreshJobDef();});

  /* Code for extra job attributes (key:value pairs to add at job creation)
   */
  var extraAppendString = 
'<div class="pure-control-group">\
    <input id="" class="extrajobdata extrajobattr" placeholder="Attribute"></textarea>\
    <input id="" class="extrajobdata extrajobval" placeholder="Value"></textarea>\
</div>';
  var addKeyValue = function() {
    $("#newKVPairs")
      .append(extraAppendString)
      .change(refreshJobDef);
  };
  $("#addKeyValButton").click(addKeyValue);
}());
