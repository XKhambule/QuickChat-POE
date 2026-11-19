# QuickChat — Final PROG5121 PoE (Parts 1–3)

This NetBeans Maven console project contains Part 1 registration/login, Part 2 message entry and JSON storage, and Part 3 arrays and message reports.

## How to run
Open this folder as a Maven project in Apache NetBeans. Use JDK 21 or later. Run `MainPOE.java`. To run tests, select **Test Project** or run `mvn clean test` where Maven is installed. JSON messages are saved to `messages.json` in the working directory. Do not include private phone numbers or passwords in public GitHub commits.

## Main menu
1. Send Messages — enter the permitted number of messages; choose Send, Disregard or Store.
2. Show recently sent messages — shows sent-message details.
3. Quit.
4. Stored Messages — sender/recipient listing, longest stored message, search by ID, search by recipient, delete by hash, and full report.

## Important assignment details
- The five sample messages are used **only in JUnit tests**, not hard-coded into the application.
- The PoE test table lists `0838884567` for message 4, but Part 2 requires `+27` followed by nine digits. The test uses the supplied value in its array; live input continues to validate international numbers.
- The PoE test table labels the message 4 phone number under a message-ID search; the application searches by its generated **10-digit message ID**, as the feature specification requires.
- The Part 3 report includes full details of **stored** messages; the separate sent report shows **sent** messages.
- Sent and disregarded messages remain in memory for the current run; stored messages persist in JSON across runs.

## Sources and attribution
Google (n.d.) *Gson User Guide*. https://github.com/google/gson/blob/main/UserGuide.md (accessed 19 November 2026).
Google (n.d.) *Gson API documentation*. https://javadoc.io/doc/com.google.code.gson/gson/latest/ (accessed 19 November 2026).
OpenAI (2026) *ChatGPT*. Assistance with Java code review, Part 3 implementation, and tests (accessed 19 November 2026).

## Final submission checklist
- Run Clean and Build, Test Project, and manually test registration, login, all message actions, reports, deletion and restart persistence.
- Push at least six meaningful GitHub commits for the final submission and verify GitHub Actions passes.
- Record your own narrated demonstration of the complete running program and explain the logic; submit the unlisted video and repository link as required by the PoE.
