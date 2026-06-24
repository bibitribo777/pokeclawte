# Fork Notes

This repository is a personal fork of `agents-io/PokeClaw`.

## Current fork status

- Working repository: `PhoneAgent Lab`
- Upstream project: `agents-io/PokeClaw`
- Base license: Apache-2.0
- Initial cleanup started on 2026-06-25

## Important trademark note

The upstream README states that `PokeClaw` is a trademark of Nicole / agents.io and that forks must be renamed before distribution.

This fork should therefore not be distributed as `PokeClaw` unless explicit permission exists. Use a separate app name, icon, package identity, release signing key, and public README before shipping APKs to other users.

## Cleanup already started

- Removed the temporary ChatGPT push-check file.
- Changed visible app name strings from `PokeClaw` to `PhoneAgent Lab` in the main English strings file.
- Marked build output as a fork build.
- Switched Gradle wrapper download from a third-party mirror to the official Gradle distribution URL.
- Disabled cleartext HTTP traffic by default.

## Still required before public distribution

1. Replace original PokeClaw logos, screenshots, and banner images.
2. Rewrite the README so it clearly describes this fork and links back to upstream only as attribution.
3. Rename package/application identifiers away from `io.agents.pokeclaw`.
4. Review all Android permissions and remove anything not needed for the fork's actual use case.
5. Create a private release keystore and configure release signing through GitHub Secrets.
6. Run a clean debug build and install it only on a test Android device first.
7. Run accessibility, notification, overlay, model-download, and task-execution smoke tests.

## Safety note

This app uses Android Accessibility features that can read screen content, perform gestures, and automate other apps. Treat test builds as high-trust software and do not install unreviewed builds on a primary phone with sensitive accounts.
