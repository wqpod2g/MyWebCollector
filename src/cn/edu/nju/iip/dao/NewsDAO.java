package cn.edu.nju.iip.dao;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.edu.nju.iip.model.JWNews;

public class NewsDAO extends DAO implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(NewsDAO.class);
	
	private BlockingQueue<JWNews> NewsQueue;
	
	
	public NewsDAO(BlockingQueue<JWNews> NewsQueue) {
		this.NewsQueue = NewsQueue;
	}
	
	public void saveNews(JWNews news) {
		try{
			begin();
			getSession().save(news);
			commit();
		}catch(Exception e) {
			rollback();
			logger.error("saveNews failed!",e);
		}
	}

	public void run() {
		logger.info("****************"+Thread.currentThread().getName()+" NewsDAO线程start！****************");
		while(true) {
			try{
				JWNews news = NewsQueue.take();
				saveNews(news);
			}catch (Exception e) {
				logger.info("NewsDAO run() failed", e);
			}
		}
	}
	
	public static void main(String[] args) {
		BlockingQueue<JWNews> NewsQueue = new LinkedBlockingQueue<JWNews>();
		NewsDAO newsDao = new NewsDAO(NewsQueue);
		Thread newsDaoThread = new Thread(newsDao);
		newsDaoThread.start();
	}

}
