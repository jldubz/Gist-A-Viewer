package com.jldubz.gistaviewer.model;

import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import okhttp3.Headers;
import retrofit2.Response;

public class NetworkUtil {

    /**
     * Check the HTTP code returned from a Retrofit Call to display the right error message to the user
     *
     * @param response the Retrofit Response to process
     * @see Response
     */
    public static String onGitHubResponseError(Response response) {

        if (response.code() == 403) {
            //HTTP 403 forbidden
            // Rate limit exceeded
            Headers headers = response.headers();
            Set<String> headerNames = headers.names();
            int rateLimit = 0;
            long rateLimitReset = 0;
            //int rateLimitRemaining = 0;
            for (String headerName : headerNames) {
                String headerValue = headers.get(headerName);
                if (headerValue == null) {
                    continue;
                }
                switch (headerName) {
                    case "X-RateLimit-Limit":
                        rateLimit = Integer.valueOf(headerValue);
                        break;
                    case "X-RateLimit-Reset":
                        rateLimitReset = Long.valueOf(headerValue);
                        break;
                }
            }

            if (rateLimit != 0) {
                Date resetDate = new Date(rateLimitReset * 1000);
                String resetTime = DateFormat.getTimeInstance().format(resetDate);
                return "Uh Oh! \nIt looks like you exceeded your API rate limit.\nTry again after " + resetTime;
            }
        }

        return response.message();
    }

}
