package info.anthonywang.zybookautocompleter;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

class DriverLog{
	private Queue<LogRecord> Log = new ArrayBlockingQueue<LogRecord>(1000);

	public void log(LogRecord record){
		Log.add(record);
	}

	public void logDebug(String message){
		Log.add(new LogRecord(Level.DEBUG, message));
	}

	public void logInfo(String message){
		Log.add(new LogRecord(Level.INFO, message));
	}

	public void logError(String message){
		Log.add(new LogRecord(Level.INFO, message));
	}

	public LogRecord peek(){
		return Log.peek();
	}

	LogRecord take(){
		return Log.remove();
	}
}

enum Level{DEBUG, INFO, ERROR}

class LogRecord{
	private Date timestamp;
	private Level level;
	private String message;

	public LogRecord(Level level, String message){
		this.timestamp = new Date();
		this.level = level;
		this.message = message;
	}

	public Date getTimestamp(){
		return timestamp;
	}

	public Level getLevel(){
		return level;
	}

	public String getMessage(){
		return message;
	}
}