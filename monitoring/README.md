# Monitoring

## 1 ) Docker Compose
```yaml
services:
  # Prometheus
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - monitoring

  # Grafana
  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    user: "$UID:$GID"
    ports:
      - "3000:3000"
    volumes:
      - ./grafana-data:/var/lib/grafana
    depends_on:
      - prometheus
    networks:
      - monitoring

networks:
  monitoring:
    driver: bridge
```

### 1 - 1 ) prometheus.yml
- `host.docker.internal` 사용 이유
  -  Docker 컨테이너에서 호스트 머신의 네트워크에 접근하기 위해서임.
    - ✅ Docker 기준 네트워크의 **기본 게이트웨이는 "호스트 머신"이기 때문**이다     
- Linux 환경에서는 적용이 불가능 하므로 Gateway IP 주소 자체를 적어 진행 필요
  - 호스트 IP를 찾는 게 아니라, "컨테이너가 밖으로 나가는 게이트웨이"를 찾아 진행
    - 해당 게이트웨이를 통해 컨테이너가 호스트 머신의 서비스에 접근 가능    
- `job_name`을 사용해 수집할 대상을 지정할 수 있다.
  - `static_configs.targets` : 수집 대상 도메인 정보
  - `metrics_path` : 수집 대상 prometheus **actuator 주소**
  - `scrape_interval` : 수집 주기
```yaml
scrape_configs:
  # ⭐️ prometheus 자체 수집을 위해 사용 - 대규모 시스템에서는 Prometheus 자체를 모니터링하는 것이 중요할 수 있음
  - job_name: "prometheus"
    static_configs:
      - targets: ["host.docker.internal:9090"]  
    
  - job_name: "api-gateway-service"
    metrics_path: "/actuator/prometheus"
    scrape_interval: 5s
    static_configs:
      - targets: ["host.docker.internal:8000"]

  - job_name: "user-service"
    metrics_path: "/user-service/actuator/prometheus"
    scrape_interval: 5s
    static_configs:
      - targets: ["host.docker.internal:8000"]
        
  - job_name: "order-service"
    metrics_path: "/order-service/actuator/prometheus"
    scrape_interval: 5s
    static_configs:
      - targets: ["host.docker.internal:8000"]
```
