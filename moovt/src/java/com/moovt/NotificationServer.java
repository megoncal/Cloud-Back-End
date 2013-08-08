package com.moovt;

public class NotificationServer implements Runnable {
 
  private NotificationService notificationService;
  
  public NotificationServer() {
 
  }

  /**
   * Main loop of the SMTP server.
   */
  public void run() {
    System.out.println("Notification Server running");
 
      // Server: loop until stopped
      while (true) {
    	  try {
    	    System.out.println("Notification Server waking up");
    	    notificationService.processNotification ();
    	    Thread.sleep(5000);
			System.out.println("Running 2....");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  System.out.println("Running .4...");
    	  
                }
          }

}
