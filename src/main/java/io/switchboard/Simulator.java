package io.switchboard;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Christoph Grotz on 07.12.14.
 */
public class Simulator {

  public static void main(String ... args) throws IOException {
    while( true )
      excutePost("http://localhost:8080/api/v1/switchboard", "{\"test\":\"Hallo Welt\", \"time\":"+System.currentTimeMillis()+"}");
  }

  public static String excutePost(String targetURL, String body)
  {
    URL url;
    HttpURLConnection connection = null;
    try {
      //Create connection
      url = new URL(targetURL);
      connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("POST");


      connection.setUseCaches (false);
      connection.setDoInput(true);
      connection.setDoOutput(true);

      //Send request
      DataOutputStream wr = new DataOutputStream (
              connection.getOutputStream ());
      wr.write(body.getBytes());
      wr.flush ();
      wr.close ();

      //Get Response
      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      String line;
      StringBuffer response = new StringBuffer();
      while((line = rd.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }
      rd.close();
      return response.toString();

    } catch (Exception e) {

      e.printStackTrace();
      return null;

    } finally {

      if(connection != null) {
        connection.disconnect();
      }
    }
  }
}
