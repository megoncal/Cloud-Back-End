dataSource {
 pooled = true
 driverClassName = "com.mysql.jdbc.Driver"
 dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
}
 
hibernate {
 cache.use_second_level_cache = true
 cache.use_query_cache = true
 cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
} // environment specific settings
 
environments {
 development {
  dataSource {
   dbCreate = "create-drop" // one of 'create', 'create-drop','update'
   url = "jdbc:mysql://localhost/MWORKSDEV?useUnicode=yes&characterEncoding=UTF-8"
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
   url = "jdbc:mysql://localhost/MWORKSTEST?useUnicode=yes&characterEncoding=UTF-8"
   username = "test"
   password = "testpw"
  }
 }
 production {
  dataSource {
   dbCreate = "create-drop"
   url = "jdbc:mysql://localhost/MWORKSPROD?useUnicode=yes&characterEncoding=UTF-8"
   username = "prod"
   password = "prodpw"
  }
  hibernate {
	  show_sql = true
	 }
 }
}
