(function () {
  /* The logic to realize a job deletion:
   */
  var deleteJob = function(jobName,force) {
    $.ajax({url:"/jobs/"+jobName+"?force="+(force ? "TRUE" : "FALSE"),
            type: "DELETE",
            dataType:"text",
            success: function(){alert("Success");
                                refreshJobList();},
            error: function() {alert("Failure");
                               refreshJobList();}});
  },
      sendDeletion = function() {
        var jobName = $("#jobNameSelect").val();
        if(jobName == "") return;
        deleteJob(jobName, false);
      },
      forceDeletion = function() {
        var jobName = $("#jobNameSelect").val();
        if(jobName == "") return;
        deleteJob(jobName, true);
      },
      getNamesStatus = function(jobs) {
        var names = [],
            status = [];
        for(var i = 0; i < jobs.length; i++) {
          names.push(jobs[i]['name']);
          status.push(jobs[i]['status']);
        }
        return {'names':names, 'statuses':status};
      },
      refreshJobList = function() {
        var jobLst;
        $.get("/jobs","",function(data){ jobLst = JSON.parse(data);
                                         var ns = getNamesStatus(jobLst);
                                         updateNameList(ns);
                                       });
      },
      updateNameList = function(ns) {
        var names = ns['names'],
            status = ns['statuses'],
            selector = $("#jobNameSelect");
        selector.empty();
        selector.append("<option value=\"\">Select one</option>");
        for(var i=0; i < names.length; i++) {
          var opText = "<option value=\""+names[i]+"\">"+names[i]+" : "+status[i]+"</option>";
          selector.append(opText);
        }
        $("#jobNameSelect").trigger("chosen:updated");
      };
  $("#deleteJobLink").click(refreshJobList);
  $("#deleteJobButton").click(sendDeletion);
  $("#deleteForceButton").click(forceDeletion);
  $("#refreshJobListButton").click(refreshJobList);
}());
