package com.demo.reservation.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemController {

    @Value("${app.pod-name}")
    private String podName;

    // K8s readiness/liveness probe + ALB 헬스체크 대상
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "UP");
        res.put("servedBy", podName);
        return res;
    }

    // [HPA 부하 시연 전용] CPU를 의도적으로 소모하는 엔드포인트.
    // 예약 오픈 부하를 재현할 때 이 엔드포인트를 동시에 때리면 CPU가 올라가 HPA가 스케일아웃됨.
    // ms = CPU를 태울 시간(밀리초), 최대 2000ms로 제한.
    @GetMapping("/load")
    public Map<String, Object> load(@RequestParam(defaultValue = "300") long ms) {
        long limit = Math.min(ms, 2000);
        long end = System.currentTimeMillis() + limit;
        double sink = 0;
        while (System.currentTimeMillis() < end) {
            sink += Math.sqrt(Math.random() * 99999.0);
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("servedBy", podName);
        res.put("burnedMs", limit);
        res.put("sink", sink);
        return res;
    }

    @GetMapping("/crash")
    public void crash() {
        System.exit(1);  // 앱 강제 종료 → 컨테이너 죽음 → K8s 자동 재시작
    }
}
