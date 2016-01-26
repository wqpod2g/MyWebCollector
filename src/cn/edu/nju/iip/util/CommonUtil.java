package cn.edu.nju.iip.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.nju.iip.model.Url;

public class CommonUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	
	/**
	 * 从excel导入url信息
	 */
	public static List<Url> importFromXls()
	{
        Workbook workbook = null;
        List<Url> url_list = new ArrayList<Url>();
		try {
			workbook=Workbook.getWorkbook(new File(System.getProperty("user.dir")+"/resources/url_source.xls"));
		} catch (Exception e) {
			logger.error("importFromXls error!",e);
		}
		Sheet sheet=workbook.getSheet(2);
		int rowCount=sheet.getRows();
		for(int i=1;i<rowCount;i++)
		{
			Url Url = new Url();
			String webname = sheet.getCell(1,i).getContents().trim();
			String link=sheet.getCell(4,i).getContents();
			Url.setLink(link);
			Url.setWebname(webname);
			url_list.add(Url);
		}
		workbook.close();
		return url_list;
	}
	
	 /**
     * 获取当前时间
     * @return
     */
    public static String getTime() {
    	Date now = new Date();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );//可以方便地修改日期格式
    	String time = dateFormat.format(now);
    	return time;
    }
	
	public static void main(String[] args) {
		importFromXls();
	}

}
