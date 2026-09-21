# ADR-001: Modular Monolith First, Microservice'e Kademeli Gecis

## Context
Tek gelistirici + ogrenme projesi. Day-1'de 7 microservice; 7x build, dagitik
transaction, local'de ayaga kaldirma yuku demek. Bankacilikta en kritik sey
transfer tutarliligi (ACID).

## Decision
Phase 1-6: tek Spring Boot `banking-app`, icinde kati paket sinirlari:
`com.bank.identity / customer / account / transfer`.
Paketler arasi erisim sadece public service interface uzerinden.
Cross-schema SQL yasak. Phase 7 (Outbox+RabbitMQ) sonrasi kademeli bolunme.

## Alternatives
- B: Day-1 microservices. Reddedildi: ogrenmeyi yavaslatir, tutarliligi zorlastirir.
- C: Tek database tek schema. Reddedildi: bolunmeyi zorlastirir.

## Consequences
+ Hizli iterasyon, tek deploy, kolay @Transactional.
- Bolunme isciligini ileriye birakir. Paket disiplini sart (review ile denetlenecek).