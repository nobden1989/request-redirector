package hello;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	@PostMapping("/mock")
	public String mockUrl(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {

		String httpsUrl = body.replace("{", "").replace("}", "");

		String remoteAddr = "";

		if (request != null) {
			remoteAddr = request.getHeader("X-FORWARDED-FOR");
			if (remoteAddr == null || "".equals(remoteAddr)) {
				remoteAddr = request.getRemoteAddr();
			}
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String time = "[" + dateFormat.format(new Date()) + "]";

		System.out.println("=========================Mock Request start=========================");
		System.out.println(time + "Request Send from:" + remoteAddr);
		System.out.println(time + "Request Send to:" + httpsUrl);
		URL url;
		try {

			url = new URL(httpsUrl);

			if ("HTTPS".equalsIgnoreCase(url.getProtocol())) {
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				return print_content(con, response);
			} else if ("HTTP".equalsIgnoreCase(url.getProtocol())) {
				HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
				return sendGet(httpConn, response);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Something Wrong";
	}

	private String print_content(HttpsURLConnection con, HttpServletResponse response) {
		if (con != null) {
			try {

				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

				String message = IOUtils.toString(br);
				br.close();
				response.setStatus(con.getResponseCode());
				return message;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

	// HTTP GET request
	private String sendGet(HttpURLConnection con, HttpServletResponse response) throws Exception {

		// optional default is GET
		con.setRequestMethod("GET");
		InputStream _is;
		response.setStatus(con.getResponseCode());
		if (con.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
		    _is = con.getInputStream();
		} else {
		     /* error from server */
		    _is = con.getErrorStream();
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(_is));
		String inputLine;
		StringBuffer resBody = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			resBody.append(inputLine);
		}
		in.close();

		// print result
		return resBody.toString();

	}
}
