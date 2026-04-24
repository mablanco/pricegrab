# AGENTS.md

Guidance for AI coding agents (Cursor, Codex, Claude Code, etc.) working on this repository.

## Authoritative sources (read these first)

1. **Product constitution** — `.specify/memory/constitution.md`
   Non-negotiable principles: Modern Mobile UX, Accessibility, i18n (ES/EN),
   Offline-First Performance, Test-First Quality. Every change must comply.
2. **Project conventions** — `.cursor/rules/project-conventions.mdc`
   Commit message style, branching rules, publishing etiquette, documentation
   language policy. These override any conflicting global rules.
3. **Spec Kit workflow** — `.cursor/rules/specify-rules.mdc` and the skills under
   `.cursor/skills/speckit-*`.

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

## Project layout (current)

```text
.
├── .cursor/
│   ├── rules/           # specify-rules.mdc, project-conventions.mdc
│   └── skills/          # speckit-* skills (tracked)
├── .specify/
│   ├── memory/
│   │   └── constitution.md
│   ├── templates/       # spec, plan, tasks, checklist templates
│   ├── extensions/      # git extension (hooks run via extensions.yml)
│   ├── workflows/       # speckit workflow definition
│   └── scripts/         # bash helpers used by slash commands
├── AGENTS.md            # this file
├── LICENSE              # MIT
└── README.md            # bilingual (ES/EN) project overview
```

The native Android sources (Kotlin, Gradle modules) will land under `android/`
once the first feature spec drives the scaffolding.

## Quality gates (Definition of Done for any user-facing change)

1. Unit and instrumented tests written and green.
2. Accessibility smoke-tested with TalkBack and max system font scale.
3. Both `en` and `es` strings present; no hardcoded user-facing text.
4. Lint and static analysis clean.
5. Spec, plan, and tasks documents updated under `specs/<feature>/`.

## When in doubt

- Prefer stopping and asking Marco over making an assumption that touches
  product direction, publishing, or destructive git operations.
- Keep answers concise and offer a recommendation alongside any options.
