package com.adventure.adventureaiagent.utils;

import com.adventure.adventureaiagent.tools.SearchApiTools;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

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
    public void testSearchApi() {
        SearchApiTools searchApiTools = new SearchApiTools(apiKey);
        String result = searchApiTools.getSearchResult("动物");
        System.out.println(result);

    }

}
