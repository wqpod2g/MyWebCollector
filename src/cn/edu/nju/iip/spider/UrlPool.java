package cn.edu.nju.iip.spider;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.hfut.dmic.contentextractor.ContentExtractor;
import cn.edu.hfut.dmic.contentextractor.News;
import cn.edu.nju.iip.dao.NewsDAO;
import cn.edu.nju.iip.model.JWNews;
import cn.edu.nju.iip.model.Url;
import cn.edu.nju.iip.util.CommonUtil;



public class UrlPool implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(UrlPool.class);
	
	private BlockingQueue<Url> UrlQueue;
	
	private static NewsDAO newsDAO = new NewsDAO();
	
	public UrlPool(BlockingQueue<Url> UrlQueue) {
		this.UrlQueue = UrlQueue;
	}
	
	public String getHTML(String url) {
		String html = null;
		try{
			Document doc = Jsoup.connect(url)
					.userAgent("Mozilla")
					.timeout(3000)
					.get();
			html = doc.html();
		}catch(Exception e) {
			logger.info("getHTML error",e);
		}
		return html;
	}
	
	public void getUrlList(String unitName) {
		String query = unitName;
		try{
			if(query.contains("有限公司")) {
				query = query.substring(0,query.indexOf("有限公司"));
			}
			String source_url = "http://news.so.com/ns?q="+query+"&rank=rank";
			String html = getHTML(source_url);
			if(html==null) return;
			Document doc = Jsoup.parse(html);
			Elements results = doc.select("li.res-list");
			for(Element result:results) {
				String link = result.select("a.news_title").attr("href");
				String source = result.select("span.sitename").text();
				String title = result.select("a.news_title").text();
				logger.info("fetch URL: "+link);
				String newsHtml = getHTML(link);
				if(newsHtml!=null&&newsHtml.contains(query)) {
					News news = ContentExtractor.getNewsByHtml(newsHtml);
					JWNews jwnews = new JWNews();
					jwnews.setContent(news.getContent().trim());
					jwnews.setSentiment(0);
					jwnews.setUrl(link);
					jwnews.setSource(source);
					jwnews.setTitle(title);
					jwnews.setCrawltime(CommonUtil.getTime());
					jwnews.setTag(unitName);
					newsDAO.saveNews(jwnews);
				}
				//UrlQueue.put(url);
			}
		}catch(Exception e) {
			logger.info("getUrlList error",e);
		}
	}
	
	public void run() {
		logger.info("**************UrlPool thread start**************");
		List<String> unitNameList = CommonUtil.importUnitName();
		for(String name:unitNameList) {
			getUrlList(name);
		}
		logger.info("**************UrlPool thread end**************");
	}
	
	public static void main(String[] args) {
		BlockingQueue<Url> UrlQueue = new LinkedBlockingQueue<Url>();
		UrlPool test = new UrlPool(UrlQueue);
		Thread thread = new Thread(test);
		thread.start();
	}

}
