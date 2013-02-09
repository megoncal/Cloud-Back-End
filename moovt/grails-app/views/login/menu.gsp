<!-- Top  -->
<sec:ifLoggedIn>
	<ul id="mnMainMenu" data-role="menu"
		class="k-widget k-reset k-header k-menu k-menu-horizontal">
		<li id="menuItemId" class="k-item k-state-default"><a
			class="k-link" href="" id="aUserMenu"
			onClick="handleUserMenu();return false;">User</a></li>
		<li id="menuItemId" class="k-item k-state-default"><a
			class="k-link" href="" id="aAssetMenu"
			onClick="handleAssetMenu();return false;">Asset</a></li>
		<li id="menuItemId" class="k-item k-state-default"><a
			class="k-link" href="" id="aTestMenu"
			onClick="handleTestMenu();return false;">Test</a></li>
	</ul>
	<script>
	   /**
	    *
	    */
	    function handleAssetMenu() {
				log.info("Asset menu clicked");
				if (!(doesTabExists("assetMain"))) {
					addTab("Assets &nbsp;<a class=\"clCloseTab\" onclick='removeTab($(this).closest(\"li\").attr(\"id\"))'>&nbsp;&nbsp;&nbsp;&nbsp;</a>","assetMain","${g.createLink(controller:'asset',action:'main')}");
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
	   
		
		function handleTestMenu() {
			//alert('Test menu clicked');
			$.ajax({
				url : "http://localhost:8080/mworks/asset/test",
				type : 'POST',
				dataType : 'jsonp',
				contentType : 'application/json',
				data : 'xxxxxxxxxxxxxx',
				success : function(data) {
					log.info('Read');
					;
					alert('data is ' + data);
				}
			});

		}
	</script>

</sec:ifLoggedIn>
