package moe.komi.crawler;

import java.io.Serializable;

public class Image implements Serializable {

	public String src;

	public String th;
	
	public Image(String src, String th) {
		this.src = src;
		this.th = th;
	}
}
