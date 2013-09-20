<!-- Top  -->
<div class="clTab" id="rideList">

	<div id="rideGrid"></div>

	<script>

			$(document).ready(function () {
                                        
                    var rideDataSource = new kendo.data.DataSource({
                            transport: {
                                read:  {
                                    url: "${g.createLink(controller:'ride',action:'retrieveAllRidesAsJSONP')}",
                                    type: "POST",
                                    dataType: "jsonp",
                                    contentType: 'application/json'
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
                                    status: function() {
              								return this.rideStatus.code;
            							},
            						pickUp: function() {
            							    return this.pickUpLocation.locationName;
            						},	
            						dropOff: function() {
            							    return this.dropOffLocation.locationName;
            						},	
                                    fields: {
                                        version: { editable: false, nullable: true },
                                        pickUpDateTime: { editable: false, nullable: true },
                                        pickUpAddress: { editable: false, nullable: true },
                                        dropOffAddress: { editable: false, nullable: true }
                                    }
               					}
                            }
                        });

                   
                    $("#rideGrid").kendoGrid({
                        dataSource: rideDataSource,
                        pageable: true,
                        height: 628,
                        columns: [
							{ field: "id", title: "Id", width: 20 },
			                { field: "version", title: "Version", width: 20 },
			                { field: "status()", title: "Status", width: 40 },
			                { field: "pickUpDateTime", title: "Date and Time", width: 80 },
			                { field: "pickUp()", title: "Pick up", width: 100 },
			                { field: "dropOff()", title: "Drop Off", width: 100 }
						]
                    });
                });
	</script>
</div>