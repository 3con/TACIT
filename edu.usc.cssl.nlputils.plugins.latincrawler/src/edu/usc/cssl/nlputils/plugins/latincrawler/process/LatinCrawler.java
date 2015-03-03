/**
 * @author Niki Parmar <nikijitp@usc.edu>
 */

package edu.usc.cssl.nlputils.plugins.latincrawler.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.usc.cssl.nlputils.utilities.Log;

public class LatinCrawler {
	private StringBuilder readMe = new StringBuilder();
	String outputDir;
	public Map<String, String> authorNames;
	Set<String> skipBooks;
	
	public LatinCrawler() {
		authorNames = new HashMap<String, String>();
		//authorUrl = new HashSet<String>();
		skipBooks = new HashSet<String>();
		
		skipBooks.add("The Latin Library");
		skipBooks.add("The Classics Page");
		skipBooks.add("The Classics Homepage");
		skipBooks.add("Christian Latin");
		skipBooks.add("Thomas May");
		skipBooks.add("Contemporary Latin");
		skipBooks.add("Apollinaris Sidonius");
		skipBooks.add("The Miscellany");
		skipBooks.add("St. Thomas Aquinas");
		skipBooks.add("St. Jerome");
		//skipBooks.add("Leo the Great"); //check this
		skipBooks.add("Isidore of Seville");
		skipBooks.add("Seneca the Younger");
		skipBooks.add("Seneca the Elder");
		skipBooks.add("Miscellanea Carminum");
		skipBooks.add("Velleius");
		skipBooks.add("Neo-Latin");
		skipBooks.add("The Bible");
		skipBooks.add("Medieval Latin");
		skipBooks.add("Christian");
		skipBooks.add("Christina Latin");
		skipBooks.add("Medieval");
		skipBooks.add("Ius Romanum");
		skipBooks.add("Miscellany");
		skipBooks.add("Paulus Diaconus");
	}
	
	public static void main(String[] args) throws Exception{
		LatinCrawler lc = new LatinCrawler();
		lc.outputDir = "/home/niki/Desktop/CSSL/latin/";
		lc.authorNames.put("Suetonius","http://www.thelatinlibrary.com/suet.html");
		//lc.getBooks("Suetonius", "http://www.thelatinlibrary.com/suet.html", "/home/niki/Desktop/CSSL/latin/Suetonius", "Suetonius");
		//lc.getContent("http://www.thelatinlibrary.com/suetonius/suet.aug.shtml", "Liber XX", "C:/Users/carlosg/Desktop/CSSL/Clusering/latin library/Suetonis/");
		lc.initialize("/home/niki/Desktop/CSSL/latin/");
		//lc.getBooks("Suetonius","http://www.thelatinlibrary.com/suet.html", "C:/Users/carlosg/Desktop/CSSL/Clusering/latin library/Suetonis/", "Suetonius");
	}
	
	public void initialize(String outputDir){
		this.outputDir = outputDir;	
	}
	
	public void crawl() throws IOException{
		checkPath(outputDir);
		
		getAllAuthors();
		
		callSpecialAuthors("http://www.thelatinlibrary.com/medieval.html", "Medieval Latin");
		callSpecialAuthors("http://www.thelatinlibrary.com/christian.html", "Christian Latin");
		callSpecialAuthors("http://www.thelatinlibrary.com/neo.html", "Neo-Latin");
		callSpecialAuthors("http://www.thelatinlibrary.com/misc.html", "Miscellany");
		callSpecialAuthors("http://www.thelatinlibrary.com/ius.html", "Ius Romanum");

		//writeReadMe(outputDir);

	}
	
	private void callSpecialAuthors(String url, String name) throws IOException{
		File authorDir = new File(outputDir + File.separator + name);
		if(!authorDir.exists()){
			authorDir.mkdirs();
		}
		getAllSubAuthors(url, outputDir + File.separator + name);
	}
	
	private void checkPath(String outputDir) {
		File outputPath = new File(outputDir);
		if (!outputPath.exists()){
			outputPath.mkdirs();
		}
	}	
	
	public void getAuthorsList(String url) throws Exception{
		int i, size = 0;
		String name ;
		Document doc = Jsoup.connect(url).timeout(10*1000).get();
		Elements authorsList = doc.getElementsByTag("option");
		
		size = authorsList.size();
		int count = 0;
		System.out.println("Here");
		for(i =0;i<size;i++)
		{
				name = authorsList.get(i).text();
				//url = "http://www.thelatinlibrary.com/" + authorsList.get(i).attr("value");
				authorNames.put(name,"http://www.thelatinlibrary.com/" + authorsList.get(i).attr("value") );
				if(skipBooks.contains(name))
					continue;
			 	count++;
		}
		
		Element secondList = doc.getElementsByTag("table").get(1);
		Elements auth2List = secondList.getElementsByTag("td");
		for(Element auth : auth2List)
		{
			name = auth.text();
			//url = auth.getElementsByTag("a").attr("abs:href");
			if(authorNames.containsKey(name))
				continue;
			authorNames.put(name,auth.getElementsByTag("a").attr("abs:href") );
			if(skipBooks.contains(name))
				continue;
		}
	}
	
	public void getAllAuthors() throws IOException{
		List<String> authors = (List<String>) authorNames.keySet();
		
		for(String name:authors){
				getSingleAuthor(name, authorNames.get(name));
		}
		return;
	}
	
	public void getSingleAuthor(String name, String url) throws IOException{
		String aurl = outputDir + File.separator + name;
		File authorDir = new File(aurl);
		if(!authorDir.exists()){
			authorDir.mkdirs();
		}
		appendLog("\nExtracting Books of Author  "+ name +"...");
		getBooks(name, url , aurl);
	}
	
	/* The sub libraries*/
	public void getAllSubAuthors(String connectUrl, String output) throws IOException{
		int i = 0, size = 0;
		String name, url;
		Document doc = Jsoup.connect(connectUrl).timeout(10*1000).get();
	
		Element secondList = doc.getElementsByTag("table").get(0);
		Elements auth2List = secondList.getElementsByTag("td");
		for(Element auth : auth2List)
		{
			name = auth.text();
			url = auth.getElementsByTag("a").attr("abs:href");
			if(authorNames.containsKey(name))
				continue;
			//authorNames.add(name);
			if(skipBooks.contains(name))
				continue;
			String aurl = output + File.separator + name;
			File authorDir = new File(aurl);
			if(!authorDir.exists()){
				authorDir.mkdirs();
			}
			//System.out.println("Extracting Books of Author  "+ name +"...");
			appendLog("\nExtracting Books of Author  "+ name +"...");
		 	getBooks(name, url, aurl);
			i++;
			
		}
		
		return;
	}
	
	/* Get all books of a single author
	 * Recursive function to trace all books and links of a particular author 
	 */
	private void getBooks(String author, String aurl, String apath) throws IOException {

		try{
			Document doc = Jsoup.connect(aurl).timeout(10*1000).get();
			Elements subLists = doc.select("div.work");
			if(subLists != null && subLists.size() > 0){
				getBooksList(author, aurl, doc, apath);
				return;
			}	
			
			Elements booksList = doc.getElementsByTag("td");
			
			int count = 0;
			String bookname = "";
			int i = 0;
			if(booksList != null){
				int size = booksList.size();
				for (i =0;i<size;i++){
					Element bookItem = booksList.get(i);
					String bookText = bookItem.getElementsByTag("a").attr("abs:href");
					if(bookText.contains("#"))
						continue;
					bookname  = bookItem.text();
					if(bookText.isEmpty() || bookText== null)
						continue;
					if(skipBooks.contains(bookname))
						continue;
					if(authorNames.containsKey(bookname))
						continue;
					getBooks(bookname, bookText, apath);
					count++;

				}
			}
			if(count == 0) {
				if(doc.select("title")!=null && doc.select("title").size() > 0)
					bookname = doc.select("title").first().text();
				else if(doc.select("p.pagehead")!= null && doc.select("p.pagehead").size() > 0 )
					bookname = doc.select("p.pagehead").first().text();
				else if(doc.select("h1")!= null && doc.select("h1").size() >  0)
					bookname = doc.select("h1").first().text();
				else
					bookname = author;
				getContent(aurl, bookname, apath);
			}
		}catch(Exception e){
			//System.out.println("Something went wrong when extracting books of Author " + author + e);
			appendLog("Something went wrong when extracting books of Author " + author);
		}
	}

	private void getBooksList(String author, String aurl, Document doc, String apath){
		Elements subLists = doc.select("div.work");
		Elements subHeaders = doc.select("h2.work");
		int i = 0, size1 = subLists.size(), size2 = subHeaders.size(), j =0;
		String bookText ="";
		String bookname = "";
		Element head = null;
		int k;

		//handle count of headers and div
		while(i< size1 || j<size2){
			try{
				if(j< size2){
					head = subHeaders.get(j);
					Elements bookLink = head.getElementsByTag("a");
					if(bookLink!= null && bookLink.size() > 0){
						//make for all links
						bookText = bookLink.get(0).attr("abs:href");
						bookname  = bookLink.get(0).text();
						if(skipBooks.contains(bookname))
							continue;
						if(authorNames.containsKey(bookname)|| (bookname.toLowerCase()).equals(author.toLowerCase()))
							continue;
						File authorDir = new File(apath + File.separator + bookname);
						if(!authorDir.exists()){
							authorDir.mkdirs();
						}
						String apath1 = authorDir.toString();
						getBooks(bookname, bookText, apath1);
						j++;
						continue;
					}
				}

				if(i< size1){
					Element list = subLists.get(i);
					Elements booksList = list.getElementsByTag("td");

					String authorNewDir = apath +  File.separator;
					if(j<size2)
						authorNewDir += head.text();
					else
						authorNewDir += "Others";
					File authorDir = new File(authorNewDir);
					if(!authorDir.exists()){
						authorDir.mkdirs();
					}
					String apath1 = authorDir.toString();

					k = 0;
					int count1 = 0;
					Element bookItem = null;
					if(booksList != null){
						for (k=0;k<booksList.size();k++ ){
							bookItem = booksList.get(k);
							bookText = bookItem.getElementsByTag("a").attr("abs:href");
							
							if(bookText == null)
								continue;
							bookname  = bookItem.text();
							if(skipBooks.contains(bookname))
								continue;
							if(authorNames.containsKey(bookname)|| (bookname.toLowerCase()).equals(author.toLowerCase()))
								continue;
							getBooks(bookname, bookText, apath1);
							count1++;
						}
					}


					if(count1 == 0) {
						if(doc.select("title")!=null)
							bookname = doc.select("title").first().text();
						else if(doc.select("p.pagehead")!= null && doc.select("p.pagehead").size() > 0 )
							bookname = doc.select("p.pagehead").first().text();
						else if(doc.select("h1")!= null && doc.select("h1").size() >  0)
							bookname = doc.select("h1").first().text();
						else
							bookname = author;
						getContent(aurl, bookname, apath1);
					}
					i++;
					
				}
				j++;
			}catch(Exception e){
				//System.out.println("Something went wrong when extracting books of Author " + author);
				appendLog("Something went wrong when extracting books of Author " + author);
			}
		}
	}
	
	private void getContent(String bookUri, String bookDir, String authorDir) throws IOException{
		BufferedWriter csvWriter= null;
		appendLog("Extracting Content of book  "+ bookDir +"...");
		try{
			bookDir = bookDir.replaceAll("[.,;\"!-()\\[\\]{}:?'/\\`~$%#@&*_=+<>*$]", "");
			csvWriter  = new BufferedWriter(new FileWriter(new File(authorDir + System.getProperty("file.separator") + bookDir+".txt")));
			Document doc = Jsoup.connect(bookUri).timeout(10*10000).get();
			Elements content = doc.getElementsByTag("p"); 
			if(content.size() == 0)
			{
				if(csvWriter!=null)
					csvWriter.close();
				getBookContent(bookUri, bookDir, authorDir);
				return;
			}
			for (Element c : content){
				csvWriter.write(c.text()+"\n");
			}
		}catch(Exception e){
			getBookContent(bookUri, bookDir, authorDir);
			//appendLog("Something went wrong when extracting book " + bookDir);
		}finally{
			if(csvWriter!=null)
				csvWriter.close();
		}
	}
	

	public void getBookContent(String bookUri, String bookDir, String authorDir) throws IOException {
		BufferedWriter csvWriter= null;
		try{
			csvWriter  = new BufferedWriter(new FileWriter(new File(authorDir + System.getProperty("file.separator") + bookDir+".txt")));
		   Document doc = Jsoup.parse(new URL(bookUri).openStream(), "UTF-16", bookUri);
		   Elements content = doc.getElementsByTag("p"); 
				for (Element c : content){
					csvWriter.write(c.text()+"\n");
				}
			}catch(Exception e){
				//System.out.println("Something went wrong when extracting book " + bookDir + e);
				appendLog("Something went wrong when extracting book " + bookDir);
			}finally{
				if(csvWriter!=null)
					csvWriter.close();
			}
	}

	
	
	@Inject IEclipseContext context;	
	private void appendLog(String message){
		Log.append(context, message);
	}
	public void writeReadMe(String location){
		File readme = new File(location+"/README.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String plugV = Platform.getBundle("edu.usc.cssl.nlputils.plugins.latincrawler").getHeaders().get("Bundle-Version");
			String appV = Platform.getBundle("edu.usc.cssl.nlputils.application").getHeaders().get("Bundle-Version");
			Date date = new Date();
			bw.write("Latin Crawler Output\n--------------------\n\nApplication Version: "+appV+"\nPlugin Version: "+plugV+"\nDate: "+date.toString()+"\n\n");
			bw.write(readMe.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
