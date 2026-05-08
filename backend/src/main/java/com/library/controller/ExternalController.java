package com.library.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("api/external")
public class ExternalController {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String url = "https://holy-bible-api.com/bibles";

    @GetMapping("/bibles")
    public Object[] getBibles() {
            return restTemplate.getForObject(url, Object[].class);
    }
}

