package com.redirector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.management.RuntimeErrorException;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {

	private static final int KILL_TRIGGERED = 1;
	private static final int KILL_DISABLED = 0;

	private static int killTrigger = KILL_DISABLED;

	@PostMapping("/redirect")
	public String mockUrl(@RequestBody String body, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		if (killTrigger == KILL_TRIGGERED) {
			response.setStatus(500);
			return "Kill Triggered, process terminated!";
		}

		String result = URLDecoder.decode(body, "UTF-8");

		Map<String, Object> reqBody = JsonUtil.fromJson(result);

		String httpsUrl = String.valueOf(reqBody.get("url"));
		String method = String.valueOf(reqBody.get("method"));
		LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) reqBody.get("body");

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

				return print_content(con, method, response, uuid, JsonUtil.toJson(data));
			} else if ("HTTP".equalsIgnoreCase(url.getProtocol())) {
				HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
				return sendPost(httpConn, method, response, uuid, JsonUtil.toJson(data));
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

	private String print_content(HttpsURLConnection con, String method, HttpServletResponse response, String uuid,
			String body) {
		if (con != null) {
			try {
				con.setRequestMethod(method);
				con.setDoOutput(true);
				con.setRequestProperty("AQA-TRACKER", uuid);
				if (null != body) {
					OutputStream os = con.getOutputStream();
					os.write(body.getBytes());
					os.close();
				}
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
	private String sendPost(HttpURLConnection con, String method, HttpServletResponse response, String uuid,
			String body) throws Exception {

		// optional default is POST
		con.setRequestMethod(method);
		con.setRequestProperty("AQA-TRACKER", uuid);
		con.setDoOutput(true);
		if (null != body) {
			OutputStream os = con.getOutputStream();
			os.write(body.getBytes());
			os.close();
		}
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

	@GetMapping("/toggleKill")
	public String mockUrl(HttpServletRequest request) {
		return RedirectController.toggleKill();
	}

	public static String toggleKill() {
		RedirectController.killTrigger = RedirectController.killTrigger == KILL_DISABLED ? KILL_TRIGGERED
				: KILL_DISABLED;
		return RedirectController.killTrigger == KILL_DISABLED ? "Kill disabled" : "Kill triggered";
	}
}
