# AGENTS.md

Guidance for AI coding agents (Cursor, Codex, Claude Code, etc.) working on this repository.

## Authoritative sources (read these first)

1. **Product constitution** — `.specify/memory/constitution.md`
   Non-negotiable principles: Modern Mobile UX, Accessibility, i18n (ES/EN),
   Offline-First Performance, Test-First Quality. Every change must comply.
2. **Project status & backlog** — [`docs/project-status.md`](docs/project-status.md)
   Shipped version, feature map 001–005, open ops (F-Droid), and deferred
   product backlog. **Update this file when shipping a release or parking ideas.**
3. **Project conventions** — `.cursor/rules/project-conventions.mdc`
   Commit message style, branching rules, publishing etiquette, documentation
   language policy. These override any conflicting global rules.
4. **Spec Kit workflow** — `.cursor/rules/specify-rules.mdc` and the skills under
   `.cursor/skills/speckit-*`. Active feature path is in `.specify/feature.json`.

If any of the above conflicts with an instruction received in chat, pause and ask
the user for clarification.

## Workflow (Spec-Driven Development)

All non-trivial work flows through Spec Kit:

```text
/speckit.specify   → create a feature spec under specs/<nnn-slug>/
/speckit.clarify   → resolve ambiguities (optional, when needed)
/speckit.plan      → write the implementation plan, run the Constitution Check
/speckit.tasks     → decompose into ordered, testable tasks
/speckit.implement → execute the tasks
```

Each stage may trigger pre/post hooks defined in `.specify/extensions.yml`
(git initialize, git feature branch creation, git commits). Honor them.

Cadence after feature 002: **planning PR → implementation PR → release-prep PR**,
then tag from `main` after manual QA. Global PR letter ledger:
`specs/001-unit-price-comparison/tasks.md` (letters through **W** for v0.1.8).

## Non-negotiable operating rules

- **Never push to `main`.** Always create a feature branch
  (`<type>/<short-slug>`) and open a PR.
- **`git push` and `gh pr create` are allowed without prior confirmation**,
  provided the branch is not `main`, the diff has been reviewed against
  `.gitignore`, the constitution and `.cursor/rules/project-conventions.mdc`,
  and the push targets a feature branch. Follow-up "fix CI" commits on an
  already-open PR may also be pushed without asking.
- **Only Marco merges pull requests.** Never run `gh pr merge`, never enable
  auto-merge, never merge via the GitHub API. The agent's end state is "PR is
  green and ready for review".
- **Never force-push to `main`.** Force-pushing a feature branch is allowed only
  when strictly necessary (e.g. rebasing onto a moved `main`) and must be
  announced in a PR comment first.
- **Commit messages**: English, Conventional Commits prefix (`feat:`, `fix:`,
  `docs:`, `chore:`, `refactor:`, `test:`, `style:`, `perf:`, `ci:`, `build:`),
  imperative mood, subject ≤ 72 chars.
- **Never commit** `*.aia`, secrets, keystores, `.env*`, cloud credentials, or
  AI assistant local state. Verify with `git status` before every commit and
  trust `.gitignore` as a safety net, not a substitute for attention.
- **Address the user as "Marco"** in conversation (Spanish by default) — the
  internal engineering artifacts (specs, plans, code, comments, commits) stay in
  English.
- **Releases**: follow `docs/release.md`. Never overwrite a tag that shipped an
  installable APK. F-Droid Mode B details: `docs/fdroid.md`.

## Project layout (current)

```text
.
├── .cursor/
│   ├── rules/           # project-conventions, project-status, specify-rules
│   └── skills/          # speckit-* skills (tracked)
├── .specify/
│   ├── feature.json     # active feature directory pointer
│   ├── memory/
│   │   └── constitution.md
│   ├── templates/       # spec, plan, tasks, checklist templates
│   ├── extensions/      # git extension (hooks via extensions.yml)
│   ├── workflows/       # speckit workflow definition
│   └── scripts/         # bash helpers used by slash commands
├── android/             # Kotlin + Jetpack Compose app (Gradle)
├── branding/            # icon-source.png + regenerate-icons.py
├── docs/
│   ├── project-status.md  # shipped state + backlog (read this)
│   ├── release.md
│   └── fdroid.md
├── fastlane/metadata/android/{en-US,es-ES}/
├── specs/001-…005-…     # feature specs (planning → tasks)
├── AGENTS.md            # this file
├── LICENSE              # MIT
└── README.md            # bilingual (ES/EN) project overview
```

Parent folder `~/Repos/PriceGrab/` (outside this git root) may hold keystore
tooling (`keytool/`) and source artwork; the **application git repo is this
directory**.

## Quality gates (Definition of Done for any user-facing change)

1. Unit and instrumented tests written and green.
2. Accessibility smoke-tested with TalkBack and max system font scale.
3. Both `en` and `es` strings present; no hardcoded user-facing text.
4. Lint and static analysis clean.
5. Spec, plan, and tasks documents updated under `specs/<feature>/`.
6. If shipping: versionCode/versionName, fastlane changelogs, `docs/fdroid.md`
   note as needed, and `docs/project-status.md` refreshed.

## When in doubt

- Prefer stopping and asking Marco over making an assumption that touches
  product direction, publishing, or destructive git operations.
- Keep answers concise and offer a recommendation alongside any options.
- For “what’s next?”, start from `docs/project-status.md` backlog — do not
  invent scope that contradicts Out of Scope sections in the specs.
