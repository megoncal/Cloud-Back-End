<!-- Top  -->
<sec:ifLoggedIn>
	<ul id="mnMainMenu" data-role="menu"
	
		class="k-widget k-reset k-header k-menu k-menu-horizontal">
	    <sec:ifAnyGranted roles="ROLE_ADMIN">	
		<li id="menuItemId" class="k-item k-state-default"><a
			class="k-link" href="" id="aUserMenu"
			onClick="handleUserMenu();return false;">User</a></li>
		</sec:ifAnyGranted>
		<sec:ifAnyGranted roles="ROLE_ADMIN">	
		<li id="menuItemId" class="k-item k-state-default"><a
			class="k-link" href="" id="aRideMenu"
			onClick="handleRideMenu();return false;">Ride</a></li>
		</sec:ifAnyGranted>
	</ul>
	<script>
	   /**
	    *
	    */
	    function handleItemMenu() {
				log.info("Item menu clicked");
				if (!(doesTabExists("itemMain"))) {
					addTab("Items &nbsp;<a class=\"clCloseTab\" onclick='removeTab($(this).closest(\"li\").attr(\"id\"))'>&nbsp;&nbsp;&nbsp;&nbsp;</a>","itemMain","${g.createLink(controller:'item',action:'main')}");
			}

		}

	    /**
	    *
	    */
	    function handleUserMenu() {
				log.info("User menu clicked");
				if (!(doesTabExists("userMain"))) {
					addTab("Users &nbsp;<a class=\"clCloseTab\" onclick='removeTab($(this).closest(\"li\").attr(\"id\"))'>&nbsp;&nbsp;&nbsp;&nbsp;</a>","userMain","${g.createLink(controller:'user',action:'main')}");
			}

		}

	    /**
	    *
	    */
	    function handleRideMenu() {
				log.info("Ride menu clicked");
				if (!(doesTabExists("rideMain"))) {
					addTab("Rides &nbsp;<a class=\"clCloseTab\" onclick='removeTab($(this).closest(\"li\").attr(\"id\"))'>&nbsp;&nbsp;&nbsp;&nbsp;</a>","rideMain","${g.createLink(controller:'ride',action:'main')}");
			}

		}



	    function handleTestMenu() {
			log.info ("Test menu clicked")
			//var obj = new Address("Test");

		}
	</script>

</sec:ifLoggedIn>
