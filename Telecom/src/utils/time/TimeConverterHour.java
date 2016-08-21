package utils.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeConverterHour extends TimeConverter {
	
	
	TimeConverterHour(String start_date, String end_date) throws Exception {
		System.out.println("Creating TimeConverterHour from "+start_date+" to "+end_date);
		start = Calendar.getInstance();
		start.setTime(F.parse(start_date));
		end = Calendar.getInstance();
		end.setTime(F.parse(end_date));
		this.startTime = start.getTimeInMillis();
		this.endTime = end.getTimeInMillis();
		time_size = (int)Math.ceil((1.0 * (endTime - startTime) / (1000 * 3600)));
	}
	
	
	public int getTimeSize() {
		return time_size;
	}

	
	public int time2index(long time) {
		int index = (int)(1.0 * (time - startTime) / (1000 * 3600));
		return index;
	}
	
	public long index2time(int index) {
		long t = startTime + (long)index * (1000 * 3600);
		return t;
	}
	
	public SimpleDateFormat getSDF() {
		return new SimpleDateFormat("yyyy-MM-dd:HH");
	}
	
	public String toString() {
		return "Hourly Time Converter From "+start.getTime()+" To "+end.getTime() +", From " + startTime + " To " + endTime;
	}
}
