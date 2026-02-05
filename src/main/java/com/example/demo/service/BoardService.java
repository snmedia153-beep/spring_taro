package com.example.demo.service;

import com.example.demo.Entity.Member;
import com.example.demo.dto.BoardDTO;
import com.example.demo.dto.BoardFileDTO;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final R2Service r2Service; // ✅ R2 서비스 주입
    private final MemberRepository memberRepository;

    public void save(BoardDTO boardDTO, String email) throws IOException {
        // 1. 이메일을 통해 회원 ID 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        // 2. DTO에 회원 ID와 이름 설정
        boardDTO.setMemberId(member.getId());
        boardDTO.setBoardWriter(member.getName()); // 인증된 이름 사용

        // 3. 로그인 유저의 경우 암호 필드를 서버 사이드에서 안전한 값으로 강제 설정
        // (이 값은 나중에 수정/삭제 시 '작성자 비교' 로직이 우선되므로 실제로는 사용되지 않음)
        boardDTO.setBoardPass("SECURE_AUTH_POST_" + member.getId());

        // 4. 파일 처리 및 저장
        List<MultipartFile> files = boardDTO.getBoardFile();
        // 1. 파일이 있는 경우만 체크
        if (files != null && !files.get(0).isEmpty()) {
            // 2. 개수 제한 체크 (최대 2개)
            if (files.size() > 2) {
                throw new RuntimeException("이미지는 최대 2개까지만 업로드 가능합니다.");
            }

            for (MultipartFile file : files) {
                // 3. 파일 형식 체크 (이미지 여부)
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new RuntimeException("이미지 파일만 업로드할 수 있습니다.");
                }

                // 4. 용량 체크 (150KB 이하)
                if (file.getSize() > 200 * 1024) {
                    throw new RuntimeException("파일 용량은 200KB를 초과할 수 없습니다.");
                }
            }

            // 모든 통과 후 기존 저장 로직 실행...
            boardDTO.setFileAttached(1);
            BoardDTO savedBoard = boardRepository.save(boardDTO);
            // 파일만 따로 가져오기
            for (MultipartFile boardFile: boardDTO.getBoardFile()) {
                // 파일 이름 가져오기
                String originalFilename = boardFile.getOriginalFilename();

                // ✅ 1. 파일 이름 앞에 'images/' 경로를 추가합니다.
                // 결과 예: images/171234567-photo.jpg
                String storedFileName = "images/" + System.currentTimeMillis() + "-" + originalFilename;
                System.out.println("originalFilename = " + originalFilename);
                // 저장용 이름 만들기
                System.out.println(System.currentTimeMillis());
                System.out.println("storedFileName = " + storedFileName);
                // ✅ 2. R2Service에 경로가 포함된 이름을 전달하여 업로드합니다.
                r2Service.uploadFile(boardFile, storedFileName);
                // BoardFileDTO 세팅
                BoardFileDTO boardFileDTO = new BoardFileDTO();
                boardFileDTO.setOriginalFileName(originalFilename);
                boardFileDTO.setStoredFileName(storedFileName);
                boardFileDTO.setBoardId(savedBoard.getId());
                // 파일 저장용 폴더에 파일 저장 처리
                //String savePath = "C:/Users/test/source/h2db/" + storedFileName;
                //boardFile.transferTo(new File(savePath));
                // board_file_table 저장 처리
                boardRepository.saveFile(boardFileDTO);
            }
        }else {
            // 파일 없다.
            boardDTO.setFileAttached(0);
            boardRepository.save(boardDTO);
        }
    }

    public List<BoardDTO> findAll() {
        return boardRepository.findAll();
    }

    public void updateHits(Long id) {
        boardRepository.updateHits(id);
    }

    public BoardDTO findById(Long id) {
        return boardRepository.findById(id);
    }
    @Transactional
    public void update(BoardDTO boardDTO, List<String> removeFiles, String loginEmail) throws IOException {
        // 1. 수정 권한 서버 측 재검증
        BoardDTO originalBoard = boardRepository.findById(boardDTO.getId());
        if (originalBoard == null) {
            throw new RuntimeException("존재하지 않는 게시글입니다.");
        }

        Member loginMember = memberRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new RuntimeException("사용자 정보가 없습니다."));

        // member_id를 통한 본인 확인
        if (originalBoard.getMemberId() == null || !originalBoard.getMemberId().equals(loginMember.getId())) {
            throw new RuntimeException("본인의 글만 수정할 수 있습니다.");
        }

        // 2. 기존 파일 삭제 처리 (체크박스 선택된 파일)
        if (removeFiles != null) {
            for (String storedFileName : removeFiles) {
                r2Service.deleteFile(storedFileName); // 클라우드(R2)에서 삭제
                boardRepository.deleteFileByStoredName(storedFileName); // DB에서 삭제
            }
        }

        // 3. 현재 남은 파일 개수 확인 (삭제 후 상태)
        List<BoardFileDTO> currentFilesAfterDelete = boardRepository.findFile(boardDTO.getId());
        int currentCount = currentFilesAfterDelete.size();

        // 4. 새 파일 업로드 (최대 2개 제한 유지)
        List<MultipartFile> newFiles = boardDTO.getBoardFile();
        if (newFiles != null && !newFiles.get(0).isEmpty()) {
            for (MultipartFile file : newFiles) {
                // 이미 파일이 2개라면 추가 업로드 중단
                if (currentCount >= 2) {
                    break;
                }

                // 3. 파일 형식 체크 (이미지 여부)
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new RuntimeException("이미지 파일만 업로드할 수 있습니다.");
                }

                // 4. 용량 체크 (150KB 이하)
                if (file.getSize() > 200 * 1024) {
                    throw new RuntimeException("파일 용량은 200KB를 초과할 수 없습니다.");
                }

                String storedFileName = "images/" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
                r2Service.uploadFile(file, storedFileName);

                BoardFileDTO fileDTO = new BoardFileDTO();
                fileDTO.setOriginalFileName(file.getOriginalFilename());
                fileDTO.setStoredFileName(storedFileName);
                fileDTO.setBoardId(boardDTO.getId());

                boardRepository.saveFile(fileDTO);
                currentCount++; // 파일 추가 후 카운트 증가
            }
        }

        // 5. ✨ 중요: fileAttached 상태 값 최종 결정
        // 새 파일 업로드까지 모두 마친 후, 최종적으로 파일이 하나라도 있는지 확인합니다.
        if (currentCount > 0) {
            boardDTO.setFileAttached(1);
        } else {
            boardDTO.setFileAttached(0);
        }

        // 6. 게시글 본문 및 fileAttached 상태 업데이트
        boardRepository.update(boardDTO);
    }

    @Transactional
    public void deleteWithAuth(Long id, String loginEmail) {
        BoardDTO board = boardRepository.findById(id);
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new RuntimeException("사용자 정보가 없습니다."));

        // 작성자가 있고, 로그인한 사람과 일치하는지 확인
        if (board.getMemberId() != null && board.getMemberId().equals(member.getId())) {
            // R2 파일 삭제 로직 포함 (기존에 작성하신 로직 활용)
            delete(id);
        } else {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
    }
    @Transactional
    public void delete(Long id) {
        // 1. 해당 게시글에 연결된 파일 목록 조회
        List<BoardFileDTO> boardFileList = boardRepository.findFile(id);

        // 2. R2 스토리지에서 실제 파일 삭제
        for (BoardFileDTO boardFile : boardFileList) {
            r2Service.deleteFile(boardFile.getStoredFileName());
        }

        // 3. DB에서 게시글 및 파일 정보 삭제
        boardRepository.delete(id);
    }


    public List<BoardFileDTO> findFile(Long id) {
        return boardRepository.findFile(id);
    }
}