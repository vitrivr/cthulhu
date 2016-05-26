(function() {
  var getStatus = function(callback) {
    var res = {},
        done = 0;
    $.get("/jobs","",function(data){ res['jobs'] = JSON.parse(data);
                                     done += 1;
                                     if(done == 2 && callback) callback();
                                   });
    $.get("/workers","",function(workers){ res['workers'] = JSON.parse(workers);
                                           done += 1;
                                           if(done == 2 && callback) callback(); });
  };
  $("#statusLink").click(getStatus);
}());
