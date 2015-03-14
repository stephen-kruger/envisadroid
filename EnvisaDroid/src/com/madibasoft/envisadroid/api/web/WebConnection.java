package com.madibasoft.envisadroid.api.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;

import com.madibasoft.envisadroid.api.EnvisaException;


public class WebConnection {

	public String execute(String server, String user, String password, String params) throws EnvisaException {
		CredentialsProvider credProvider = new BasicCredentialsProvider();
		credProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
				new UsernamePasswordCredentials(user, password));
		//
		DefaultHttpClient http = new DefaultHttpClient();
		http.setCredentialsProvider(credProvider);
		//
		HttpGet get = new HttpGet("http://"+server+params);
		try {
			HttpResponse response = http.execute(get);
			if (response.getStatusLine().getStatusCode()!=200) {
				throw new EnvisaException("Server returned error ("+response.getStatusLine().getReasonPhrase()+")");
			}
			return getStringFromInputStream(response.getEntity().getContent());
		}
		catch (Throwable t) {
			throw new EnvisaException(t.getMessage());
		}
	}
	
	public String getHomePage(String server, String user, String password) throws EnvisaException {
		return execute(server,user,password,"");
	}
	
	private static String getStringFromInputStream(InputStream is) {
		 
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
 
		String line;
		try {
 
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
 
		return sb.toString();
 
	}
	
	public String getHomePageX(String server, String user, String password) {
//		return "<HTML><TITLE>Envisalink 3</TITLE><link rel=\"stylesheet\" href=\"css.css\" type=\"text/css\"><meta http-equiv=\"refresh\" content=\"60;url=2\"><BODY><TABLE WIDTH=100% CELLSPACING=0px><TR><TD CLASS=HDL><img src=\"http://www.envisacor.com/images/EnvisALERTS195.png\"></TD><TD CLASS=HDR><H1>EnvisaLink 3</H1></TD></TR></TABLE><TABLE WIDTH=100% CELLSPACING=0px><TR><TH ID=NL>2013-07-18 03:40 - System Time</TH><TH ID=NR><A HREF=2>Home</A>| <A HREF=3>Network</A> </TH></TR></TABLE><DIV ID=CONTENT><br><TABLE WIDTH=100% CELLSPACING=0px><TR><TH COLSPAN=2>Security Subsystem - DSC</TH></TR><TR><TD><H2>Zone Status</H2><TABLE BORDER=2 CLASS=keypad><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">1</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">2</SPAN></TD><TD BGCOLOR=#300000><SPAN TITLE=\"CLOSED: 2 Hours Ago\">3</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">4</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">5</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 89 Hours Ago\">6</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">7</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 89 Hours Ago\">8</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">9</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">10</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">11</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">12</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">13</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">14</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">15</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">16</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">17</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">18</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 37 Hours Ago\">19</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 11 Hours Ago\">20</SPAN></TD><TD BGCOLOR=#270000><SPAN TITLE=\"CLOSED: 2 Hours Ago\">21</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 69 Hours Ago\">22</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">23</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">24</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">25</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 10 Hours Ago\">26</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 13 Hours Ago\">27</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">28</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 36 Hours Ago\">29</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">30</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">31</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 11 Hours Ago\">32</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">33</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">34</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">35</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">36</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">37</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">38</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">39</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">40</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">41</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">42</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">43</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">44</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">45</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">46</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">47</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">48</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">49</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">50</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">51</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">52</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">53</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">54</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">55</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">56</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">57</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">58</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">59</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">60</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">61</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">62</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">63</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">64</SPAN></TD></TR></TABLE></TD><TD VALIGN=TOP><H2>System Status</H2><TABLE BORDER=1> <TR><TD>System</TD><TD BGCOLOR=\"LIME\">Ready </TD><TD > </TD><TD WIDTH=200px><P><FORM ACTION=2 METHOD=GET><INPUT TYPE=HIDDEN NAME=A VALUE=3>					<INPUT TYPE=SUBMIT VALUE=ARM><input type=hidden name=p value=1><SPAN CLASS=LITTLE>USER CODE</SPAN><INPUT TYPE=PASSWORD MAXLENGTH=6 SIZE=6 NAME=X></FORM></P></TD><TD><P><FORM ACTION=\"2\" METHOD=\"GET\"><INPUT TYPE=\"hidden\" NAME=\"A\" VALUE=\"1\"><SELECT NAME=\"P\"><OPTION VALUE=\"1\">PGM 1<OPTION VALUE=\"2\">PGM 2<OPTION VALUE=\"3\">PGM 3<OPTION VALUE=\"4\">PGM 4</select><input type=submit value=\"Toggle PGM\"><input type=hidden name=p value=1></FORM></P></TD></TR>   </TABLE></TD></TR><TR><TD CLASS=SP COLSPAN=2></TD></TR><TR><TH COLSPAN=2>Environmental Subsystem - If Equipped</TH></TR><TR><TD COLSPAN=2></TD></TR><TR><TD CLASS=SP COLSPAN=2></TD></TR><TR><TH COLSPAN=2>Expansion Modules</TH></TR><TR><TD COLSPAN=2>None Installed<BR></TD></TR></TABLE><p><p><span class=black><a href=\"2\"> Refresh Page</a></span></DIV></BODY></HTML>";
		return "<HTML><TITLE>Envisalink 3</TITLE><link rel=\"stylesheet\" href=\"css.css\" type=\"text/css\"><meta http-equiv=\"refresh\" content=\"60;url=2\"><BODY><TABLE WIDTH=100% CELLSPACING=0px><TR><TD CLASS=HDL><img src=\"http://www.envisacor.com/images/EnvisALERTS195.png\"></TD><TD CLASS=HDR><H1>EnvisaLink 3</H1></TD></TR></TABLE><TABLE WIDTH=100% CELLSPACING=0px><TR><TH ID=NL>2013-07-19 01:49 - System Time</TH><TH ID=NR><A HREF=2>Home</A>| <A HREF=3>Network</A> </TH></TR></TABLE><DIV ID=CONTENT><br><TABLE WIDTH=100% CELLSPACING=0px><TR><TH COLSPAN=2>Security Subsystem - DSC</TH></TR><TR><TD><H2>Zone Status</H2><TABLE BORDER=2 CLASS=keypad><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">1</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">2</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 8 Hours Ago\">3</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">4</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">5</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">6</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">7</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">8</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">9</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">10</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">11</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">12</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">13</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">14</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">15</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">16</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">17</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">18</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 59 Hours Ago\">19</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 8 Hours Ago\">20</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 9 Hours Ago\">21</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">22</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">23</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">24</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">25</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 8 Hours Ago\">26</SPAN></TD><TD  CLASS=open><SPAN TITLE=\"OPEN\"\">27</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">28</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: 8 Hours Ago\">29</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">30</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"OPEN: 14 Minutes Ago\">31</SPAN></TD><TD BGCOLOR=#FF0000><SPAN TITLE=\"CLOSED: 10 Seconds Ago\">32</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">33</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">34</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">35</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">36</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">37</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">38</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">39</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">40</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">41</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">42</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">43</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">44</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">45</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">46</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">47</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">48</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">49</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">50</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">51</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">52</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">53</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">54</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">55</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">56</SPAN></TD></TR><TR><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">57</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">58</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">59</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">60</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">61</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">62</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">63</SPAN></TD><TD BGCOLOR=#000000><SPAN TITLE=\"CLOSED: \">64</SPAN></TD></TR></TABLE></TD><TD VALIGN=TOP><H2>System Status</H2><TABLE BORDER=1> <TR><TD>System</TD><TD BGCOLOR=\"LIME\">Ready </TD><TD > </TD><TD WIDTH=200px><P><FORM ACTION=2 METHOD=GET><INPUT TYPE=HIDDEN NAME=A VALUE=3>					<INPUT TYPE=SUBMIT VALUE=ARM><input type=hidden name=p value=1><SPAN CLASS=LITTLE>USER CODE</SPAN><INPUT TYPE=PASSWORD MAXLENGTH=6 SIZE=6 NAME=X></FORM></P></TD><TD><P><FORM ACTION=\"2\" METHOD=\"GET\"><INPUT TYPE=\"hidden\" NAME=\"A\" VALUE=\"1\"><SELECT NAME=\"P\"><OPTION VALUE=\"1\">PGM 1<OPTION VALUE=\"2\">PGM 2<OPTION VALUE=\"3\">PGM 3<OPTION VALUE=\"4\">PGM 4</select><input type=submit value=\"Toggle PGM\"><input type=hidden name=p value=1></FORM></P></TD></TR>   </TABLE></TD></TR><TR><TD CLASS=SP COLSPAN=2></TD></TR><TR><TH COLSPAN=2>Environmental Subsystem - If Equipped</TH></TR><TR><TD COLSPAN=2></TD></TR><TR><TD CLASS=SP COLSPAN=2></TD></TR><TR><TH COLSPAN=2>Expansion Modules</TH></TR><TR><TD COLSPAN=2>None Installed<BR></TD></TR></TABLE><p><p><span class=black><a href=\"2\"> Refresh Page</a></span></DIV></BODY></HTML>";
	}

	public void close() {
		
	}


}
