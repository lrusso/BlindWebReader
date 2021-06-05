package ar.com.lrusso.blindwebreader;

import android.os.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class WebReaderThreadGoTo extends AsyncTask<String, String, Boolean>
	{
	String webContent;

	public WebReaderThreadGoTo(String a)
		{
		webContent = a;
		}

	@Override protected void onPreExecute()
		{
		super.onPreExecute();
		}

	@Override protected Boolean doInBackground(String... url)
		{
		return readWebsite(webContent);
		}
		
	@Override protected void onPostExecute(Boolean pageloaded)
		{
		GlobalVars.browserPleaseWaitDialog.dismiss();
		GlobalVars.browserRequestInProgress=false;

		if (pageloaded==true)
			{
			try{WebReaderPageViewer.linkLocation=-1;}catch(Exception e){}
			GlobalVars.startActivity(WebReaderPageViewer.class);
			}
			else
			{
			GlobalVars.talk(GlobalVars.context.getResources().getString(R.string.layoutBrowserPageNotFound));
			}
		}

	private boolean readWebsite(String htmlContent)
		{
		try
			{
			htmlContent = unescapeJavaString(htmlContent);

			Document doc = Jsoup.parse(htmlContent);

			//GETS EVERY PAGE LINK
			GlobalVars.browserWebLinks.clear();
			Elements links = doc.select("a[href]");
			for (Element link : links)
				{
				if (!link.attr("abs:href").startsWith("javascript:"))
					{
					if (link.attr("abs:href")!=null &&
						link.attr("abs:href")!="" &&
					    link.attr("abs:href").replaceAll(" ","")!="" &&
						link.attr("abs:href").length()>0 &&
						link.text()!=null &&
						link.text()!="" &&
						link.text().replaceAll(" ","")!="" &&
						link.text().length()>0 &&

						//DELETES GOOGLE CACHE LINKS TO ALLOW THE USER TO PERFORM A BETTER AND CLEANER SEARCHING
						!link.attr("abs:href").toLowerCase().startsWith("http://webcache.googleusercontent.com/") &&
						!link.attr("abs:href").toLowerCase().startsWith("https://webcache.googleusercontent.com/") &&

						//DELETES GOOGLE SIMILAR LINKS TO ALLOW THE USER TO PERFORM A BETTER AND CLEANER SEARCHING
						!(link.attr("abs:href").toLowerCase().startsWith("http://www.google.com/custom?") && link.attr("abs:href").toLowerCase().contains("q=related:")) &&
						!(link.attr("abs:href").toLowerCase().startsWith("https://www.google.com/custom?") && link.attr("abs:href").toLowerCase().contains("q=related:"))
						)
						{
						if (GlobalVars.isValidURL(link.attr("abs:href"))==true)
							{
							addLinkToList(link.text() + "|" + link.attr("abs:href"));
							}
						}
					}
				if (link.attr("href")!=null)
					{
					if (!link.attr("href").startsWith("javascript:"))
						{
						if (link.attr("href")!="" &&
							link.attr("href").replaceAll(" ","")!="" &&
							link.attr("href").length()>0 &&
							link.text()!=null &&
							link.text()!="" &&
							link.text().replaceAll(" ","")!="" &&
							link.text().length()>0)
							{
							addLinkToList(link.text() + "|" + getURLForLinks(link.attr("href")) + link.attr("href"));
							}
						}
					}
				}

			// REMOVING THE HEADER TAG
			Elements header = doc.select("header");
			for (Element headerTag : header)
				{
				headerTag.remove();
				}
			doc = Jsoup.parse(doc.html());

			// REMOVING THE NAV TAG
			Elements nav = doc.select("nav");
			for (Element navTag : nav)
				{
				navTag.remove();
				}
			doc = Jsoup.parse(doc.html());

			// REMOVING THE NAVBAR DIVS
			Elements divList = doc.select("div");
			for (Element selectedDiv : divList)
				{
				if (selectedDiv.className().contains("navbar"))
					{
					selectedDiv.remove();
					}
				}
			doc = Jsoup.parse(doc.html());

			// REMOVING THE FOOTER TAG
			Elements footer = doc.select("footer");
			for (Element footerTag : footer)
				{
				footerTag.remove();
				}
			doc = Jsoup.parse(doc.html());

			GlobalVars.browserWebTitle = doc.title();
			GlobalVars.browserWebText = doc.text();
			GlobalVars.browserWebURL = GlobalVars.browserGoTo;

			return true;
			}
			catch(Exception e)
			{
			}

		return false;
		}

	private String getURLForLinks(String link)
		{
		if (link.startsWith("https://") || link.startsWith("http://"))
			{
			return "";
			}

		if (GlobalVars.browserGoTo.endsWith("/"))
			{
			return  GlobalVars.browserGoTo.substring(0,GlobalVars.browserGoTo.length()-1);
			}
		else
			{
			return GlobalVars.browserGoTo;
			}
		}

	private void addLinkToList(String linkToAdd)
		{
		boolean foundRepeated = false;

		for (int i=0;i<GlobalVars.browserWebLinks.size();i++)
			{
			if (GlobalVars.browserWebLinks.get(i).equals(linkToAdd))
				{
				foundRepeated = true;
				}
			}

		if (foundRepeated==false)
			{
			GlobalVars.browserWebLinks.add(linkToAdd);
			}
		}

		/**
		 * Unescapes a string that contains standard Java escape sequences.
		 * <ul>
		 * <li><strong>&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'</strong> :
		 * BS, FF, NL, CR, TAB, double and single quote.</li>
		 * <li><strong>&#92;X &#92;XX &#92;XXX</strong> : Octal character
		 * specification (0 - 377, 0x00 - 0xFF).</li>
		 * <li><strong>&#92;uXXXX</strong> : Hexadecimal based Unicode character.</li>
		 * </ul>
		 *
		 * @param st
		 *            A string optionally containing standard java escape sequences.
		 * @return The translated string.
		 */
		public String unescapeJavaString(String st) {

			StringBuilder sb = new StringBuilder(st.length());

			for (int i = 0; i < st.length(); i++) {
				char ch = st.charAt(i);
				if (ch == '\\') {
					char nextChar = (i == st.length() - 1) ? '\\' : st
							.charAt(i + 1);
					// Octal escape?
					if (nextChar >= '0' && nextChar <= '7') {
						String code = "" + nextChar;
						i++;
						if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
								&& st.charAt(i + 1) <= '7') {
							code += st.charAt(i + 1);
							i++;
							if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
									&& st.charAt(i + 1) <= '7') {
								code += st.charAt(i + 1);
								i++;
							}
						}
						sb.append((char) Integer.parseInt(code, 8));
						continue;
					}
					switch (nextChar) {
						case '\\':
							ch = '\\';
							break;
						case 'b':
							ch = '\b';
							break;
						case 'f':
							ch = '\f';
							break;
						case 'n':
							ch = '\n';
							break;
						case 'r':
							ch = '\r';
							break;
						case 't':
							ch = '\t';
							break;
						case '\"':
							ch = '\"';
							break;
						case '\'':
							ch = '\'';
							break;
						// Hex Unicode: u????
						case 'u':
							if (i >= st.length() - 5) {
								ch = 'u';
								break;
							}
							int code = Integer.parseInt(
									"" + st.charAt(i + 2) + st.charAt(i + 3)
											+ st.charAt(i + 4) + st.charAt(i + 5), 16);
							sb.append(Character.toChars(code));
							i += 5;
							continue;
					}
					i++;
				}
				sb.append(ch);
			}
			return sb.toString();
		}
	}