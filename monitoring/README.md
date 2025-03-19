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

### 1 - 1 ) Prometheus.yml
- `host.docker.internal` 사용 이유
  -  Docker 컨테이너에서 호스트 머신의 네트워크에 접근하기 위해서임.
    - ✅ Docker 기준 네트워크의 **기본 게이트웨이는 "호스트 머신"이기 때문**이다     
- Linux 환경에서는 적용이 불가능 하므로 Gateway IP 주소 자체를 적어 진행 필요
  - 호스트 IP를 찾는 게 아니라, "컨테이너가 밖으로 나가는 게이트웨이"를 찾아 진행
    - 해당 게이트웨이를 통해 컨테이너가 호스트 머신의 서비스에 접근 가능    
    
```yaml
scrape_configs:
  - job_name: "dt-server"
    metrics_path: "/actuator/prometheus"
    scrape_interval: 5s
    static_configs:
      - targets: ["host.docker.internal:9081"]
```
