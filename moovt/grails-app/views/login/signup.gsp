<div class="clTab" id="signin">
	<div class="clNewsImage" id="newsImage">
		<a href="/"><img
			src="${resource(dir: 'images', file: 'world-globe_full.png')}"
			alt="Global" /></a>
	</div>


	<div class="clSigninForm" id="signinForm">
		<form id="signupForm">
			<div class="clFormTitle">Sign up</div>
			<div class="clFormRow">
				<div class="clFormField">
					<label for="company">Company</label> <input
						class="k-textbox clInputLarge" id="tenantname" name="tenantname"
						type="text" tabindex="10" value="ACME" required />
				</div>
			</div>
			<div class="clFormRow">
				<div class="clFormField">
					<label for="username">User Name</label> <input
						class="k-textbox clInputLarge" id="username" name="username"
						type="text" tabindex="11" value="megoncal" required/>
				</div>
			</div>
            <div class="clFormRow">
				<div class="clFormField">
					<label for="email">Email</label> <input
						class="k-textbox clInputLarge" id="email" name="email"
						type="email" tabindex="12" value="megoncal@gmail.com" required data-email-msg="Email format is not valid"/>
				</div>
			</div>
			<div class="clFormRow">
				<div class="clFormField">
					<label for="password">Password</label> <input
						class="k-textbox clInputLarge" id="password" name="password"
						type="password" tabindex="13" value="1234" required/>
				</div>
			</div>

			<div id="signupMessageArea" class="clMessage"></div>

			<div class="clDoubleEmptyRow">&nbsp;</div>


			<div class="clFormRow">
				<div class="clFormField">
					<input
						onclick="saveUser(this); return false"
						type="submit" tabindex="14" value="Create User" class="k-button" />

					</div>
				</div>
			</div>
		</form>
	</div>
<script>

var validator = $("#signupForm").kendoValidator().data("kendoValidator");



function serializeObject(form)
{
   var o = {};
   var a = form.serializeArray();
   $.each(a, function() {
       if (o[this.name]) {
           if (!o[this.name].push) {
               o[this.name] = [o[this.name]];
           }
           o[this.name].push(this.value || '');
       } else {
           o[this.name] = this.value || '';
       }
   });
   return o;
};

/**
 * Handle click on the Save button 
 */   
function saveUser(theButton) {
	log.info("Save user clicked");
	if (validator.validate()) {
		 var o = serializeObject(jQuery(theButton).parents('form:first'));
		 var dat = JSON.stringify(o);
		 var url = "${g.createLink(controller:'user',action:'createCompanyAndUser')}";
		 alert (url);
		 jQuery.ajax({type:'POST',
    	 data: dat,
    	 contentType: 'application/json', 
         url:url,
    	 success:function(data,textStatus){handleSignupResponse(data,textStatus);},
    	 error:function(XMLHttpRequest,textStatus,errorThrown){log.info("Save call failed"+errorThrown)}});	        	 			
	}
}

/**
 * Handle the Sign In Response 
 */
function handleSignupResponse(data, textStatus) {

	log.info("Handling Signup Response "+ data.code+":"+data.msg);
	//TODO: Why is this initialization Required? 
	$("#tbsMain").kendoTabStrip();
	var tabStrip = $("#tbsMain").data("kendoTabStrip");

	if (data.code == "ERROR") {
		$("#signupMessageArea").text(data.msg);
	}

	if (data.code == "SUCCESS") {
		removeTab("signup");
		removeTab("news");
		addTab("Dashboard","dashboard","${g.createLink(controller:'login',action:'dashboard')}");
		refreshSigninDiv();
		refreshMenuDiv();
	}

}

</script>
</div>


