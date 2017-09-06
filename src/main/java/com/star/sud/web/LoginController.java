package com.star.sud.web;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

	public static String redirect_url = "/linkedin/result";
	public static String client_id = "";//change accordingly
	public static String client_secret = "";//change accordingly

	final static String JSON_REG_DATA = "JSON_REG_DATA";

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String loginPage(Model model) {

		String restUrl = "http://localhost:8085/linked-in-integration";//change accordingly

		String linkedInUrl = "https://www.linkedin.com/oauth/v2/authorization?response_type=code&client_id=" + client_id
				+ "&redirect_uri=" + restUrl + redirect_url + "&state=fdfdfdfd&scope=r_basicprofile%20r_emailaddress";

		model.addAttribute("linkedInUrl", linkedInUrl);
		return "login/login-page";

	}

	@RequestMapping(value = "/linkedin/result{code}", method = RequestMethod.GET)
	public String getResultFromLinkedIn(Model model, ServletRequest request, HttpServletResponse response1,
			HttpSession session, @PathVariable("code") String code1, RedirectAttributes redirectAttributes) {

		String temp = "";
		String code = request.getParameter("code");
		String url = "https://www.linkedin.com/oauth/v2/accessToken";

		String firtName = "";
		String lastName = "";
		String emailAddress = "";
		String address = "";

		try {
			URL obj = new URL(url);

			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Host", "www.linkedin.com");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			String urlParameters = "grant_type=authorization_code&code=" + code + "&redirect_uri="
					+ "http://localhost:8085/linked-in-integration" + redirect_url + "&client_id=" + client_id
					+ "&client_secret=" + client_secret + "";
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();

			if (responseCode == 200) {
				InputStream inputStream = con.getInputStream();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader in = new BufferedReader(inputStreamReader);

				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				JSONObject jsonObj = new JSONObject(response.toString());
				String access_token = jsonObj.getString("access_token");

				temp = sendGet(access_token);
				JSONObject jsonObjTemp = new JSONObject(temp);

				firtName = jsonObjTemp.getString("firstName");
				lastName = jsonObjTemp.getString("lastName");
				emailAddress = jsonObj.getString("emailAddress");
				address = jsonObj.getJSONObject("location").getString("name");

			}

			else {
				JSONObject jsonObj = (JSONObject) session.getAttribute(JSON_REG_DATA);

				firtName = jsonObj.getString("firstName");
				lastName = jsonObj.getString("lastName");
				emailAddress = jsonObj.getString("emailAddress");
				address = jsonObj.getJSONObject("location").getString("name");

			}
		} catch (Exception e) {
			System.out.println("sdfsgd");
		}
		model.addAttribute("firtName", firtName);
		model.addAttribute("lastName", lastName);
		model.addAttribute("emailAddress", emailAddress);
		model.addAttribute("location", address);

		return "welcome/welcome-page";
	}

	private static String sendGet(String access_token) throws Exception {
		String url = "https://api.linkedin.com/v1/people/~:(id,firstName,lastName,headline,industry,num-connections,specialties,positions,location,emailAddress)?format=json";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Host", "api.linkedin.com");
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Authorization", "Bearer " + access_token);
		con.getResponseCode();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

}
