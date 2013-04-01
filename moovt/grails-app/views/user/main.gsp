<!-- Top  -->
<div class="clTab" id="userList">

	<div id="grid"></div>

	<script>

			$(document).ready(function () {
                                        
                    var dataSource = new kendo.data.DataSource({
                            transport: {
                                read:  {
                                    url: "${g.createLink(controller:'user',action:'retrieveAllUsersAsJSONP')}",
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