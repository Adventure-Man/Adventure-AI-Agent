package com.adventure.adventureaiagent.utils;

import com.adventure.adventureaiagent.tools.SearchApiTools;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Adventure
 * @date 2026/4/25
 * @description TODO
 */
@SpringBootTest
public class SearchApiTest {

    @Value("${search.api.key}")
    String apiKey;

    @Test
    public void testSearchApi() throws IOException {
        SearchApiTools searchApiTools = new SearchApiTools(apiKey);
        String result = searchApiTools.getSearchResult("动物");
        System.out.println(result);
    }

}
