package com.omp.hub.callback.infrastructure.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Configuration
public class OkHttpClientConfig {
    @Value("${okhttp.connect-timeout:10s}")
    private String connectTimeout;

    @Value("${okhttp.read-timeout:30s}")
    private String readTimeout;

    @Value("${okhttp.write-timeout:20s}")
    private String writeTimeout;

    @Value("${okhttp.trust-all:false}")
    private boolean trustAll;

    private long parseDuration(String duration) {
        // Suporta "5s", "1000ms", "2m"
        if (duration.endsWith("ms")) {
            return Long.parseLong(duration.replace("ms", ""));
        } else if (duration.endsWith("s")) {
            return TimeUnit.SECONDS.toMillis(Long.parseLong(duration.replace("s", "")));
        } else if (duration.endsWith("m")) {
            return TimeUnit.MINUTES.toMillis(Long.parseLong(duration.replace("m", "")));
        } else {
            // Default: milissegundos
            return Long.parseLong(duration);
        }
    }

    @Bean
    public OkHttpClient okHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(parseDuration(connectTimeout), TimeUnit.MILLISECONDS)
                .readTimeout(parseDuration(readTimeout), TimeUnit.MILLISECONDS)
                .writeTimeout(parseDuration(writeTimeout), TimeUnit.MILLISECONDS);

        if (trustAll) {
            try {
                // TrustManager que aceita todos os certificados
                final TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            }

                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[] {};
                            }
                        }
                };
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao configurar TrustManager para OkHttpClient", e);
            }
        }

        return builder.build();
    }
}
