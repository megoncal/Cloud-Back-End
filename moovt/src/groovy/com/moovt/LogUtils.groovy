package com.moovt

import java.io.PrintWriter
import java.io.StringWriter

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LogUtils {

	   protected final static Logger log = LoggerFactory.getLogger(getClass());
	
	    public static String getStackTrace(Throwable t)
	    {
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw, true);
	        t.printStackTrace(pw);
	        pw.flush();
	        sw.flush();
	        return sw.toString();
	    }

		public static void printStackTrace(Throwable t)
		{
			log.info(getStackTrace(t));
			return;
		}
}

