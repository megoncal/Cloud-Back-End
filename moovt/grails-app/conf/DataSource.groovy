dataSource {
 pooled = true
 //driverClassName = "com.mysql.jdbc.Driver"
 //dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
 driverClassName = "org.postgresql.Driver"
 dialect = org.hibernate.dialect.PostgreSQLDialect
 }
 
hibernate {
 cache.use_second_level_cache = false
 cache.use_query_cache = falsen
 cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
} // environment specific settings
 
//environments {
// development {
//  dataSource {
//   dbCreate = "create-drop" // one of 'create', 'create-drop','update'
//   url = "jdbc:mysql://localhost/MOOVTDEV?useUnicode=yes&characterEncoding=UTF-8"
//   username = "dev"
//   password = "devpw"
//  }
//  hibernate {
//   show_sql = true
//  }
// }
// test {
//  dataSource {
//   dbCreate = "create-drop" // one of 'create', 'create-drop','update'
//   url = "jdbc:mysql://localhost/MOOVTTEST?useUnicode=yes&characterEncoding=UTF-8"
//   username = "test"
//   password = "testpw"
//  }
// }
// production {
//  dataSource {
//   dbCreate = "create-drop"
//   url = "jdbc:mysql://localhost/MOOVTPROD?useUnicode=yes&characterEncoding=UTF-8"
//   username = "prod"
//   password = "prodpw"
//  }
//  hibernate {
//	  show_sql = true
//	 }
// }
//}
environments {
	development {
	 dataSource {
	  dbCreate = "create-drop" // one of 'create', 'create-drop','update'
	  url = "jdbc:postgresql://localhost:5432/MOOVTDEV"
	  username = "dev"
	  password = "devpw"
	 }
	 hibernate {
	  show_sql = true
	 }
	}
	test {
	 dataSource {
	  dbCreate = "create-drop" // one of 'create', 'create-drop','update'
	  url = "jdbc:mysql://localhost/MOOVTTEST?useUnicode=yes&characterEncoding=UTF-8"
	  username = "test"
	  password = "testpw"
	 }
	}
	production {
	 dataSource {
	  dbCreate = "create-drop"
	  url = "jdbc:mysql://localhost/MOOVTPROD?useUnicode=yes&characterEncoding=UTF-8"
	  username = "prod"
	  password = "prodpw"
	 }
	 hibernate {
		 show_sql = true
		}
	}
   }