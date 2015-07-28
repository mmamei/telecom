package utils.time;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class TimeConverter implements Serializable {
	public static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
	
	public static final String start_date = "2015-03-01:0:0:0";
	public static final String end_date = "2015-04-30:23:59:59";
	
	public Calendar start;
	public Calendar end;
	public long startTime;
	public long endTime;
	public int time_size;
	
	
	private static TimeConverter tc = null;
	
	public static TimeConverter getInstance() throws Exception { 
		if(tc == null)
			//tc = new TimeConverter15Mins(start_date, end_date);
			tc = new TimeConverterHour(start_date, end_date);
			//tc = new TimeConverterHour1Day(start_date, end_date);
		    //tc = new TimeConverterDay(start_date, end_date);
		
		return tc;
	}
	
	
	public String getName(){
		return this.getClass().getSimpleName();
	}

	public abstract int getTimeSize();
	public abstract long index2time(int index);
	public abstract int time2index(long time); 
	public abstract SimpleDateFormat getSDF();
	
	public String print(long time) {
		try {
			return getSDF().format(new Date(time));
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public boolean contains(Calendar cal) {
		return !cal.before(start) && !cal.after(end);
	}
	
	public boolean contains(long time) {
		return startTime <= time && time <= endTime; 
	}
	
	
	public String[] getTimeLabels() {
		String[] tl = new String[getTimeSize()];
		for(int i=0; i<tl.length;i++)
			tl[i] = print(index2time(i));
		return tl;
	}
	
	// main for testing purposes
	public static void main(String[] args) throws Exception  {
		TimeConverter tc = TimeConverter.getInstance();
		System.out.println(tc);
		System.out.println(tc.getTimeSize());
		Calendar c = new GregorianCalendar(2015,Calendar.MARCH,1,0,15,10);
		int index = tc.time2index(c.getTimeInMillis());
		System.out.println(c.getTime()+" --> "+index+" --> "+tc.print(tc.index2time(index)));
		c = new GregorianCalendar(2015,Calendar.APRIL,30,23,50,10);
		index = tc.time2index(c.getTimeInMillis());
		System.out.println(c.getTime()+" --> "+index+" --> "+tc.print(tc.index2time(index)));
		
		
		/*
		c = new GregorianCalendar(2015,Calendar.MARCH,1,0,15,10);
		for(int i=0;i<5856;i++) {
			index = tc.time2index(c.getTimeInMillis());
			System.out.println(c.getTime()+" --> "+index+" --> "+new Date(tc.index2time(index)));
			c.add(Calendar.MINUTE, 15);
		}
		*/
		
	}
	
}
