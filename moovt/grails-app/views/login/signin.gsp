<!-- Top  -->
<sec:ifNotLoggedIn>
		<form>
			<div class="clSigninRow">
					<input class="k-textbox clInputMedium" id="tenantname" name="tenantname"
						type="text" tabindex="1" placeholder="Company Name" value=""/> 
					<input
						class="k-textbox clInputMedium" id="username" name="username"
						type="text" tabindex="2" placeholder="User Name" value=""/> 
					<input
						class="k-textbox clInputMedium" id="password" name="password"
						type="password" tabindex="3" placeholder="Password" value=""/>
			<input
				onclick="signinUser(this); return false;"
				type="submit" value="Sign In" tabindex="4" class="k-button" /> 
			<input
				type="submit" class="k-button" name="btn-signup" disabled=true
				onclick="signupUser(); return false;" id="btn-signin" tabindex="5" value="Sign Up" />
			<div id="signinMessageArea" class="clSigninMessage"></div>	
			</div>
		</form>


</sec:ifNotLoggedIn>

<sec:ifLoggedIn>
	<div class="clTopRightUser">
		<sec:username />
		<a href="" id="aSignOutId" onClick="return handleSignout()">(sign
			out)</a>
	</div>
</sec:ifLoggedIn>

<script>				
   		   /**
			* This function checks for the existence of a tab in the TabStrip called "signin"
			* and adds one if the tab does not exist
			* The HTML fragment is required to handle the closing of the tab
			*
			* Note: The scripts for controlling tab can be found in the index.gsp file
			*/

		function signupUser() {
			log.info("Signup button clicked");
			if (!(doesTabExists("signup"))) {
					addTab("Sign Up &nbsp;<a class=\"clCloseTab\" onclick='removeTab($(this).closest(\"li\").attr(\"id\"))'>&nbsp;&nbsp;&nbsp;&nbsp;</a>","signup","${g.createLink(controller:'login',action:'signup')}");
			}
		}

		/**
		 * Handle click on the Sign In button 
		 */   
		function signinUser(theButton) {
			log.info("Signin button clicked " + jQuery(theButton).parents('form:first').serialize());
			//jQuery.ajax({type:'POST',
	        //	 data:jQuery(theButton).parents('form:first').serialize(), 
	        //    url:'./j_spring_security_check',
	        //	 success:function(data,textStatus){handleSigninResponse(data,textStatus);},
	        //	 error:function(XMLHttpRequest,textStatus,errorThrown){log.info("Error from login call"+errorThrown)}});
	        
	        //Build and stringify a user object
	        var userInstance =  { "type" : "Self", "tenantname" : $("#tenantname").val(), "username" : $("#username").val(), "password" : $("#password").val()} 	        	 			
	        jQuery.ajax({type:'POST',
	        	 data: JSON.stringify(userInstance),
	        	 contentType: 'application/json',
	            url:"${g.createLink(controller:'login',action:'authenticateUser')}",
	        	 success:function(data,textStatus){handleSigninResponse(data,textStatus);},
	        	 error:function(XMLHttpRequest,textStatus,errorThrown){log.info("Error from login call"+errorThrown)}});
		}

		/**
		 * Handle the Sign In Response 
		 *
		 * This is used from the signin form (this) as well as signup 
		 */
		function handleSigninResponse(data, textStatus) {

			log.info("Handling Signin Response : "+ data);
			var pdata = JSON.parse(data);
			log.info("Handling Signin Response : "+ pdata.result.code);
			//TODO: Why is this initialization Required? 
			$("#tbsMain").kendoTabStrip();
			var tabStrip = $("#tbsMain").data("kendoTabStrip");

			if (pdata.result.code == "ERROR") {
				$("#signinMessageArea").text(pdata.result.message);
			}

			if (pdata.result.code == "SUCCESS") {
				removeTab("signin");
				removeTab("news");
				removeTab("signup");
				addTab("Dashboard","dashboard","${g.createLink(controller:'login',action:'dashboard')}");
				refreshSigninDiv();
				refreshMenuDiv();
				
			}
		}
</script>