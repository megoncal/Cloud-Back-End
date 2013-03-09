<!-- Top  -->
<sec:ifLoggedIn>
	<ul id="mnMainMenu" data-role="menu"
	
		class="k-widget k-reset k-header k-menu k-menu-horizontal">
	    <sec:ifAnyGranted roles="ROLE_ADMIN">	
		<li id="menuItemId" class="k-item k-state-default"><a
			class="k-link" href="" id="aUserMenu"
			onClick="handleUserMenu();return false;">User</a></li>
		</sec:ifAnyGranted>
		<sec:ifAnyGranted roles="ROLE_ITEM_MGR">
		<li id="menuItemId" class="k-item k-state-default"><a
			class="k-link" href="" id="aItemMenu"
			onClick="handleItemMenu();return false;">Item</a></li>
		</sec:ifAnyGranted>
		<li id="menuItemId" class="k-item k-state-default"><a
			class="k-link" href="" id="aTestMenu"
			onClick="handleTestMenu();return false;">Test</a></li> 
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

		/*public class Address {
			String street;
		}
		public class Person {
			String text;
			Address address;
			}
		*/
		function handleTestMenu() {
			log.info ("Test menu clicked")
			//var obj = new Address("Test");

		}
	</script>

</sec:ifLoggedIn>
