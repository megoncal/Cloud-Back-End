package com.notnoop.apns;

import java.util.Date;
import java.util.Map;

public class PushTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Pushing");
		ApnsService service =
		APNS.newService()
		.withCert("/study/docs/certificates/aps_development.p12", "vilela1983")
		.withSandboxDestination()
		.build();
		
		service.testConnection();
		
		PayloadBuilder payloadBuilder = APNS.newPayload();
		payloadBuilder.alertBody("Can't be simpler than this! Test 1");
		String payload = payloadBuilder.build();
		
		String token =   "9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504";
		                //9a1cd75847e20f1a27132790dfe1a0cb4107f42da1a39c019dd1a0820fc5c504
		service.push(token, payload);
		
		System.out.println("Pushed");

				Map<String, Date> inactiveDevices = service.getInactiveDevices();
		for (String deviceToken : inactiveDevices.keySet()) {
			Date inactiveAsOf = inactiveDevices.get(deviceToken);
			System.out.println("Device inactive as of " + inactiveAsOf);
		}


	}

}
