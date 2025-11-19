package io.github.jihyundev.excel_download.controller;

import io.github.jihyundev.excel_download.dto.MemberDto;
import io.github.jihyundev.excel_download.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberApiController {
    private final MemberRepository memberRepository;

    @GetMapping("/members")
    public Page<MemberDto> memberList(@PageableDefault(size = 20) Pageable pageable){
        return memberRepository.memberPagination(pageable);
    }

}
