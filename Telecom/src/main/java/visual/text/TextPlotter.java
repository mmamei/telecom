package visual.text;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.log4j.PropertyConfigurator;
import utils.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


/*
 * NOTA: Se all'inizio compare un ? � il problema del BOM nell'encoding UTF-8.
 * Occorre usare notepad++ e salvare il file del template nella modalit� senza BOM
 */

public class TextPlotter {
	
	private static TextPlotter tp;
	private Configuration cfg;
	private TextPlotter() {
		PropertyConfigurator.configure("Telecom/src/main/resources/log4j.properties");
		cfg = new Configuration(Configuration.getVersion());
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);  
	}
	
	public static TextPlotter getInstance() {
		if(tp == null) tp = new TextPlotter();
		return tp;
	}
	
	
	public void run(Map<String,Object> root, String ftl, String outFile) {		
		try {
			
			for(String k: root.keySet()){
				Object o = root.get(k);
				if(o instanceof String)
					root.put(k, ((String)o).replaceAll("_", "-"));
			}
			
			
			root.put("random", (int)(100*Math.random()));
			
			File f = new File(ftl);
			cfg.setDirectoryForTemplateLoading(f.getParentFile());
			Template temp = cfg.getTemplate(f.getName());
			//Writer out = new OutputStreamWriter(System.out);
			Writer out = new OutputStreamWriter(new FileOutputStream(outFile));
			temp.process(root, out);  
		}catch(Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	
	public static void main(String[] args) {
		Map<String,Object> root = new HashMap<String,Object>();
		root.put("region", "Venicex");
		root.put("time", "March 2014");
		
		String ftl = "ftl/TouristStatistics.ftl";
		String out = Config.getInstance().paper_folder+"/img/TouristStatistics/test.tex";
		TextPlotter tp = TextPlotter.getInstance();
		tp.run(root, ftl, out);
		System.out.println("Done!");
	}
	
}
