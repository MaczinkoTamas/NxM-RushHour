# Clean Code Guidelines

Clean Code is the project-wide development standard. Apply these rules to
production code and tests, including small changes and follow-up features.

- Give classes, methods, and variables clear names that describe their purpose.
- Keep each class and method focused; extract a named method when a block has a
  separate responsibility or becomes difficult to follow.
- Prefer immutable data and small interfaces. Avoid duplication and speculative
  abstractions.
- Validate inputs at boundaries and report invalid operations explicitly.
  Do not hide failures behind broad catches, silent defaults, or success-shaped
  fallbacks.
- Keep comments for non-obvious intent; use names and structure instead of
  comments that merely repeat the code.
- Write focused tests for observable behavior and edge cases. Keep test classes
  organized by the production behavior they cover; do not combine unrelated
  scenarios into one oversized test.
- Run `mvn verify` before considering a change complete. This checks tests,
  Checkstyle, and the JaCoCo coverage threshold.

The Maven Checkstyle rules in `config/checkstyle/checkstyle.xml` enforce a
baseline for both production and test sources. Passing automated checks does
not replace these guidelines or code review.
