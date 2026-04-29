package com.livescore.api.controllers;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/sofascore")
@CrossOrigin(origins = "*") 
public class SofaScoreProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    // Dùng lại domain .com kết hợp với chiến thuật "Ống nước"
    private static final String SOFASCORE_API = "https://api.sofascore.com/api/v1";
    private static final String SOFASCORE_IMAGE_API = "https://api.sofascore.com/api/v1";

    @GetMapping("/player/{id}")
    public ResponseEntity<?> getPlayerInfo(@PathVariable Long id) {
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
    public ResponseEntity<?> getTeamEvents(@PathVariable Long id, @PathVariable String type) {
        return proxyGet(SOFASCORE_API + "/team/" + id + "/events/" + type + "/0");
    }

    @GetMapping("/match/{id}/lineups")
    public ResponseEntity<?> getMatchLineups(@PathVariable Long id) {
        return proxyGet(SOFASCORE_API + "/event/" + id + "/lineups");
    }

    @GetMapping("/match/{id}/statistics")
    public ResponseEntity<?> getMatchStatistics(@PathVariable Long id) {
        return proxyGet(SOFASCORE_API + "/event/" + id + "/statistics");
    }

    @GetMapping("/fixtures/{date}")
    public ResponseEntity<?> getFixtures(@PathVariable String date) {
        return proxyGet(SOFASCORE_API + "/category/1/day/" + date + "/events");
    }

    @GetMapping("/league/{id}/matches")
    public ResponseEntity<?> getLeagueMatches(@PathVariable Long id) {
        // Chốt cứng mùa 76986 (Premier League) để bảo vệ đồ án
        return proxyGet(SOFASCORE_API + "/unique-tournament/" + id + "/season/76986/events/last/0");
    }

    @GetMapping("/league/{id}/standings")
    public ResponseEntity<?> getStandings(@PathVariable Long id) {
        return proxyGet(SOFASCORE_API + "/unique-tournament/" + id + "/season/76986/standings/total");
    }

    @GetMapping("/league/{id}/top-scorers")
    public ResponseEntity<?> getTopScorers(@PathVariable Long id) {
        String url = SOFASCORE_API + "/unique-tournament/" + id + "/season/76986" 
                   + "/statistics?limit=20&order=-goals&accumulation=total&fields=goals,assists,player,team";
        return proxyGet(url);
    }

    @GetMapping("/league/{id}/top-assists")
    public ResponseEntity<?> getTopAssists(@PathVariable Long id) {
        String url = SOFASCORE_API + "/unique-tournament/" + id + "/season/76986" 
                   + "/statistics?limit=20&order=-assists&accumulation=total&fields=goals,assists,player,team";
        return proxyGet(url);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAll(@RequestParam("q") String query) {
        String url = UriComponentsBuilder.fromUriString(SOFASCORE_API + "/search/all")
                .queryParam("q", query).toUriString();
        return proxyGet(url);
    }

    // 🏆 CHIẾN THUẬT "ỐNG NƯỚC" - CHUYỂN TIẾP TRỰC TIẾP DỮ LIỆU NÉN
    private ResponseEntity<?> proxyGet(String url) {
        try {
            HttpHeaders headers = createHeaders();
            headers.set("Accept-Encoding", "gzip"); // Xin file nén để qua mặt SofaScore
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Lấy nguyên cục bytes thô (không dịch ra String để tránh rác)
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            
            HttpHeaders returnHeaders = new HttpHeaders();
            returnHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            // Báo cho Flutter biết đây là file nén gzip để Flutter tự bung ra
            List<String> contentEncoding = response.getHeaders().get(HttpHeaders.CONTENT_ENCODING);
            if (contentEncoding != null && !contentEncoding.isEmpty()) {
                returnHeaders.put(HttpHeaders.CONTENT_ENCODING, contentEncoding);
            }
            
            return ResponseEntity.ok()
                    .headers(returnHeaders)
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Lỗi proxy: " + e.getMessage() + "\"}");
        }
    }

    private ResponseEntity<byte[]> proxyGetImage(String url) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            return restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.set("Referer", "https://www.sofascore.com/");
        headers.set("Origin", "https://www.sofascore.com");
        return headers;
    }
}