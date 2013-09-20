<!-- Top  -->
<div class="clTab" id="userList">

	<div id="grid"></div>

	<script>

			$(document).ready(function () {
                              
                              
var people =
  { "programmers": [
    { "firstName": "Brett", "lastName":"McLaughlin", "email": "brett@newInstance.com" },
    { "firstName": "Jason", "lastName":"Hunter", "email": "jason@servlets.com" },
    { "firstName": "Elliotte", "lastName":"Harold", "email": "elharo@macfaq.com" }
   ],
  "authors": [
    { "firstName": "Isaac", "lastName": "Asimov", "genre": "science fiction" },
    { "firstName": "Tad", "lastName": "Williams", "genre": "fantasy" },
    { "firstName": "Frank", "lastName": "Peretti", "genre": "christian fiction" }
   ],
  "musicians": [
    { "firstName": "Eric", "lastName": "Clapton", "instrument": "guitar" },
    { "firstName": "Sergei", "lastName": "Rachmaninoff", "instrument": "piano" }
   ]
  }          
  var sp = JSON.stringify (people);
  
  log.info("Here " + sp);
  
 
  
                    var dataSource = new kendo.data.DataSource({
                            transport: {
                                read:  {
                                    url: "${g.createLink(controller:'user',action:'retrieveAllUsersAsJSONP')}",
                                    type: "POST",
                                    dataType: "jsonp",
                                    contentType: "text/plain",
                                    data: ""
                                },
                                parameterMap: function(options, operation) {
                                    if (operation !== "read" && options.models) {
                                        return kendo.stringify(options.models);
                                    } else {
                                    	return "{}";
                                    }
                                }
                             },
                            pageSize: 12,
                            schema: {
                                model: {
                                    id: "id",
                                    fields: {
                                        version: { editable: false, nullable: true },
                                        firstName: { editable: false, nullable: true },
                                        lastName: { editable: false, nullable: true },
                                        email: { editable: false, nullable: true },
                                        phone: { editable: false, nullable: true }
                                    }
               					}
                            }
                        });

                   
                    $("#grid").kendoGrid({
                        dataSource: dataSource,
                        pageable: true,
                        height: 628,
                        columns: [
			                { field: "id", title: "Id", width: 20 },
			                { field: "version", title: "Version", width: 20 },
			                { field: "firstName", title: "First Name", width: 80 },
			                { field: "lastName", title: "Last Name", width: 80 },
			                { field: "email", title: "email", width: 100 },
			                { field: "phone", title: "Phone", width: 40 }
						]
                    });
                });
	</script>
</div>