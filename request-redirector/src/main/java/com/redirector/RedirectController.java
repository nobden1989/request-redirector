package com.redirector;

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
import java.util.UUID;
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
public class RedirectController {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@PostMapping("/redirect")
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
		UUID idGen = UUID.randomUUID();
		String uuid = idGen.toString();
		System.out.println("=========================Mock Request start=========================");
		System.out.println(time + "Request Send from:" + remoteAddr);
		System.out.println(time + "Request Send to:" + httpsUrl);
		System.out.println(time + "Request Tracking Header: [AQA-TRACKER:" + uuid + "]");
		URL url;
		try {

			url = new URL(httpsUrl);

			if ("HTTPS".equalsIgnoreCase(url.getProtocol())) {
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				return print_content(con, response, uuid);
			} else if ("HTTP".equalsIgnoreCase(url.getProtocol())) {
				HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
				return sendGet(httpConn, response, uuid);
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

	private String print_content(HttpsURLConnection con, HttpServletResponse response, String uuid) {
		if (con != null) {
			try {

				con.setRequestProperty("AQA-TRACKER", uuid);
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
	private String sendGet(HttpURLConnection con, HttpServletResponse response, String uuid) throws Exception {

		// optional default is GET
		con.setRequestMethod("GET");
		con.setRequestProperty("AQA-TRACKER", uuid);
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
