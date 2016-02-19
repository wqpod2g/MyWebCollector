package cn.edu.nju.iip.model;

public class JWNews {

	private int id;
	private String url;
	private String title;
	private String content;
	// 来源
	private String source;
	// 正面还是负面
	private int sentiment;
	// 抓取时间
	private String crawltime;
	//标签
	private String tag;
	
	
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getSentiment() {
		return sentiment;
	}

	public void setSentiment(int sentiment) {
		this.sentiment = sentiment;
	}

	public String getCrawltime() {
		return crawltime;
	}

	public void setCrawltime(String crawltime) {
		this.crawltime = crawltime;
	}

}
