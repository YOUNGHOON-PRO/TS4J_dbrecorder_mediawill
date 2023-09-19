package com.dbrecorder;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DBRecorderDaemon implements Daemon{

  private static final Logger LOGGER = LogManager.getLogger(DBRecorderDaemon.class.getName());
	
  DBRecorder dbRecorder;

  public DBRecorderDaemon() {
  }

  public void init(DaemonContext context) throws Exception {
   println("DBRecorderDaemon instance: init()");
 }

 public void start() {
   println("DBRecorderDaemon instance: start(): in");

   dbRecorder = new DBRecorder();
   dbRecorder.main(new String[1]);

   println("DBRecorderDaemon instance: start(): out");
 }

 public void stop() throws Exception {
   println("DBRecorderDaemon instance: stop(): in");

   dbRecorder.shutdown();

   println("DBRecorderDaemon instance: stop(): out");
 }

 public void destroy() {
   println("DBRecorderDaemon instance: destroy(): in");

   println("DBRecorderDaemon instance: destroy(): out");
 }

 private String getCurrentTime() {
   java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(
       "yyyy/MM/dd HH:mm:ss", java.util.Locale.US);
   return fmt.format(new java.util.Date());
 }

 private void println(String msg) {
   LOGGER.info(getCurrentTime() + " : " + msg);
 }

}
