<!-- Top  -->
<div class="clTab" id="userList">

	<div id="grid"></div>

	<div id="inputWindow">
		<form method='POST'
			action="${g.createLink(controller:'user',action:'saveImage')}"
			enctype='multipart/form-data' target='submitIFrame'>
			<input type="file" name="image"> 
			<input type="hidden" name="selectedUserId" id="selectedUserId"/>
			<input type="submit" value="Submit" /> 
		</form>
	<!-- TODO: Adjust the source -->	
	<iframe id='submitIFrame' name='submitIFrame' style="display: none" src="${g.createLink(controller:'blank')}"></iframe>	
	</div>
	​


	<script>

			$(document).ready(function () {
                                        
                    var dataSource = new kendo.data.DataSource({
                            transport: {
                                read:  {
                                    url: "${g.createLink(controller:'user',action:'search')}",
                                    type: "POST",
                                    dataType: "jsonp",
                                    contentType: 'application/json'
                                },
                                update: {
                                    url: "${g.createLink(controller:'user',action:'upsert')}",
                                    type: "POST",
                                    dataType: "jsonp",
                                    contentType: 'application/json'
                                },
                                destroy: {
                                    url: "${g.createLink(controller:'user',action:'delete')}",
                                    type: "POST",
                                    dataType: "jsonp",
                                    contentType: 'application/json'
                                },
                                create: {
                                    url: "${g.createLink(controller:'user',action:'upsert')}",
                                    type: "POST",
                                    dataType: "jsonp",
                                    contentType: 'application/json'
                                },
                                parameterMap: function(options, operation) {
                                    if (operation !== "read" && options.models) {
                                        return kendo.stringify(options.models);
                                    } else {
                                    	return JSON.stringify(options);
                                    }
                                }
                            },
                            batch: true,
                            serverPaging: true,
                            serverSorting: true,
                            serverFiltering: true,
                            allowUnsort: true,
                            pageSize: 12,
                            schema: {
                                model: {
                                    id: "id",
                                    fields: {
                                        id: { editable: false, nullable: true },
                                        version: { editable: false, nullable: true },
                                        class: { editable: false, nullable: true },
                                        imageUUID: { editable: true, nullable: true },
                                        companyname: { validation: { required: true } },
                                        username: { validation: { required: true } },
                                        password: { validation: { required: true } },
                                    }
               					}
                            }
                        });

                    $("#inputWindow").kendoWindow({
                            animation: {
                                open: {
                                    effects: "slideIn:down fadeIn",
                                    duration: 500
                                },
                                close: {
                                    effects: "slide:up fadeOut",
                                    duration: 500
                                }
                            },
                            minWidth: 300,
                            modal: true,
                            resizable: true,
                            title: "Choose File to Upload",
                            visible: false,
                    });
                    
                    $("#grid").kendoGrid({
                        dataSource: dataSource,
                        pageable: true,
                        sortable: {
                            mode: "multiple"
                        },
                        filterable: true,
                        height: 628,
                        toolbar: ["create"],
                        columns: [
							{ title: "User", width: 25, template: "#= imageFunc(data) #" },      
			                { field: "companyname", title: "Company Name", width: 100 },
			                { field: "username", title: "User Name", width: 100 },
			                { field: "password", title: "Password", width: 100 },
                            { command: ["edit", "destroy"], title: "&nbsp;", width: "210px" }],
                        editable: "inline"
                    });
                });

				//When the frame loads, use the results to update the grid's datasource
                $("#submitIFrame").load(function() {

    			  	  var responseText = this.contentDocument.body.innerText;
    			  	  if (!responseText) {
    			  	    return;
    			  	  }
                      //TODO: Why JSONP can't be interpreted?
    			  	  var parsedResponse = JSON.parse(responseText);

    			  	  // Change the data of the data source
    			  	  var grid = $('#grid').data('kendoGrid');
    			  	  var selectedUserId = $('#selectedUserId').val();
					  var dataSource = $("#grid").data("kendoGrid").dataSource;
					  
    			  	  dataSource._data[selectedUserId]['imageUUID']=parsedResponse.UUID;
    			  	  dataSource._data[selectedUserId]['dirty']=true;
    			  	  dataSource.data(dataSource._data);

					  //Put the row in edit mode
    			  	  var uid = dataSource._data[selectedUserId]['uid'];
    			  	  var row = grid.tbody.find("tr[data-uid='" + uid + "']");
    			  	  grid.editRow(row);
    			  	  
    			  	  //Clear the content of the iFrame
    			  	  this.contentDocument.body.innerText = '';

    			  	  //Close the window
					  $("#inputWindow").data("kendoWindow").close();
    			 });

               //Opens the window and sets the index/id of the clicked row in a window hidden field
               function openInputWindow(clickedImage){
                   	var grid = $("#grid").data("kendoGrid");
                   	var row = clickedImage.closest("tr");
                    var selectedUserId = row.index();
                    log.info ("The select data grid row index is " + selectedUserId);
                	var win = $("#inputWindow").data("kendoWindow");
                	$('#selectedUserId').val(selectedUserId);
                    win.open();
                }
                 
              //This function transform the image UUID retrieved from the DB into the div fragment
              //with the image
                function imageFunc(model) {
					var file = "${resource(dir: 'images', file: 'openpiceditor.png')}";

					if (model.imageUUID != null) {
						file = "${resource(dir: 'images')}/" + model.imageUUID + ".jpg";
					} 

                    var html = "<div class='clImage' id='userImage'><a href='#'><img class='clImage' id='userImage' onclick='openInputWindow($(this));return false;' src='" + file + "' /></a></div>";
					
                    return html;
                 }
                
              
	</script>
</div>