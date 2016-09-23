package com.imeetcentral.apijava;

import net.sf.json.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class ApiCaller {
    String baseUrl;
    String accessToken;

    public ApiCaller(String baseUrl, String accessToken) {
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
    }

    public void callEndpoint(String endpoint) throws IOException {
        System.out.println(endpoint);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(baseUrl + endpoint);
        httpGet.addHeader("Authorization", "Bearer " + accessToken);

        CloseableHttpResponse response = httpClient.execute(httpGet);
        String responseBody = EntityUtils.toString(response.getEntity());

        JSONObject jsonResp = JSONObject.fromObject(responseBody);

        System.out.println("API Response: " + jsonResp.toString(5));
    }
}
