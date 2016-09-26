package utils.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeConverter15Mins extends TimeConverter {
	
	
	TimeConverter15Mins(String start_date, String end_date) throws Exception {
		start = Calendar.getInstance();
		start.setTime(F.parse(start_date));
		end = Calendar.getInstance();
		end.setTime(F.parse(end_date));
		this.startTime = start.getTimeInMillis();
		this.endTime = end.getTimeInMillis();
		time_size = (int)Math.ceil((1.0 * (endTime - startTime) / (1000 * 3600 / 4)));
	}
	
	
	public int getTimeSize() {
		return time_size;
	}
	
	
	public int time2index(long time) {
		int index = (int)(1.0 * (time - startTime)/(1000 * 3600 / 4));
		return index;
	}
	
	public long index2time(int index) {
		long t = startTime + (long)index * (1000 * 3600 / 4);
		return t;
	}
	
	public SimpleDateFormat getSDF() {
		return new SimpleDateFormat("yyyy-MM-dd:HH:mm");
	}
	
	public String toString() {
		return "15 Mins Time Converter From "+start.getTime()+" To "+end.getTime() +", From " + startTime + " To " + endTime;
	}
}
