package com.livescore.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/sofascore")
@CrossOrigin(origins = "*") // Mở cửa cho app Flutter gọi vào
public class SofaScoreProxyController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // Bộ đọc JSON siêu tốc

    // --- HẰNG SỐ CẤU HÌNH (Đã nâng cấp chống Cloudflare) ---
    // Đổi toàn bộ sang đuôi .app
    private static final String SOFASCORE_API = "https://api.sofascore.app/api/v1";
    private static final String SOFASCORE_IMAGE_API = "https://api.sofascore.app/api/v1";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    // ==========================================
    // 1. NHÓM DỮ LIỆU CẦU THỦ & ĐỘI BÓNG
    // ==========================================
    @GetMapping("/player/{id}")
    public ResponseEntity<String> getPlayerInfo(@PathVariable Long id) {
        return proxyGet(SOFASCORE_API + "/player/" + id);
    }

    @GetMapping("/player-image/{id}")
    public ResponseEntity<byte[]> getPlayerImage(@PathVariable Long id) {
        return proxyGetImage(SOFASCORE_IMAGE_API + "/player/" + id + "/image");
    }

    @GetMapping("/team-logo/{id}")
    public ResponseEntity<byte[]> getTeamLogo(@PathVariable Long id) {
        return proxyGetImage(SOFASCORE_IMAGE_API + "/team/" + id + "/image");
    }

    @GetMapping("/team/{id}/events/{type}")
    public ResponseEntity<String> getTeamEvents(@PathVariable Long id, @PathVariable String type) {
        return proxyGet(SOFASCORE_API + "/team/" + id + "/events/" + type + "/0");
    }

    // ==========================================
    // 2. NHÓM DỮ LIỆU TRẬN ĐẤU (MATCH)
    // ==========================================
    @GetMapping("/match/{id}/lineups")
    public ResponseEntity<String> getMatchLineups(@PathVariable Long id) {
        return proxyGet(SOFASCORE_API + "/event/" + id + "/lineups");
    }

    @GetMapping("/match/{id}/statistics")
    public ResponseEntity<String> getMatchStatistics(@PathVariable Long id) {
        return proxyGet(SOFASCORE_API + "/event/" + id + "/statistics");
    }

    @GetMapping("/fixtures/{date}")
    public ResponseEntity<String> getFixtures(@PathVariable String date) {
        return proxyGet(SOFASCORE_API + "/category/1/day/" + date + "/events");
    }

    // ==========================================
    // 3. NHÓM DỮ LIỆU GIẢI ĐẤU (TỰ ĐỘNG CHUYỂN MÙA GIẢI)
    // ==========================================
    @GetMapping("/league/{id}/matches")
    public ResponseEntity<String> getLeagueMatches(@PathVariable Long id) {
        String seasonId = getCurrentSeasonId(id); 
        return proxyGet(SOFASCORE_API + "/unique-tournament/" + id + "/season/" + seasonId + "/events/last/0");
    }

    @GetMapping("/league/{id}/standings")
    public ResponseEntity<String> getStandings(@PathVariable Long id) {
        String seasonId = getCurrentSeasonId(id);
        return proxyGet(SOFASCORE_API + "/unique-tournament/" + id + "/season/" + seasonId + "/standings/total");
    }

    @GetMapping("/league/{id}/top-scorers")
    public ResponseEntity<String> getTopScorers(@PathVariable Long id) {
        String seasonId = getCurrentSeasonId(id);
        String url = SOFASCORE_API + "/unique-tournament/" + id + "/season/" + seasonId 
                   + "/statistics?limit=20&order=-goals&accumulation=total&fields=goals,assists,player,team";
        return proxyGet(url);
    }

    @GetMapping("/league/{id}/top-assists")
    public ResponseEntity<String> getTopAssists(@PathVariable Long id) {
        String seasonId = getCurrentSeasonId(id);
        String url = SOFASCORE_API + "/unique-tournament/" + id + "/season/" + seasonId 
                   + "/statistics?limit=20&order=-assists&accumulation=total&fields=goals,assists,player,team";
        return proxyGet(url);
    }

    // ==========================================
    // 4. TÌM KIẾM
    // ==========================================
    @GetMapping("/search")
    public ResponseEntity<String> searchAll(@RequestParam("q") String query) {
        String url = UriComponentsBuilder.fromUriString(SOFASCORE_API + "/search/all")
                .queryParam("q", query)
                .toUriString();
        return proxyGet(url);
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ LÕI & TỰ ĐỘNG HÓA
    // ==========================================

    private String getCurrentSeasonId(Long tournamentId) {
        try {
            String url = SOFASCORE_API + "/unique-tournament/" + tournamentId + "/seasons";
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("seasons").get(0).path("id").asText();
        } catch (Exception e) {
            return "76986"; 
        }
    }

    private ResponseEntity<String> proxyGet(String url) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Lỗi proxy: " + e.getMessage() + "\"}");
        }
    }

    private ResponseEntity<byte[]> proxyGetImage(String url) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // ✅ HÀM TẠO HEADER NGỤY TRANG "VŨ KHÍ HẠNG NẶNG" ĐỂ QUA MẶT CLOUDFLARE
    private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
    headers.set("Accept", "application/json, text/plain, */*");
    headers.set("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7");
    headers.set("Accept-Encoding", "gzip, deflate, br"); // Rất quan trọng để giả lập trình duyệt
    headers.set("Referer", "https://www.sofascore.com/");
    headers.set("Origin", "https://www.sofascore.com");
    headers.set("Connection", "keep-alive");
    headers.set("Sec-Fetch-Dest", "empty");
    headers.set("Sec-Fetch-Mode", "cors");
    headers.set("Sec-Fetch-Site", "same-site");
    
    // Thêm các thông số Client Hint của Chrome 124
    headers.set("sec-ch-ua", "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"");
    headers.set("sec-ch-ua-mobile", "?0");
    headers.set("sec-ch-ua-platform", "\"Windows\"");
    
    return headers;
    }

}