package cn.edu.nju.iip.spider;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.hfut.dmic.contentextractor.ContentExtractor;
import cn.edu.hfut.dmic.contentextractor.News;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.nju.iip.dao.NewsDAO;
import cn.edu.nju.iip.model.JWNews;
import cn.edu.nju.iip.model.Url;
import cn.edu.nju.iip.util.CommonUtil;
/**
 * Crawling news from hfut news
 * @author hu
 */
public class NewsCrawler extends BreadthCrawler {

	private static final Logger logger = LoggerFactory.getLogger(NewsCrawler.class);
	
	private BlockingQueue<JWNews> NewsQueue;

	/**
	 * @param crawlPath
	 *            crawlPath is the path of the directory which maintains
	 *            information of this crawler
	 * @param autoParse
	 *            if autoParse is true,BreadthCrawler will auto extract links
	 *            which match regex rules from pag
	 */
	public NewsCrawler(String crawlPath, boolean autoParse) {
		super(crawlPath, autoParse);
		List<Url> list = CommonUtil.importFromXls();
		for (Url url : list) {
			this.addSeed(new CrawlDatum(url.getLink()).putMetaData("source",
					url.getWebname()));
		}
		this.addRegex(".*");
		this.addRegex("-.*\\.(jpg|png|gif).*");
		this.addRegex("-.*\\.com/");
		this.addRegex("-.*\\.cn/");
		this.addRegex("-.*\\.net/");
	}

	public void visit(Page page, CrawlDatums next) {
		String url = page.getUrl();
		try {
			News news = ContentExtractor.getNewsByHtml(page.getHtml());
			JWNews jwnews = new JWNews();
			jwnews.setContent(news.getContent());
			jwnews.setSentiment(0);
			jwnews.setSource(page.getMetaData("source"));
			jwnews.setUrl(url);
			jwnews.setTitle(news.getTitle());
			jwnews.setCrawltime(CommonUtil.getTime());
			NewsQueue.put(jwnews);
		} catch (Exception e) {
			logger.info("visit failed", e);
		}
	}

	public void afterVisit(Page page, CrawlDatums next) {
		super.afterVisit(page, next);
		String source = page.getMetaData("source");
		for (CrawlDatum data : next) {
			data.putMetaData("source", source);
		}
	}
	
	public BlockingQueue<JWNews> getNewsQueue() {
		return NewsQueue;
	}

	public void setNewsQueue(BlockingQueue<JWNews> newsQueue) {
		NewsQueue = newsQueue;
	}

	public static void main(String[] args) throws Exception {
		BlockingQueue<JWNews> NewsQueue = new LinkedBlockingQueue<JWNews>();
		NewsDAO newsDao1 = new NewsDAO(NewsQueue);
		ExecutorService service = Executors.newCachedThreadPool();
		service.execute(newsDao1);
		NewsCrawler crawler = new NewsCrawler("crawl", true);
		crawler.setNewsQueue(NewsQueue);
		crawler.setThreads(50);
		crawler.setTopN(50000);
		//crawler.setResumable(true);
		crawler.start(2);
		logger.info("*************NewsCrawler finish*********************");
	}
}
