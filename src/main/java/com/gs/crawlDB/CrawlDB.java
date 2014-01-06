package com.gs.crawlDB;

import java.util.HashSet;
import java.util.Set;


import org.apache.commons.pool.impl.GenericObjectPool.Config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.gs.utils.URL;

public final class CrawlDB {
	private static JedisPool tocrawl;
	private static JedisPool crawled;

	/**
	 * @param host
	 * @param port
	 * @param password
	 * @param timeout
	 * @param toCrawlDB 待抓取的数据库编号
	 * @param crawledDB 已经抓取的URL的数据库的编号
	 */
	public CrawlDB(String host, int port, String password, int timeout,
			int toCrawlDB, int crawledDB) {
		tocrawl = new JedisPool(new Config(), host, port, timeout, password,
				toCrawlDB);
		crawled = new JedisPool(new Config(), host, port, timeout, password,
				crawledDB);
	}

	/**
	 * @author GS
	 * @param maxGenerate 每次产生个数
	 * @return
	 */
	public Set<URL> generate(final int maxGenerate) {
		Jedis toj = tocrawl.getResource();
		Jedis edj = crawled.getResource();
		Set<URL> result = new HashSet<URL>();
		for (int i = 0; i <= maxGenerate; i++) {
			URL u = new URL();
			u.url = toj.randomKey();
			if(u.url == null || u.url.equals(""))break;
			u.level = Integer.valueOf(toj.get(u.url));
			toj.del(u.url);
			result.add(u);
			edj.set(u.url, String.valueOf(u.level));
		}
		return result;
	}

	/**
	 * @author GS
	 * @param set 注入URL集合,检测是否已经抓取过
	 */
	public void inject(final Set<URL> set) {
		Jedis toj = tocrawl.getResource();
		Jedis edj = crawled.getResource();
		for (URL u : set) {
			if(edj.exists(u.url)){continue;}//检测是否已经抓取过,已抓取的话不在加入待抓取数据库
			toj.set(u.url, String.valueOf(u.level));
		}
	}
	
	/**
	 * @author GS
	 * @return true-空 false-非空
	 */
	public boolean isEmpty(){
		return tocrawl.getResource().dbSize() == 0?true:false;
	}
}
