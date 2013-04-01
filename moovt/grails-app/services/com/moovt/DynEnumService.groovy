package com.moovt

class DynEnumService {

	def messageSource;

		public List<DynamicEnum> getDynamicEnum(Object dynEnum, Locale locale) {
			List<DynamicEnum> l = new ArrayList<DynamicEnum>()
			for (item in dynEnum){
				l.add(new DynamicEnum(item.toString(),messageSource.getMessage(item.toString(), null, locale)));
			}
			return l;
		}
	
	
}

