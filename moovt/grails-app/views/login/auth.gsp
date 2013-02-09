<div class="clTab" id="signin">
	<div class="clNewsImage" id="newsImage">
		<a href="/"><img
			src="${resource(dir: 'images', file: 'world-globe_full.png')}"
			alt="Global" /></a>
	</div>


	<div class="clSigninForm" id="signinForm">
		<form method="POST" action='http://localhost:8080/moovt/j_spring_security_check'>
			<div class="clFormTitle">Sign in</div>
			<div class="clFormRow">
				<div class="clFormField">
					<label for="company">Company</label> <input
						class="k-textbox clInputLarge" id="company" name="company"
						type="text" tabindex="1" />
				</div>
			</div>
			<div class="clFormRow">
				<div class="clFormField">
					<label for="username">User Name</label> <input
						class="k-textbox clInputLarge" id="username" name="j_username"
						type="text" tabindex="1" />
				</div>
			</div>
			<div class="clFormRow">
				<div class="clFormField">
					<label for="password">Password</label> <input
						class="k-textbox clInputLarge" id="password" name="j_password"
						type="password" tabindex="2" />
				</div>
			</div>

			<div class="clDoubleEmptyRow"></div>

			<div id="messageArea" class="clMessage"></div>

			<div class="clDoubleEmptyRow"></div>

			<div class="clFormRow">
				<div class="clFormField">
					
					<%  onclick="jQuery.ajax({type:'POST', data:jQuery(this).parents('form:first').serialize(),  url:'http://localhost:8080/moovt/j_spring_security_check',   	 success:function(data,textStatus){handleSignin(data,textStatus);},          	 error:function(XMLHttpRequest,textStatus,errorThrown){}});    	 return false"	%>
					
					
					<input type="submit" value="Sign In" class="k-button" />


					<div class="clCheckbox">
						<label onclick=""> <input type="checkbox"
							name="PersistentCookie" id="PersistentCookie" value="yes"
							<g:if test='${hasCookie}'>checked='checked'</g:if> tabindex="4" />
							<strong class="clRememberLabel"> Stay signed in </strong>
						</label>
					</div>
				</div>
			</div>
		</form>
		<a href=""> Can't access your account? </a>
	</div>
<script>
/**
 * Handle the Sign In action 
 */
function handleSignin(data, textStatus) {

	log.info("handleSignin "+ data.code);
	//TODO: Why is this initialization Required? 
	$("#tbsMain").kendoTabStrip();
	var tabStrip = $("#tbsMain").data("kendoTabStrip");

	if (data.code == "ERROR") {
		$("#messageArea").text(data.msg);
	}

	if (data.code == "SUCCESS") {
		removeTab("signin");
		removeTab("news");
		addTab("Dashboard","dashboard","${g.createLink(controller:'login',action:'showDashboard')}");
		refreshSigninDiv();
		refreshMenuDiv();
		
	}
}

</script>
</div>


