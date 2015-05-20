package moe.komi.crawler;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class Post implements Serializable {

	public String title = "無題";

	public String name = "無名";

	public String com = "";

	public String tripId = "";

	public String no = "";

	public String res = "";

	public Calendar date = null;

	public Image image = null;

	public long uuid;
	
	public void setImage(Image image) {
		this.image = image;
	}

    public Post() {
        uuid = UUID.randomUUID().hashCode();
    }

	private void writeObject(ObjectOutputStream oos) throws IOException {
		//oos.defaultWriteObject();
		oos.writeUTF(res);
		//oos.writeUTF(content);
	}

	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
		//ois.defaultReadObject();
		res = ois.readUTF();
		//content = ois.readUTF();
	}

    public void print() {
        System.out.printf("Post No.%s ID:%s Date:%s\n", no, tripId, printDate());
    }

    public String printDate() {
        return (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a")).format(date.getTime());
    }

    public String br2nl() {
        return com.replaceAll("(?i)<br[^>]*>", "\n");
    }

    public void setCom(String com) {
        //Whitelist whitelist = new Whitelist();
        //whitelist.addTags("font", "a", "br");
        this.com = Jsoup.clean(com, Whitelist.basic());
    }
}
