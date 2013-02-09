<!DOCTYPE html>
<html>
<head>
<title>Demo</title>

<link href="${resource(dir: 'css', file: 'kendo.common.min.css')}"
	rel="stylesheet">
<link href="${resource(dir: 'css', file: 'kendo-ui.css')}"
	rel="stylesheet">

<link href="${resource(dir: 'css', file: 'mw.main.css')}"
	rel="stylesheet">

<script src="${resource(dir: 'js', file: 'jquery.min.js')}"></script>
<script src="${resource(dir: 'js', file: 'kendo.web.min.js')}"></script>
<script src="${resource(dir: 'js', file: 'log4javascript.js')}"></script>
<script src="${resource(dir: 'js', file: 'mworks.js')}"></script>

</head>
<body>
	<div class="clIndexTopControls">
		<span class="clIndexFloatLeft"> <input id="acSearch" /> <input
			type="submit" class="k-button" name="btnSearch" id="btnSearch"
			value="Search" />
		</span>
		<!-- This next DIV is loaded via ajax call -->
		<div id="divSignin" class="clDivSignin" >
		</div>
	</div>

	<div class="clEmptyRow"></div>

	<div id="divMainMenu">
		<!-- Used for testing only -->
	    <!-- <ul id="mnMainMenu"><li>.</li></ul>--> 
		<!-- -->
	</div>

	<div class="clEmptyRow"></div>
	
	<div id="tbsMain">
		<ul id="ulTbsMain">
		</ul>
	</div>

	<div class="clEmptyRow"></div>

	<div class="clBottomStatus"></div>
	
	<!--  <a href="" id="menuItemId">xxxxx</a> -->



	<script>

		var log = log4javascript.getDefaultLogger();
	
		$(document).ready(function() {

			//Populate the signin div accordingly by calling the controller Signin and the action showSignin
			refreshSigninDiv();
			
			refreshMenuDiv();

			
			$("#tbsMain").kendoTabStrip({
				animation : {
					open : {
						effects : "fadeIn"
					}
				}
			});

			$("#acSearch").kendoAutoComplete({
				minLength : 3,
				dataTextField : "Name",
				dataSource : {
					type : "odata",
					serverFiltering : true,
					serverPaging : true,
					pageSize : 20,
					transport : {
						read : "http://odata.netflix.com/Catalog/Titles"
					}
				}
			 
			});

			<sec:ifNotLoggedIn>
			addTab("News","news","${g.createLink(controller:'login',action:'news')}");
			</sec:ifNotLoggedIn>
			<sec:ifLoggedIn>
			addTab("Dashboard","dashboard", "${g.createLink(controller:'login',action:'dashboard')}");
			</sec:ifLoggedIn>

		});

		
		/**
		 * Refresh the Signin Div in the index.gsp 
		 * @param {String} ajaxUrl
		 */

		function refreshSigninDiv(){
			$.ajax({
			    url:"${g.createLink(controller:'login',action:'signin')}",
			    dataType: 'html',
			    data: {
			    },
			    success: function(data) {
			        $("#divSignin").html(data);        
			    }
			});
		}

		 function refreshMenuDiv(){
				$.ajax({
				    url:"${g.createLink(controller:'login',action:'menu')}",
				    dataType: 'html',
				    async: false,
				    data: {
				    },
				    success: function(data) {
				        $("#divMainMenu").html(data);        
				    }
				});

				$("#mnMainMenu").kendoMenu();
				//$("#mnMainMenu").kendoMenu({dataSource:
				//    [{
				//        text: "Item 1",
				//        url: "http://www.kendoui.com"                // Link URL if navigation is needed, optional.
				//    }]});
					
			}
		 
		//
		/**
		 * Adds a tab to a KendoUI TabStrip
		 * @param {TabStrip} tabStrip 
		 * @param {String} title
		 * @param {String} id
		 */
		function addTab(title, id, url)
		{
		log.info("Adding a Tab with title "+title+", id "+id+" and url " + url);
		var tabStrip = $("#tbsMain").data("kendoTabStrip");	
		tabStrip.append({
			text : title,
			encoded : false,
			contentUrl : url
		});
		tabStrip.tabGroup.children("li:last").attr('id', id);
		tabStrip.select("#"+id);
		}

		/**
		 * Closes a tab is a KendoUI TabStrip identified by id
		 * @param {TabStrip} tabStrip 
		 * @param {String} id
		 */

		function removeTab(id) {
			log.info("Removing Tab " + id);	
			var tabStrip = $("#tbsMain").data("kendoTabStrip");
			tabStrip.remove("#"+id);
			tabStrip.select(tabStrip.tabGroup.children().first())
		}

		/**
		 * Returns true or false depending on the fact that a tab identified by id exists in a KendoUI TabStrip
		 * @param {TabStrip} tabStrip 
		 * @param {String} id
		 * @returns (Boolean) exists
		 */
		function doesTabExists (id){
			var tabID;
			var exists = false;
			var tabStrip = $("#tbsMain").data("kendoTabStrip");
			tabStrip.tabGroup.children().each(function() {
				tabID = $(this).attr("id");
				if (tabID == id) {
					exists = true;
				}
			})
			
			return Boolean(exists);
			
		}

		/**
		 * Handle the Sign Out action 
		 */
		function handleSignout(){
			log.info("Start processing sign out");
			$.ajax({
			    url: "${g.createLink(controller:'login',action:'signout')}",
			    dataType: 'html',
			    data: {
			    },
			    success: function(data) {
					log.info("Successful call to sign user out");
			    	var tabStrip = $("#tbsMain").data("kendoTabStrip");
			    	removeTab("dashboard");
					addTab("News","news","${g.createLink(controller:'login',action:'news')}");  
					refreshSigninDiv();   
					refreshMenuDiv();
			    }
			});
			log.info("Processed sign out");
			return false;
		}

</script>

</body>
</html>
