package cn.edu.nju.iip.spider;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import cn.edu.nju.iip.BloomFilter.BloomFactory;
import cn.edu.nju.iip.dao.NewsDAO;
import cn.edu.nju.iip.model.JWNews;
import cn.edu.nju.iip.model.Url;
import cn.edu.nju.iip.util.CommonUtil;

/**
 * Crawling news from hfut news
 * 
 * @author hu
 */
public class NewsCrawler extends BreadthCrawler {

	private static final Logger logger = LoggerFactory.getLogger(NewsCrawler.class);

	private BlockingQueue<JWNews> NewsQueue;

	private static BloomFactory bf = BloomFactory.getInstance();

	private static Set<String> seed_set = new HashSet<String>();
	
	private static int count;

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
		count = 0;
		List<Url> list = CommonUtil.importFromXls();
		for (Url url : list) {
			this.addSeed(new CrawlDatum(url.getLink()).putMetaData("source",
					url.getWebname()));
			seed_set.add(url.getLink());
		}
		this.addRegex(".*");
		this.addRegex("-.*\\.(jpg|png|gif).*");
		this.addRegex("-.*\\.com/?");
		this.addRegex("-.*\\.cn/?");
		this.addRegex("-.*\\.net/?");
		this.addRegex("-.*#.*");
	}

	public void visit(Page page, CrawlDatums next) {
		String url = page.getUrl();
		if (bf.contains(url)) {
			return;
		} else {
			if (!seed_set.contains(url)) {
				bf.add(url);
				count++;
				try {
					News news = ContentExtractor.getNewsByHtml(page.getHtml());
					JWNews jwnews = new JWNews();
					jwnews.setContent(news.getContent().trim());
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

	public static void startNewsCrawler(BlockingQueue<JWNews> newsQueue) {
		while (true) {
			logger.info("*************NewsCrawler begin*********************");
			NewsCrawler crawler = new NewsCrawler("crawl", true);
			try {
				crawler.setNewsQueue(newsQueue);
				crawler.setThreads(50);
				crawler.setTopN(50000);
				crawler.setResumable(true);
				crawler.start(2);
				File file = new File("crawl");
				CommonUtil.deleteFile(file);
				bf.saveBloomFilter();
				logger.info("新增新闻:"+count);
				logger.info("*************NewsCrawler finish*********************");
				logger.info("*************start sleep*********************");
				Thread.sleep(3 * 60 * 60 * 1000);
			} catch (Exception e) {
				logger.info("NewsCrawler run() error", e);
			}
		}

	}

	public static void main(String[] args) throws Exception {
		BlockingQueue<JWNews> NewsQueue = new LinkedBlockingQueue<JWNews>();
		ExecutorService service = Executors.newCachedThreadPool();
		NewsDAO newsDao = new NewsDAO(NewsQueue);
		service.execute(newsDao);
		startNewsCrawler(NewsQueue);
	}

}
