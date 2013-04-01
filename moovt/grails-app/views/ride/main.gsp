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
                                }
                            },
                            pageSize: 12,
                            schema: {
                                model: {
                                    id: "id",
                                    fields: {
                                        version: { editable: false, nullable: true },
                                        rideStatus: { editable: false, nullable: true },
                                        pickupDateTime: { editable: false, nullable: true },
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
			                { field: "rideStatus", title: "Status", width: 40 },
			                { field: "pickupDateTime", title: "Date and Time", width: 80 },
			                { field: "pickUpAddress", title: "Pick up", width: 100 },
			                { field: "dropOffAddress", title: "Drop Off", width: 100 }
						]
                    });
                });
	</script>
</div>