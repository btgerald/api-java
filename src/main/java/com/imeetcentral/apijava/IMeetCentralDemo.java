package com.imeetcentral.apijava;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.sf.json.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.Base64.Decoder;


/**
 * Based on code by Douglas Gischlar <d.gischlar@ieee.org> with permission.
 */

@SuppressWarnings("Since15")
public class IMeetCentralDemo {
    private static Map getConfig() throws IOException {
        Yaml yaml = new Yaml();
        Map clientConfig = (Map)yaml.load(new FileInputStream(new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + "client_config.yml")));
        Map config = (Map)yaml.load(new FileInputStream(new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + "config.yml")));
        ((Map)config.get("parameters")).putAll((Map)clientConfig.get("parameters"));

        return (Map)config.get("parameters");
    }

    public static void main(String[] args) {
        String[] endpoints = args;

        try {
            Map config = getConfig();

            String clientPrivateKey = (String)config.get("client.key");
            String clientId = (String)config.get("client.id");
            List<String> audience = Collections.singletonList((String)config.get("auth.cd.issuer"));
            String grant_type = (String)config.get("auth.cd.grant_type");
            String auth_url = (String)config.get("auth.cd.auth_url");

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Decoder decoder = Base64.getDecoder();

            byte[] servicePrivateKeyBytes = decoder.decode(clientPrivateKey);
            PrivateKey serviceRsaPrivateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(servicePrivateKeyBytes));
            RSASSASigner serviceSigner = new RSASSASigner((RSAPrivateKey) serviceRsaPrivateKey);

            DateTime now = new DateTime().minusSeconds(10);
            DateTime expiresIn = new DateTime().plusSeconds(60);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .issuer(clientId)
                    .audience(audience)
                    .issueTime(now.toDate())
                    .expirationTime(expiresIn.toDate())
                    .claim("scp", (String)config.get("auth.cd.scp"))
                    .build();


            JWSHeader header = new JWSHeader(JWSAlgorithm.RS256, new JOSEObjectType("JWT"), null, null, null, null, null, null, null, null, UUID.randomUUID().toString(), null, null);

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            signedJWT.sign(serviceSigner);

            JSONObject jsonAssert = new JSONObject();
            jsonAssert.put("grant_type", grant_type);
            jsonAssert.put("assertion", signedJWT.serialize());

            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(auth_url);
            StringEntity params = new StringEntity(jsonAssert.toString());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(params);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());

            JSONObject jsonResp = JSONObject.fromObject(responseBody);

            System.out.println("Access Token: " + jsonResp.toString(5));

            String accessToken = (String)jsonResp.get("access_token");

            ApiCaller apiCaller = new ApiCaller((String)config.get("edge.base_url"), accessToken);

            for (String endpoint: endpoints) {
                apiCaller.callEndpoint(endpoint);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}