package com.moovt.common

enum LocationType {
	ROOFTOP, RANGE_INTERPOLATED, GEOMETRIC_CENTER, APPROXIMATE
}

class Location {

	String locationName
	String politicalName
	Double latitude
	Double longitude
	LocationType locationType

}
