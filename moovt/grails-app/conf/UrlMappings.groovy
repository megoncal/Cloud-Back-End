class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}


		"/"(controller: "login")
		"/blank"(view:"/blank")
		
		"500"(view:'/error')
		
		
	}
}