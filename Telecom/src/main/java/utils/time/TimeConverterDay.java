package utils.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeConverterDay extends TimeConverter {
	
	
	
	TimeConverterDay(String start_date, String end_date) throws Exception {
		start = Calendar.getInstance();
		start.setTime(F.parse(start_date));
		end = Calendar.getInstance();
		end.setTime(F.parse(end_date));
		this.startTime = start.getTimeInMillis();
		this.endTime = end.getTimeInMillis();
		time_size = (int)Math.ceil((1.0 * (endTime - startTime) / (24 * 1000 * 3600)));
	}
	

	
	public int getTimeSize() {
		return time_size;
	}
	
	public int time2index(long time) {
		int index = (int)((time - startTime)/(24 * 1000 * 3600));
		return index;
	}
	
	public long index2time(int index) {
		long t = startTime + (long)index * (24 * 1000 * 3600);
		return t;
	}
	
	public SimpleDateFormat getSDF() {
		return new SimpleDateFormat("yyyy-MM-dd");
	}
	
	public String toString() {
		return "Daily Time Converter From "+start.getTime()+" To "+end.getTime() +", From " + startTime + " To " + endTime;
	}
}
