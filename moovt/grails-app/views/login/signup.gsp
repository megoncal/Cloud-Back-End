<div class="clTab" id="signin">
	<div class="clNewsImage" id="newsImage">
		<a href="/"><img
			src="${resource(dir: 'images', file: 'world-globe_full.png')}"
			alt="Global" /></a>
	</div>


	<div class="clSigninForm" id="signinForm">
		<form id="signupForm">
			<div class="clFormTitle">Sign up</div>
			<!--
			<div class="clFormRow">
				<div class="clFormField">
					<label for="company">Company</label> 
				</div>
			</div>
			-->
			<input id="signUptenantname" name="signUptenantname" type="hidden" value="naSavassi" /> 
			<input id="signUplocale" name="signUplocale" type="hidden"	value="en_US" />
			<div class="clFormRow">
				<div class="clFormField">
					<label for="username">User Name</label> <input
						class="k-textbox clInputLarge" id="signUpusername" name="signUpusername"
						type="text" tabindex="11" value="megoncal" required />
				</div>
			</div>
			<div class="clFormRow">
				<div class="clFormField">
					<label for="email">Email</label> <input
						class="k-textbox clInputLarge" id="signUpemail" name="signUpemail"
						type="email" tabindex="12" value="megoncal@gmail.com" required
						data-email-msg="Email format is not valid" />
				</div>
			</div>
			<div class="clFormRow">
				<div class="clFormField">
					<label for="password">Password</label> <input
						class="k-textbox clInputLarge" id="signUppassword" name="signUppassword"
						type="password" tabindex="13" value="1234" required />
				</div>
			</div>

			<div id="signupMessageArea" class="clMessage"></div>

			<div class="clDoubleEmptyRow">&nbsp;</div>


			<div class="clFormRow">
				<div class="clFormField">
					<input onclick="saveUser(this); return false" type="submit"
						tabindex="14" value="Create User" class="k-button" />

				</div>
			</div>
		</form>
	</div>
<script>

var validator = $("#signupForm").kendoValidator().data("kendoValidator");

/**
 * Handle click on the Save button 
 */   
function saveUser(theButton) {
	log.info("Save user clicked");
	if (validator.validate()) {
		var userInstance =  { "tenantname" : $("#signUptenantname").val(), "username" : $("#signUpusername").val(), "email" : $("#signUpemail").val(), "password" : $("#signUppassword").val(), "locale":$("#signUplocale").val()};
        jQuery.ajax({type:'POST',
        	 data: JSON.stringify(userInstance),
        	 contentType: 'application/json',
            url:"${g.createLink(controller:'user',action:'createUserInExistingTenant')}",
        	 success:function(data,textStatus){handleSignupResponse(data,textStatus);},
        	 error:function(XMLHttpRequest,textStatus,errorThrown){log.info("Error from login call"+errorThrown)}});
	}
}

/**
 * Handle the Sign Up Response 
 */
function handleSignupResponse(data, textStatus) {

	log.info("Handling Signup Response "+ data.type+":"+ data.code+":"+data.message);
	//TODO: Why is this initialization Required? 
	$("#tbsMain").kendoTabStrip();
	var tabStrip = $("#tbsMain").data("kendoTabStrip");

	if (data.code == "ERROR") {
		$("#signupMessageArea").text(data.message);
	}

	if (data.code == "SUCCESS") {
		//Automatically log this user in
		var userInstance =  { "tenantname" : $("#signUptenantname").val(), "username" : $("#signUpusername").val(), "password" : $("#signUppassword").val(), "locale":$("#signUplocale").val()}

		//Authenticate user
		//The handleSigninResponse (will be successful) can be found in the signin form
		jQuery.ajax({ 	type:'POST',
   	 				  	data: JSON.stringify(userInstance),
   	 				  	contentType: 'application/json',
       					url:"${g.createLink(controller:'login',action:'authenticateUser')}",
   	 				    success:function(data,textStatus){handleSigninResponse(data,textStatus);},
   	 					error:function(XMLHttpRequest,textStatus,errorThrown){log.info("Error from login call"+errorThrown)}});
   	 
	}
}





</script>
</div>


