(function() {
  var layouts = $(".content");
  $("#homeLink").click(function(){ layouts.hide(); $("#homeLayout").show(); });
  $("#aboutLink").click(function(){ layouts.hide(); $("#aboutLayout").show(); });
  $("#createJobLink").click(function(){ layouts.hide(); $("#createJobLayout").show(); });
  $("#deleteJobLink").click(function(){ layouts.hide(); $("#deleteJobLayout").show(); });
  $("#statusLink").click(function(){ layouts.hide(); $("#statusLayout").show(); });
  layouts.hide();
  $("#homeLayout").show();
}());
