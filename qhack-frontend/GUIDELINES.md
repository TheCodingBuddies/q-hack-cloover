# Programmierrichtlinien

In diesem Projekt gelten die folgenden Prinzipien für die Softwareentwicklung:

- **Lean Programming**: Wir konzentrieren uns auf das Wesentliche. Es wird nur Code geschrieben, der aktuell benötigt wird (YAGNI - You Ain't Gonna Need It). Überflüssige Komplexität wird vermieden, um die Wartbarkeit und Geschwindigkeit hoch zu halten.
- **MVP Fokus & Pragmatismus**: Im Zentrum steht die schnelle Lieferung der Kernfunktionalität (Minimum Viable Product). Wir wählen pragmatische Lösungen über over-engineering, ohne dabei die Codequalität oder Wartbarkeit zu opfern. Feedback-Zyklen werden kurz gehalten.
- **Kontinuierliches Refactoring**: Bei jeder Änderung wird auf Möglichkeiten zur Verbesserung der Codestruktur geachtet. Wir hinterlassen den Code sauberer, als wir ihn vorgefunden haben (Boy Scout Rule). Refactoring ist ein integraler Bestandteil des Entwicklungsprozesses, kein separater Schritt.
- **Regressionsvermeidung**: Vorhandene Features werden durch neue Änderungen oder Patches nicht beschädigt. Wir stellen durch sorgfältige Entwicklung und Tests sicher, dass die bestehende Funktionalität erhalten bleibt.
- **UI/UX Best Practices & Barrierefreiheit**: Das Frontend wird nach aktuellen Best Practices für UI/UX gestaltet, wobei der Fokus auf einer intuitiven Benutzerführung für das MVP-Kernfeature liegt. Barrierefreiheit (a11y) wird von Beginn an durch semantisches HTML berücksichtigt.
- **Typensicherheit (TS)**: Wir nutzen TypeScript konsequent, um Fehler frühzeitig zu vermeiden. Dies beschleunigt die Entwicklung im MVP, da Laufzeitfehler reduziert werden.
- **Text im Frontend soll immer auf Englisch sein**: Der Usecase für das MVP ist international orientiert und unterstützt daher ausschließlich Englisch als Sprache für die Benutzeroberfläche.