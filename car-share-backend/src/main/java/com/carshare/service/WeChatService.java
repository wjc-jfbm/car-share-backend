package com.carshare.service;

import com.carshare.config.WeChatConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class WeChatService {

    @Autowired
    private WeChatConfig weChatConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile String accessToken;
    private volatile long accessTokenExpireTime = 0;
    private final ReentrantLock tokenLock = new ReentrantLock();

    private static final long TOKEN_AHEAD_EXPIRE = 200;

    @PostConstruct
    public void init() {
        log.info("WeChatService initialized, appid={}", weChatConfig.getAppid());
    }

    public String getAccessToken() {
        if (isAccessTokenValid()) {
            return accessToken;
        }
        tokenLock.lock();
        try {
            if (isAccessTokenValid()) {
                return accessToken;
            }
            refreshAccessToken();
            return accessToken;
        } finally {
            tokenLock.unlock();
        }
    }

    private boolean isAccessTokenValid() {
        return accessToken != null
                && System.currentTimeMillis() < (accessTokenExpireTime - TOKEN_AHEAD_EXPIRE);
    }

    private void refreshAccessToken() {
        String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                weChatConfig.getAccessTokenUrl(),
                weChatConfig.getAppid(),
                weChatConfig.getSecret());
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(response);
            if (node.has("access_token")) {
                accessToken = node.get("access_token").asText();
                int expiresIn = node.get("expires_in").asInt();
                accessTokenExpireTime = System.currentTimeMillis() + expiresIn * 1000L;
                log.info("access_token refreshed, expires_in={}s", expiresIn);
            } else {
                int errcode = node.has("errcode") ? node.get("errcode").asInt() : -1;
                String errmsg = node.has("errmsg") ? node.get("errmsg").asText() : "unknown";
                log.error("refresh access_token failed: errcode={}, errmsg={}", errcode, errmsg);
            }
        } catch (Exception e) {
            log.error("refresh access_token exception: {}", e.getMessage());
        }
    }

    public JsCode2SessionResult jsCode2Session(String code) {
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                weChatConfig.getAuthUrl(),
                weChatConfig.getAppid(),
                weChatConfig.getSecret(),
                code);
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(response);
            JsCode2SessionResult result = new JsCode2SessionResult();
            result.setOpenid(node.has("openid") ? node.get("openid").asText() : null);
            result.setSessionKey(node.has("session_key") ? node.get("session_key").asText() : null);
            result.setUnionid(node.has("unionid") ? node.get("unionid").asText() : null);
            if (node.has("errcode")) {
                result.setErrcode(node.get("errcode").asInt());
                result.setErrmsg(node.has("errmsg") ? node.get("errmsg").asText() : null);
            }
            if (result.getErrcode() != null && result.getErrcode() != 0) {
                log.error("jscode2session failed: errcode={}, errmsg={}", result.getErrcode(), result.getErrmsg());
            }
            return result;
        } catch (Exception e) {
            log.error("jscode2session exception: {}", e.getMessage());
            JsCode2SessionResult result = new JsCode2SessionResult();
            result.setErrcode(-1);
            result.setErrmsg(e.getMessage());
            return result;
        }
    }

    @lombok.Data
    public static class JsCode2SessionResult {
        private String openid;
        private String sessionKey;
        private String unionid;
        private Integer errcode;
        private String errmsg;

        public boolean isSuccess() {
            return errcode == null || errcode == 0;
        }
    }

    public String decryptPhoneNumber(String sessionKey, String encryptedData, String iv) {
        try {
            byte[] sessionKeyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] ivBytes = Base64.getDecoder().decode(iv);

            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec keySpec = new SecretKeySpec(sessionKeyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(encryptedBytes);
            String jsonStr = new String(decrypted, StandardCharsets.UTF_8);

            JsonNode node = objectMapper.readTree(jsonStr);
            if (node.has("phoneNumber")) {
                return node.get("phoneNumber").asText();
            }
            if (node.has("purePhoneNumber")) {
                return node.get("purePhoneNumber").asText();
            }
            return null;
        } catch (Exception e) {
            log.error("decrypt phone number failed: {}", e.getMessage());
            return null;
        }
    }

    public String getPhoneNumberFromCode(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s&code=%s",
                getAccessToken(), code);
        try {
            String response = restTemplate.postForObject(url, null, String.class);
            JsonNode node = objectMapper.readTree(response);
            if (node.has("errcode") && node.get("errcode").asInt() == 0) {
                JsonNode phoneInfo = node.get("phone_info");
                if (phoneInfo != null && phoneInfo.has("phoneNumber")) {
                    return phoneInfo.get("phoneNumber").asText();
                }
            }
            log.error("get phone number from code failed: {}", response);
            return null;
        } catch (Exception e) {
            log.error("get phone number from code exception: {}", e.getMessage());
            return null;
        }
    }
}
