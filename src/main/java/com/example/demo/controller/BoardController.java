package com.example.demo.controller;

import com.example.demo.Security.PrincipalDetails;
import com.example.demo.dto.BoardDTO;
import com.example.demo.dto.BoardFileDTO;
import com.example.demo.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/save")
    public String save(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        // 1. 로그인 상태 확인
        if (principalDetails != null) {
            // 2. 현재 로그인한 사용자의 이름(또는 이메일) 가져오기
            String loginName = principalDetails.getMember().getName();
            model.addAttribute("loginName", loginName);
        }
        return "save";
    }

    @PostMapping("/save")
    public String save(BoardDTO boardDTO, Principal principal) throws IOException {
        System.out.println("boardDTO = " + boardDTO);
        //boardService.save(boardDTO);
        boardService.save(boardDTO, principal.getName());
        return "redirect:/board/" + boardDTO.getId();
        //return "redirect:/list";
    }

    @GetMapping("/list")
    public String findAll(Model model) {
        List<BoardDTO> boardDTOList = boardService.findAll();
        model.addAttribute("boardList", boardDTOList);
        System.out.println("boardDTOList = " + boardDTOList);
        return "list";
    }

    // /10, /1
    @Value("${CLOUD_PUBLIC_URL}")
    private String CLOUD_PUBLIC_URL;
    @GetMapping("/board/{id}")
    public String findById(@PathVariable("id") Long id, Model model) {
        // 조회수 처리
        boardService.updateHits(id);
        // 상세내용 가져옴
        BoardDTO boardDTO = boardService.findById(id);
        model.addAttribute("board", boardDTO);
        model.addAttribute("fileurl", CLOUD_PUBLIC_URL);
        System.out.println("boardDTO = " + boardDTO);
        if (boardDTO.getFileAttached() == 1) {
            List<BoardFileDTO> boardFileDTOList = boardService.findFile(id);
            model.addAttribute("boardFileList", boardFileDTOList);
        }
        return "detail";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") Long id, @AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        // 1. 게시글 본문 데이터 가져오기
        BoardDTO boardDTO = boardService.findById(id);
        model.addAttribute("board", boardDTO);
        model.addAttribute("fileurl", CLOUD_PUBLIC_URL);

        // 2. 파일이 있다면 파일 목록을 별도로 조회해서 모델에 담아야 함!
        if (boardDTO.getFileAttached() == 1) {
            List<BoardFileDTO> fileList = boardService.findFile(id); // 파일 목록 조회 메서드
            model.addAttribute("fileList", fileList); // HTML의 th:each="file : ${fileList}"와 일치해야 함
        }

        // 3. 로그인 상태 확인
        if (principalDetails != null) {
            // 2. 현재 로그인한 사용자의 이름(또는 이메일) 가져오기
            String loginName = principalDetails.getMember().getName();
            model.addAttribute("loginName", loginName);
        }
        return "update";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         BoardDTO boardDTO,
                         @RequestParam(value = "removeFiles", required = false) List<String> removeFiles,
                         Principal principal) throws IOException {

        // 1. 서비스에 DTO와 삭제할 파일 목록을 전달하여 업데이트 수행
        boardService.update(boardDTO, removeFiles, principal.getName());

        // 2. 수정이 완료된 후 상세 페이지로 리다이렉트
        // (직접 detail.html을 열면 새로고침 시 중복 제출 문제가 발생할 수 있음)
        return "redirect:/board/" + boardDTO.getId();
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, Principal principal) {
        BoardDTO board = boardService.findById(id);

        // 1. 로그인 상태인 경우
        if (principal != null) {
            // PrincipalDetails를 통해 현재 로그인한 사용자의 ID를 가져와 작성자와 비교
            // principal 객체에서 memberId를 꺼내기 위해 형변환이 필요할 수 있습니다.
            // 여기서는 간단히 email로 비교하거나, 서비스 레이어에 검증을 위임합니다.
            boardService.deleteWithAuth(id, principal.getName());
        } else {
            // 2. 비회원인 경우 (이미 JS에서 비회원 글임을 확인하고 보냈으므로 삭제 진행)
            // 보안을 강화하려면 서비스 레이어에서 암호를 한 번 더 체크하는 PostMapping 권장
            boardService.delete(id);
        }
        return "redirect:/list";
    }


}