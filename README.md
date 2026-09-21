# Digital Banking Platform

Production-grade dijital bankacılık simülasyonu. Tutorial değil, enterprise pratiği: modular monolith ile başla, event-driven microservice'e evril.

---

## 🏛 Mimari (Özet)

```
React (TS + MUI) ──► ALB / WAF ──► EKS / Ingress ──► Spring Boot (Java 21, Modular Monolith)
                                                           │
        ┌──────────────────────────────────────────────────┼─────────────────────────────────┐
        │                                                  │                                 │
        ▼                                                  ▼                                 ▼
PostgreSQL (Schema-per-domain)                   Redis (Ref-Data Cache)              RabbitMQ (Transactional Outbox)
```

* **Observability:** Prometheus / Grafana / Loki + OpenTelemetry / Jaeger.
* **Detaylar:** Detaylı mimari kararlar için `docs/adr/` dizinine ve Phase 0 dokümanına bakın.

---

## 📁 Repo Yapısı

```text
.
├── services/
│   └── banking-app/            # Modular Monolith Spring Boot App
│       ├── identity/           # Kimlik doğrulama & yetkilendirme
│       ├── customer/           # Müşteri yönetimi
│       ├── account/            # Hesap yönetimi
│       └── transfer/           # Transfer & Ödeme işlemleri
├── frontend/
│   └── banking-web/            # React + TypeScript + MUI Web App (Phase 10)
├── infrastructure/
│   ├── docker/                 # Container tanımları ve compose dosyaları
│   ├── kubernetes/             # K8s manifestleri ve Helm chart'ları
│   ├── terraform/              # IaC script'leri
│   └── observability/          # Prometheus, Grafana, Jaeger yapılandırmaları
├── docs/                       # Mimari kararlar (ADR), API dokümanları ve runbook'lar
├── scripts/                    # Otomasyon ve devops script'leri
└── .github/
    └── workflows/              # CI/CD hatları
```

---

## 🚀 Hızlı Başlangıç

*(Phase 2 tamamlandıktan sonra geçerli olacaktır)*

Bağımlılıkları (PostgreSQL, Redis, RabbitMQ) ayağa kaldırmak için:

```bash
docker compose up -d
```

### Servis Portları:
* **PostgreSQL:** `localhost:5432`
* **Redis:** `localhost:6379`
* **RabbitMQ:** `localhost:5672` *(Management UI: `http://localhost:15672`)*

---

## 🌿 Branch ve Commit Stratejisi

```text
main (korumalı) ◄── develop ◄── feature/*
```

Projede **Conventional Commits** standartları zorunludur:
* `feat:` Yeni bir özellik eklendiğinde.
* `fix:` Bir hata düzeltildiğinde.
* `chore:` Bağımlılık güncellemeleri veya rutin işler.
* `docs:` Dokümantasyon değişiklikleri.
* `test:` Test ekleme veya düzenleme.
* `sec:` Güvenlik geliştirmeleri.

---

## 💰 Parasal İşlem Kuralları (Kritik)

1. **Veri Tipi:** Kesinlikle `float` veya `double` kullanılamaz. Tüm parasal alanlarda **`BigDecimal`** kullanılmalıdır.
2. **Eşzamanlılık ve Tutarlılık:** Tüm parasal işlemler `@Transactional` ve `@Version` (optimistic locking) ile korunmalıdır.
3. **Outbox Pattern:** Bakiye güncellemeleri ve olay (event) bildirimleri (Outbox Pattern) aynı veritabanı transaction'ı içerisinde yazılmalıdır.

---

## 🗺 Yol Haritası (Roadmap)

- [x] **Phase 0:** Mimari Tasarım & ADR'ların Belirlenmesi
- [ ] **Phase 1:** Bootstrap & Proje İskeleti *(Şu an buradayız)*
- [ ] **Phase 2:** Local Environment & Docker Compose
- [ ] **Phase 3:** Identity & Authentication Modülü
- [ ] **Phase 4:** Customer & Account Modülleri
- [ ] **Phase 5:** Transfer Engine & Transactional Outbox
- [ ] **Phase 6:** Observability & OpenTelemetry Entegrasyonu
- [ ] **Phase 7:** Resilience & Fault Tolerance (Resilience4j)
- [ ] **Phase 8:** Infrastructure as Code (Terraform)
- [ ] **Phase 9:** Kubernetes & EKS Deployment
- [ ] **Phase 10:** Frontend (React + TS + MUI)
