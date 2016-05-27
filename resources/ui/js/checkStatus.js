(function() {
  var displayStatus = function(jwDict) {
    var node = new PrettyJSON.view.Node({ 
      el:$("#statusResultSpan"), 
      data:jwDict
            });
  };
  var getStatus = function(callback) {
    var res = {},
        done = 0;
    $.get("/jobs","",function(data){ res['jobs'] = JSON.parse(data);
                                     done += 1;
                                     if(done == 2 && callback) callback(res);
                                   });
    $.get("/workers","",function(workers){ res['workers'] = JSON.parse(workers);
                                           done += 1;
                                           if(done == 2 && callback) callback(res); });
  };
  $("#statusLink").click(function(){getStatus(displayStatus);});
}());
