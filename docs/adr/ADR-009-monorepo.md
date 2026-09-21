# ADR-009: Monorepo

## Context
Backend + frontend + infra + docs ayni kiside. Degisiklikler cogu zaman
birlikte gidiyor (or: API degisti + frontend + compose).

## Decision
Tek repo `enterprise-level-banking-app`. Yapisi README'deki gibi.
Polyrepo yok.

## Alternatives
- Service basina repo. Reddedildi: 7 repo yonetimi, versiyon senkronu eziyet.
- Alternatif secilseydi: atomic commit kaybolur, CI 7 yerde bakim isterdi.

## Consequences
+ Tek PR'da uc-to-uca degisiklik, tek CI.
- Repo buyur; CODEOWNERS ve path-based CI ile cozulur (Phase 13).