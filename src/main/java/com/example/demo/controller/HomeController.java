package com.example.demo.controller;

import com.example.demo.dto.BoardDTO;
import com.example.demo.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.demo.service.BoardService;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final BoardRepository boardRepository;
    /*
    @GetMapping("/")
    public String home() {
        return "index";
    }
    @GetMapping("hello")
    public String hello(Model model) {
        model.addAttribute("data", "hello@!@!@");
        return "hello";
    }*/
    @Value("${TOSS_CKEY}") // application.yml이나 환경변수에 정의된 값
    private String apiKey;
    @GetMapping("/")
    public String index(Model model) {
        // MyBatis 레포지토리의 findLatest 메서드 호출
        List<BoardDTO> latestPosts = boardRepository.findLatest();

        model.addAttribute("latestPosts", latestPosts);
        model.addAttribute("apiKey", apiKey);
        return "index";
    }
    @GetMapping("/terms")
    public String termsPage() {
        return "terms";
    }
    @GetMapping("/privacy")
    public String privacyPage() {
        return "privacy";
    }
}