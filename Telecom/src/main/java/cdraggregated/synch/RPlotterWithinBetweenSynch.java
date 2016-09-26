package cdraggregated.synch;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import org.rosuda.REngine.Rserve.RConnection;

import visual.r.RPlotter;

public class RPlotterWithinBetweenSynch {
	
	
	public static void main(String[] args) {
		drawBoxplot("C:/Users/marco/Desktop/SOCIALCAP/img/all/I-24/withinIvoryCoast1Month.csv",
				    "C:/Users/marco/Desktop/SOCIALCAP/img/all/I-24/betweenIvoryCoast1Month.csv",
				    "","I", "C:/Users/marco/Desktop/SOCIALCAP/img/all/I-24/boxplotIvoryCoast1Month.png");
	}
	
	public static void drawBoxplot(String within_csv, String between_csv, String xlab, String ylab, String file) {
		
		RPlotter.FONT_SIZE = 18;
		
		RConnection c = null;
		try {
			c = new RConnection();// make a new local connection on default port (6311)
            
			
			String code = ""+

			"library(ggplot2);"+
			"library(reshape2);"+
			"library(maptools);"+


			//"#load data for italian provinces"+
			//-----"italy_map <- map_data('italy');"+
			//"#eliminate capital letters for names matching"+
			//-----"italy_map$region <- tolower(italy_map$region);"+

			//"# provinces names"+
			//"print(unique(italy_map$region))"+
			//"#create a unique value for longitude of the province"+
			//-----"lat <- aggregate(lat ~ region, data=italy_map, FUN=max);"+

			"wt_sync = read.csv('"+within_csv+"', header = FALSE);"+
			"bt_sync = read.csv('"+between_csv+"', header = FALSE);"+

			"names(wt_sync) <- c('region','wt_sync');"+
			"names(bt_sync)<- c('region','bt_sync');"+

			"sync <- list(bt_sync,wt_sync);"+
			"df.m <- melt(sync, id.var = 'region');"+

			//-----"df.m <-merge(lat,df.m,by='region',all.x = TRUE);"+
			//-----"df.m <- df.m[complete.cases(df.m),];"+

			//-----"o = rev(levels(with(df.m,reorder(region,lat))));"+

			
			//-----"box_sync <- ggplot(data = df.m, aes(x=factor(region,levels=o), y=value), order=as.numeric(long)) + geom_boxplot(aes(fill=variable))+"+
			"box_sync <- ggplot(data = df.m, aes(x=factor(region), y=value)) + geom_boxplot(aes(fill=variable))+"+

			"labs(x = '"+xlab+"',y='"+ylab+"')  +  theme_bw(base_size = "+RPlotter.FONT_SIZE+") + theme(legend.title=element_blank(),legend.position=c(0,1),  legend.justification = c(0,1), axis.text.x = element_text(angle = 90, hjust = 1));"+ 
			
			"ggsave(box_sync, file='"+file+"', scale=2);";
			
			
            System.out.println(code.replaceAll(";", ";\n"));
            c.eval(code);
            c.close();
            if(RPlotter.VIEW) Desktop.getDesktop().open(new File(file));
            
            
        } catch (Exception e) {
        	e.printStackTrace();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        	c.close();
        }      
	}
}
