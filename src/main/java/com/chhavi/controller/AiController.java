package com.chhavi.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chhavi.ai.GeminiClient;
import com.chhavi.pojo.Candidate;
import com.chhavi.pojo.Election;
import com.chhavi.pojo.User;
import com.chhavi.repository.CandidateRepository;
import com.chhavi.repository.ElectionRepository;
import com.chhavi.repository.UserRepository;
import com.chhavi.repository.VoteRepository;

@Controller
public class AiController {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final ElectionRepository electionRepository;
    private final VoteRepository voteRepository;
    private final GeminiClient geminiClient;

    public AiController(UserRepository userRepository, CandidateRepository candidateRepository,
                        ElectionRepository electionRepository, VoteRepository voteRepository,
                        GeminiClient geminiClient) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.electionRepository = electionRepository;
        this.voteRepository = voteRepository;
        this.geminiClient = geminiClient;
    }

    @GetMapping("/voter/ai-assistant")
    public String showAssistantPage() {
        return "voter/ai-assistant";
    }

    @PostMapping("/ai/chat")
    @ResponseBody
    public String processChat(Principal principal, @RequestParam String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Message cannot be empty.";
        }

        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        Election activeElection = electionRepository.findByStatus("ACTIVE").orElse(null);
        List<Candidate> candidates = candidateRepository.findAll();

        boolean isActive = activeElection != null;
        boolean hasVoted = activeElection != null && voteRepository.existsByUserIdAndElectionId(user.getId(), activeElection.getId());

        String candidatesInfo = candidates.stream()
                .map(c -> c.getName() + " (" + c.getParty() + ") - Manifesto: " + (c.getManifesto() != null ? c.getManifesto() : "None"))
                .collect(Collectors.joining("; "));

        String factualContext = "Current Election State:\n"
                + "- Active Election exists: " + (isActive ? "Yes, title: " + activeElection.getTitle() : "No") + "\n"
                + "- Current user has voted: " + (hasVoted ? "Yes" : "No") + "\n"
                + "- Available candidates: " + candidatesInfo + "\n"
                + "- Voting process: Voter logs in, goes to 'Cast Vote' page, selects candidate, and clicks 'Submit Vote'.\n";

        String sysInstruction = "You are a neutral AI Voting Assistant. Use the following factual context to answer voter queries:\n"
                + factualContext + "\n"
                + "Strict Neutrality Instructions:\n"
                + "1. Do not tell users who to vote for, do not rank candidates, and do not manipulate choices.\n"
                + "2. Stated promises and manifestos must be summarized neutrally.\n"
                + "3. If asked for recommendations, explain that you are a neutral AI assistant designed only to explain the voting process and candidate manifestos neutrally.\n"
                + "4. Limit replies to a few sentences.";

        return geminiClient.generateContent(sysInstruction, message);
    }
}
