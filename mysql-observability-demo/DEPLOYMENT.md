# Deployment Guide - Where Does This Run? 🚀

## Runtime Environment

This demo project runs in a **local development environment** with the following components:

### 1. Your Local Machine (Host)
- **Spring Boot Application** (Java process)
  - Runs directly on your machine
  - Port: 8080
  - Requires: Java 21+ and Maven

### 2. Docker Containers (via Docker Compose)
- **MySQL Database** (Container: `mysql-demo`)
  - Port: 3306
  - Volume: `mysql_data`

- **Prometheus** (Container: `prometheus-demo`)
  - Port: 9090
  - Volume: `prometheus_data`

- **Loki** (Container: `loki-demo`)
  - Port: 3100
  - Volume: `loki_data`

- **Tempo** (Container: `tempo-demo`)
  - Ports: 3200, 4317, 4318
  - Volume: `tempo_data`

- **Grafana** (Container: `grafana-demo`)
  - Port: 3000
  - Volume: `grafana_data`

## Architecture Diagram

```
┌─────────────────────────────────────────────┐
│         Your Local Machine (Host)           │
│                                             │
│  ┌───────────────────────────────────────┐ │
│  │   Spring Boot Application             │ │
│  │   (Java Process)                      │ │
│  │   Port: 8080                          │ │
│  │   - REST APIs                         │ │
│  │   - Query Observability               │ │
│  └──────────┬────────────────────────────┘ │
│             │                               │
│             │ Connects to                   │
│             ▼                               │
│  ┌─────────────────────────────────────┐   │
│  │     Docker Network (bridge)         │   │
│  │                                     │   │
│  │  ┌────────┐  ┌──────────┐          │   │
│  │  │ MySQL  │  │Prometheus│          │   │
│  │  │:3306   │  │:9090     │          │   │
│  │  └────────┘  └──────────┘          │   │
│  │                                     │   │
│  │  ┌────────┐  ┌──────────┐          │   │
│  │  │ Loki   │  │  Tempo   │          │   │
│  │  │:3100   │  │:3200     │          │   │
│  │  └────────┘  └──────────┘          │   │
│  │                                     │   │
│  │  ┌────────────────────┐             │   │
│  │  │     Grafana        │             │   │
│  │  │      :3000         │             │   │
│  │  └────────────────────┘             │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

## System Requirements

### Minimum Requirements
- **OS**: macOS, Linux, or Windows 10/11 with WSL2
- **CPU**: 2 cores
- **RAM**: 4GB
- **Disk**: 2GB free space

### Recommended Requirements
- **OS**: macOS or Linux
- **CPU**: 4+ cores
- **RAM**: 8GB+
- **Disk**: 5GB free space

## Software Prerequisites

### Required
1. **Java Development Kit (JDK) 21+**
   ```bash
   java -version
   # Should show: java version "21" or higher
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   # Should show: Apache Maven 3.6.x or higher
   ```

3. **Docker 20.x+**
   ```bash
   docker --version
   # Should show: Docker version 20.x or higher
   ```

4. **Docker Compose 2.x+**
   ```bash
   docker-compose --version
   # Should show: Docker Compose version 2.x or higher
   ```

### Installation Guides

#### macOS
```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java 21
brew install openjdk@21

# Install Maven
brew install maven

# Install Docker Desktop
# Download from: https://www.docker.com/products/docker-desktop
```

#### Linux (Ubuntu/Debian)
```bash
# Install Java 21
sudo apt update
sudo apt install openjdk-21-jdk

# Install Maven
sudo apt install maven

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install docker-compose-plugin
```

#### Windows
1. Install [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)
2. Install [Java 21 JDK](https://adoptium.net/)
3. Install [Maven](https://maven.apache.org/download.cgi)
4. Use PowerShell or WSL2 for running scripts

## Deployment Options

### Option 1: Local Development (Default)
Run everything on your local machine for development and testing.

**Pros:**
- Easy to debug
- Fast iteration
- Full control

**Cons:**
- Uses local resources
- Not production-ready

### Option 2: Remote Server
Deploy to a remote server (VM, cloud instance).

**Steps:**
1. Copy project to server
2. Install prerequisites
3. Run `./scripts/start-stack.sh`
4. Update Prometheus config to use server IP
5. Access via server IP

**Suitable for:**
- Team demos
- Staging environment
- Testing at scale

### Option 3: Kubernetes (Advanced)
For production deployments, convert to Kubernetes.

**Required Changes:**
- Create Kubernetes manifests
- Use Helm charts
- Set up ingress
- Configure persistent volumes
- Use Kubernetes service discovery

**Not included in this demo.**

## Port Usage

| Service | Port | Protocol | Access |
|---------|------|----------|--------|
| Spring Boot App | 8080 | HTTP | localhost:8080 |
| MySQL | 3306 | TCP | localhost:3306 |
| Grafana | 3000 | HTTP | localhost:3000 |
| Loki | 3100 | HTTP | localhost:3100 |
| Tempo HTTP | 3200 | HTTP | localhost:3200 |
| Tempo OTLP gRPC | 4317 | gRPC | localhost:4317 |
| Tempo OTLP HTTP | 4318 | HTTP | localhost:4318 |
| Prometheus | 9090 | HTTP | localhost:9090 |

Make sure these ports are not in use:
```bash
# Check if ports are available
lsof -i :8080
lsof -i :3306
lsof -i :3000
```

## Data Persistence

All data is stored in Docker volumes:

```bash
# List volumes
docker volume ls | grep mysql-observability-demo

# Inspect volume
docker volume inspect mysql-observability-demo_mysql_data

# Backup volume
docker run --rm -v mysql-observability-demo_mysql_data:/data \
  -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz /data

# Restore volume
docker run --rm -v mysql-observability-demo_mysql_data:/data \
  -v $(pwd):/backup alpine tar xzf /backup/mysql-backup.tar.gz -C /

# Remove all volumes (clean slate)
docker-compose down -v
```

## Network Configuration

The demo uses Docker's bridge network for container communication.

**Host to Container:**
- Use `localhost` or `127.0.0.1`

**Container to Host:**
- macOS/Windows: Use `host.docker.internal`
- Linux: Use `172.17.0.1` (or check with `ip addr show docker0`)

If you're on Linux, update `observability/prometheus/prometheus.yml`:
```yaml
scrape_configs:
  - job_name: 'spring-boot-app'
    static_configs:
      - targets: ['172.17.0.1:8080']  # Change from host.docker.internal
```

## Firewall Configuration

If you have a firewall enabled:

### macOS
```bash
# Allow Docker
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --add $(which docker)
```

### Linux (UFW)
```bash
# Allow ports
sudo ufw allow 8080/tcp
sudo ufw allow 3000/tcp
sudo ufw allow 9090/tcp
```

### Windows
Add rules in Windows Defender Firewall for Docker Desktop.

## Cloud Deployment (Optional)

### AWS EC2
```bash
# Launch EC2 instance (t3.medium or larger)
# Install prerequisites
# Clone project
# Run: ./scripts/start-stack.sh
# Access via EC2 public IP
```

### Google Cloud VM
```bash
# Create VM (n1-standard-2 or larger)
# Install prerequisites
# Clone project
# Run: ./scripts/start-stack.sh
# Configure firewall rules
```

### Azure VM
```bash
# Create VM (Standard_B2s or larger)
# Install prerequisites
# Clone project
# Run: ./scripts/start-stack.sh
# Configure NSG rules
```

## Monitoring Resource Usage

```bash
# Monitor Docker containers
docker stats

# Check disk usage
docker system df

# View container logs
docker-compose logs -f

# Check Java process
ps aux | grep java
jps -lv
```

## Cleanup

```bash
# Stop services
./scripts/stop-stack.sh

# Remove volumes
docker-compose down -v

# Remove images
docker-compose down --rmi all

# Full cleanup
docker system prune -a --volumes
```

## Troubleshooting

### Port Already in Use
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>
```

### Docker Network Issues
```bash
# Restart Docker
sudo systemctl restart docker  # Linux
# or restart Docker Desktop on macOS/Windows

# Recreate network
docker-compose down
docker network prune
docker-compose up -d
```

### Out of Memory
```bash
# Increase Docker memory limit in Docker Desktop settings
# Recommended: 4GB minimum, 8GB for best experience
```

## Production Considerations

This demo is NOT production-ready. For production:

1. **Security**
   - Use secrets management (Vault, AWS Secrets Manager)
   - Enable TLS/SSL
   - Set strong passwords
   - Configure authentication

2. **Scalability**
   - Use managed databases (RDS, Cloud SQL)
   - Scale Prometheus (Thanos, Cortex)
   - Use Loki in distributed mode
   - Add load balancers

3. **High Availability**
   - Multiple replicas
   - Database replication
   - Backup automation
   - Disaster recovery plan

4. **Monitoring**
   - Set up alerting
   - Create on-call schedules
   - Monitor the monitors

---

For questions or issues, check the main [README.md](README.md) or open an issue.
