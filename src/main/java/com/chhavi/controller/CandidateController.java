package com.chhavi.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.chhavi.ai.GeminiClient;
import com.chhavi.dto.CandidateRequestDto;
import com.chhavi.pojo.Candidate;
import com.chhavi.repository.CandidateRepository;

import jakarta.validation.Valid;

@Controller
public class CandidateController {

    private final CandidateRepository candidateRepository;
    private final GeminiClient geminiClient;
    private final Map<Long, String> manifestoSummaryCache = new HashMap<>();

    public CandidateController(CandidateRepository candidateRepository, GeminiClient geminiClient) {
        this.candidateRepository = candidateRepository;
        this.geminiClient = geminiClient;
    }

    // Admin View
    @GetMapping("/admin/candidates")
    public String adminListCandidates(Model model) {
        model.addAttribute("candidates", candidateRepository.findAll());
        return "admin/candidates";
    }

    @GetMapping("/admin/candidates/new")
    public String showCandidateForm(Model model) {
        model.addAttribute("candidateDto", new CandidateRequestDto());
        return "admin/candidate-form";
    }

    @PostMapping("/admin/candidates")
    public String saveCandidate(
            @Valid @ModelAttribute("candidateDto") CandidateRequestDto dto,
            BindingResult result,
            @RequestParam(value = "symbolImageFile", required = false) org.springframework.web.multipart.MultipartFile symbolImageFile,
            Model model) {

        if (result.hasErrors()) {
            return "admin/candidate-form";
        }

        Candidate candidate = new Candidate();
        candidate.setName(dto.getName());
        candidate.setParty(dto.getParty());
        
        if (symbolImageFile != null && !symbolImageFile.isEmpty()) {
            try {
                byte[] bytes = symbolImageFile.getBytes();
                String base64Image = "data:" + symbolImageFile.getContentType() + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
                candidate.setSymbol(base64Image);
            } catch (Exception e) {
                candidate.setSymbol(dto.getSymbol());
            }
        } else {
            candidate.setSymbol(dto.getSymbol());
        }

        candidate.setManifesto(dto.getManifesto());
        candidate.setProfileImage(dto.getProfileImage());
        candidate.setCreatedAt(LocalDateTime.now());
        candidate.setUpdatedAt(LocalDateTime.now());

        candidateRepository.save(candidate);
        return "redirect:/admin/candidates";
    }

    @GetMapping("/admin/candidates/edit/{id}")
    public String showEditCandidateForm(@PathVariable Long id, Model model) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        CandidateRequestDto dto = new CandidateRequestDto();
        dto.setName(candidate.getName());
        dto.setParty(candidate.getParty());
        dto.setSymbol(candidate.getSymbol());
        dto.setManifesto(candidate.getManifesto());
        dto.setProfileImage(candidate.getProfileImage());

        model.addAttribute("candidateDto", dto);
        model.addAttribute("candidateId", id);
        return "admin/candidate-form";
    }

    @PostMapping("/admin/candidates/edit/{id}")
    public String updateCandidate(
            @PathVariable Long id,
            @Valid @ModelAttribute("candidateDto") CandidateRequestDto dto,
            BindingResult result,
            @RequestParam(value = "symbolImageFile", required = false) org.springframework.web.multipart.MultipartFile symbolImageFile,
            Model model) {

        if (result.hasErrors()) {
            return "admin/candidate-form";
        }

        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        candidate.setName(dto.getName());
        candidate.setParty(dto.getParty());
        
        if (symbolImageFile != null && !symbolImageFile.isEmpty()) {
            try {
                byte[] bytes = symbolImageFile.getBytes();
                String base64Image = "data:" + symbolImageFile.getContentType() + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
                candidate.setSymbol(base64Image);
            } catch (Exception e) {
                // keep old or fallback
            }
        } else {
            if (dto.getSymbol() != null && !dto.getSymbol().trim().isEmpty() && !dto.getSymbol().startsWith("data:image")) {
                candidate.setSymbol(dto.getSymbol());
            }
        }

        candidate.setManifesto(dto.getManifesto());
        candidate.setProfileImage(dto.getProfileImage());
        candidate.setUpdatedAt(LocalDateTime.now());

        candidateRepository.save(candidate);

        // Evict AI cache on update
        manifestoSummaryCache.remove(id);

        return "redirect:/admin/candidates";
    }

    @PostMapping("/admin/candidates/delete/{id}")
    public String deleteCandidate(@PathVariable Long id) {
        // Soft delete / remove implementation simple logic (can hard delete since constraints are not active yet, or just delete)
        try {
            candidateRepository.deleteById(id);
        } catch (Exception e) {
            // Fallback soft delete logic if database prevents delete due to existing votes
            Candidate candidate = candidateRepository.findById(id).orElse(null);
            if (candidate != null) {
                // Here you would set status = INACTIVE if candidate has status
                // But for now, we just delete or log
            }
        }
        return "redirect:/admin/candidates";
    }

    // Voter View
    @GetMapping("/voter/candidates")
    public String voterListCandidates(Model model) {
        model.addAttribute("candidates", candidateRepository.findAll());
        return "voter/candidates";
    }

    @GetMapping("/voter/candidates/{id}")
    public String voterCandidateDetails(@PathVariable Long id, Model model) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        model.addAttribute("candidate", candidate);

        // Retrieve or generate manifesto AI summary
        String summary = manifestoSummaryCache.computeIfAbsent(id, cid -> {
            String manifesto = candidate.getManifesto();
            if (manifesto == null || manifesto.trim().isEmpty()) {
                return "No manifesto provided by the candidate.";
            }
            if (manifesto.length() > 5000) {
                return "Manifesto is too long for AI summary.";
            }

            String sysInstruction = "Summarize the provided candidate manifesto neutrally. Highlight the main stated promises and policy areas. Do not endorse, rank, compare, or recommend the candidate.";
            return geminiClient.generateContent(sysInstruction, manifesto);
        });

        model.addAttribute("summary", summary);
        return "voter/candidate-details";
    }
}
