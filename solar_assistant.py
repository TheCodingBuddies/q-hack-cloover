#!/usr/bin/env python3
"""
HeliosTech Solar-Vertriebs-Assistent
=====================================
Ein CLI-Tool für Vertriebler auf dem Weg zum Kunden.

Phase 1: Vor-Qualifizierung (bekannte Eckdaten eingeben)
Phase 2: Maßgeschneiderter Fragekatalog für das Kundengespräch
Phase 3: Antworten eingeben & Angebots-Summary als Markdown erstellen
"""

import os
import sys
from datetime import datetime
from typing import Optional

# ---------------------------------------------------------------------------
# ANSI-Farben
# ---------------------------------------------------------------------------


def _supports_color() -> bool:
    if not hasattr(sys.stdout, "isatty"):
        return False
    if not sys.stdout.isatty():
        return False
    if sys.platform == "win32":
        try:
            import ctypes

            kernel32 = ctypes.windll.kernel32
            kernel32.SetConsoleMode(kernel32.GetStdHandle(-11), 7)
            return True
        except Exception:
            return False
    return True


USE_COLOR = _supports_color()


class C:
    RESET = "\033[0m" if USE_COLOR else ""
    BOLD = "\033[1m" if USE_COLOR else ""
    CYAN = "\033[96m" if USE_COLOR else ""
    YELLOW = "\033[93m" if USE_COLOR else ""
    GREEN = "\033[92m" if USE_COLOR else ""
    BLUE = "\033[94m" if USE_COLOR else ""
    RED = "\033[91m" if USE_COLOR else ""
    MAGENTA = "\033[95m" if USE_COLOR else ""
    DIM = "\033[2m" if USE_COLOR else ""
    WHITE = "\033[97m" if USE_COLOR else ""


def cyan(s: str) -> str:
    return f"{C.CYAN}{s}{C.RESET}"


def yellow(s: str) -> str:
    return f"{C.YELLOW}{s}{C.RESET}"


def green(s: str) -> str:
    return f"{C.GREEN}{s}{C.RESET}"


def blue(s: str) -> str:
    return f"{C.BLUE}{s}{C.RESET}"


def red(s: str) -> str:
    return f"{C.RED}{s}{C.RESET}"


def bold(s: str) -> str:
    return f"{C.BOLD}{s}{C.RESET}"


def dim(s: str) -> str:
    return f"{C.DIM}{s}{C.RESET}"


def magenta(s: str) -> str:
    return f"{C.MAGENTA}{s}{C.RESET}"


# ---------------------------------------------------------------------------
# Hilfsfunktionen
# ---------------------------------------------------------------------------

TERM_WIDTH = min(os.get_terminal_size().columns if sys.stdout.isatty() else 80, 100)


def line(char: str = "─", color_fn=blue) -> str:
    return color_fn(char * TERM_WIDTH)


def wrap(text: str, indent: int = 0, width: int = TERM_WIDTH) -> str:
    """Einfacher Zeilenumbruch ohne externe Deps."""
    prefix = " " * indent
    words = text.split()
    lines = []
    current = prefix
    for word in words:
        if len(current) + len(word) + 1 > width and current.strip():
            lines.append(current.rstrip())
            current = prefix + word + " "
        else:
            current += word + " "
    if current.strip():
        lines.append(current.rstrip())
    return "\n".join(lines)


def ask(prompt: str, default: str = "") -> str:
    """Eingabe mit farbigem Prompt. Leere Eingabe → default."""
    default_hint = (
        f" {dim('[Enter = ' + (default if default else 'unbekannt') + ']')}"
        if True
        else ""
    )
    try:
        val = input(f"{cyan('  ▶')} {prompt}{default_hint}: ").strip()
    except (EOFError, KeyboardInterrupt):
        print()
        return default
    return val if val else default


def ask_yn(prompt: str, default: str = "n") -> bool:
    """Ja/Nein-Abfrage."""
    hint = "(J/n)" if default.lower() == "j" else "(j/N)"
    try:
        val = input(f"{cyan('  ▶')} {prompt} {dim(hint)}: ").strip().lower()
    except (EOFError, KeyboardInterrupt):
        print()
        return default.lower() == "j"
    if val in ("j", "ja", "y", "yes", "1"):
        return True
    if val in ("n", "nein", "no", "0"):
        return False
    return default.lower() in ("j", "ja", "y", "yes")


def ask_multi(prompt: str, options: list[str]) -> list[str]:
    """
    Mehrfachauswahl aus einer Liste.
    Gibt Liste der gewählten Werte zurück.
    """
    print(f"\n{cyan('  ▶')} {prompt}")
    for i, opt in enumerate(options, 1):
        print(f"     {dim(str(i) + '.')} {opt}")
    print(
        f"  {dim('Nummern mit Komma trennen, z.B. 1,3 – oder Enter für keine Auswahl')}"
    )
    try:
        raw = input(f"{cyan('  ▶')} Auswahl: ").strip()
    except (EOFError, KeyboardInterrupt):
        print()
        return []
    if not raw:
        return []
    result = []
    for part in raw.split(","):
        part = part.strip()
        if part.isdigit():
            idx = int(part) - 1
            if 0 <= idx < len(options):
                result.append(options[idx])
    return result


def section_header(title: str) -> None:
    print()
    print(line("═"))
    print(f"{C.BOLD}{C.BLUE}  {title}{C.RESET}")
    print(line("═"))
    print()


def sub_header(title: str) -> None:
    print()
    print(f"{C.BOLD}{C.WHITE}  {title}{C.RESET}")
    print(line("─", dim))
    print()


# ---------------------------------------------------------------------------
# Phase 1: Vor-Qualifizierung
# ---------------------------------------------------------------------------


def phase1_vorqualifizierung() -> dict:
    """Fragt den Vertriebler nach bekannten Eckdaten. Alle Felder optional."""
    section_header(
        "PHASE 1 – VOR-QUALIFIZIERUNG  |  Was wissen Sie bereits über den Kunden?"
    )
    print(
        wrap(
            "Alle Felder sind optional. Einfach Enter drücken wenn unbekannt. "
            "Diese Informationen bestimmen den maßgeschneiderten Fragekatalog für Ihr Kundengespräch.",
            indent=2,
        )
    )
    print()

    data: dict = {}

    # Vertriebler-Name
    data["vertriebler"] = ask("Ihr Name (Vertriebler)")

    # Kundenname
    data["kundenname"] = ask("Kundenname / Firma")

    # Interesse
    interesse_optionen = [
        "PV (Photovoltaik)",
        "Batteriespeicher",
        "Wallbox / E-Auto-Laden",
        "Wärmepumpe",
    ]
    data["interesse"] = ask_multi(
        "Wofür hat der Kunde Interesse bekundet? (Mehrfachauswahl)", interesse_optionen
    )

    sub_header("Gebäude & Dach")

    raw_dach = ask("Ungefähre Dachgröße (m²) – nutzbare Fläche")
    data["dachflaeche"] = _parse_float(raw_dach)

    ausrichtung_optionen = [
        "S (Süd)",
        "SW (Südwest)",
        "SO (Südost)",
        "W (West)",
        "O (Ost)",
        "N (Nord)",
        "unbekannt",
    ]
    print(f"\n{cyan('  ▶')} Ungefähre Dachausrichtung (Hauptfläche):")
    for i, opt in enumerate(ausrichtung_optionen, 1):
        print(f"     {dim(str(i) + '.')} {opt}")
    raw_ausrichtung = ask("Nummer eingeben oder Kürzel (S/SW/SO/W/O/N)")
    data["ausrichtung"] = _parse_ausrichtung(raw_ausrichtung, ausrichtung_optionen)

    raw_bj = ask("Baujahr des Gebäudes (z.B. 1978)")
    data["baujahr"] = _parse_int(raw_bj)

    raw_flaeche = ask("Wohnfläche in m² (falls bekannt)")
    data["wohnflaeche"] = _parse_float(raw_flaeche)

    raw_personen = ask("Anzahl Personen im Haushalt")
    data["personen"] = _parse_int(raw_personen)

    sub_header("Energie & Verbrauch")

    raw_verbrauch = ask("Jahresstromverbrauch (kWh/Jahr, z.B. 5000)")
    data["jahresverbrauch"] = _parse_float(raw_verbrauch)

    heizung_optionen = [
        "Gas",
        "Öl",
        "Wärmepumpe (vorhanden)",
        "Pellets",
        "Fernwärme",
        "Strom (Nachtspeicher)",
        "unbekannt",
    ]
    print(f"\n{cyan('  ▶')} Aktuelles Heizsystem:")
    for i, opt in enumerate(heizung_optionen, 1):
        print(f"     {dim(str(i) + '.')} {opt}")
    raw_heizung = ask("Nummer oder Kurzbezeichnung")
    data["heizung"] = _parse_heizung(raw_heizung, heizung_optionen)

    ev_optionen = ["ja, vorhanden", "geplant / in Kürze", "nein", "unbekannt"]
    print(f"\n{cyan('  ▶')} Elektroauto vorhanden oder geplant?")
    for i, opt in enumerate(ev_optionen, 1):
        print(f"     {dim(str(i) + '.')} {opt}")
    raw_ev = ask("Nummer oder Kurzantwort")
    data["elektroauto"] = _parse_ev(raw_ev, ev_optionen)

    print()
    print(line())
    print(f"  {green('✔')}  Vor-Qualifizierung abgeschlossen.")
    print(line())

    return data


def _parse_float(val: str) -> Optional[float]:
    try:
        return float(val.replace(",", ".").strip())
    except (ValueError, AttributeError):
        return None


def _parse_int(val: str) -> Optional[int]:
    try:
        return int(val.strip())
    except (ValueError, AttributeError):
        return None


def _parse_ausrichtung(raw: str, options: list[str]) -> str:
    if not raw:
        return "unbekannt"
    raw = raw.strip().upper()
    if raw.isdigit():
        idx = int(raw) - 1
        if 0 <= idx < len(options):
            return options[idx].split(" ")[0]
        return "unbekannt"
    mapping = {"S": "S", "SW": "SW", "SO": "SO", "W": "W", "O": "O", "N": "N"}
    return mapping.get(raw, "unbekannt")


def _parse_heizung(raw: str, options: list[str]) -> str:
    if not raw:
        return "unbekannt"
    if raw.isdigit():
        idx = int(raw) - 1
        if 0 <= idx < len(options):
            return options[idx].split(" ")[0]
        return "unbekannt"
    raw_lower = raw.lower()
    for opt in options:
        if opt.lower().startswith(raw_lower[:3]):
            return opt.split(" ")[0]
    return raw if raw else "unbekannt"


def _parse_ev(raw: str, options: list[str]) -> str:
    if not raw:
        return "unbekannt"
    if raw.isdigit():
        idx = int(raw) - 1
        if 0 <= idx < len(options):
            return options[idx]
        return "unbekannt"
    raw_lower = raw.lower()
    if raw_lower in ("ja", "j", "yes", "1"):
        return "ja, vorhanden"
    if raw_lower in ("nein", "n", "no", "0"):
        return "nein"
    if "geplant" in raw_lower or "bald" in raw_lower:
        return "geplant / in Kürze"
    return "unbekannt"


def _hat_interesse(data: dict, keyword: str) -> bool:
    interesse = data.get("interesse", [])
    keyword_lower = keyword.lower()
    return any(keyword_lower in i.lower() for i in interesse)


def _hat_wallbox(data: dict) -> bool:
    return _hat_interesse(data, "wallbox") or data.get("elektroauto", "unbekannt") in (
        "ja, vorhanden",
        "geplant / in Kürze",
    )


def _hat_waermepumpe_interesse(data: dict) -> bool:
    return _hat_interesse(data, "wärmepumpe")


def _hat_fossil_heizung(data: dict) -> bool:
    h = data.get("heizung", "unbekannt").lower()
    return any(x in h for x in ("gas", "öl", "ol"))


# ---------------------------------------------------------------------------
# Phase 2: Fragekatalog
# ---------------------------------------------------------------------------

PRIO_WICHTIG = "WICHTIG"
PRIO_HILFREICH = "HILFREICH"


def _frage(nummer: int, prio: str, frage: str, begruendung: str) -> None:
    """Gibt eine einzelne Frage aus dem Fragekatalog aus."""
    prio_fmt = (
        f"{C.BOLD}{C.RED}[{prio}]{C.RESET}"
        if prio == PRIO_WICHTIG
        else f"{C.BOLD}{C.YELLOW}[{prio}]{C.RESET}"
    )
    num_fmt = f"{C.BOLD}{C.WHITE}{nummer:2d}.{C.RESET}"
    print(f"  {num_fmt} {prio_fmt}")
    print(wrap(frage, indent=6))
    print(f"      {dim('→ ' + begruendung)}")
    print()


def phase2_fragekatalog(data: dict) -> None:
    """Generiert und gibt den maßgeschneiderten Fragekatalog aus."""
    section_header("PHASE 2 – FRAGEKATALOG  |  Fragen für Ihr Kundengespräch")

    hat_wb = _hat_wallbox(data)
    hat_wp = _hat_waermepumpe_interesse(data)
    hat_fossil = _hat_fossil_heizung(data)
    baujahr = data.get("baujahr")
    alte_heizung = baujahr is not None and baujahr < 1980
    ausrichtung_unbekannt = data.get("ausrichtung", "unbekannt") in (
        "unbekannt",
        "",
        None,
    )
    verbrauch_unbekannt = data.get("jahresverbrauch") is None

    katalog: list[tuple[str, str, str]] = []  # (prio, frage, begruendung)

    # ── Dach ────────────────────────────────────────────────────────────────
    sub_header("🏠  DACH & GEBÄUDE")

    if ausrichtung_unbekannt:
        katalog.append(
            (
                PRIO_WICHTIG,
                "In welche Himmelsrichtung zeigt die Hauptdachfläche, auf der die Anlage montiert werden soll? "
                "Schauen Sie bitte kurz aus dem Dachfenster oder einem Fenster auf der Südseite – wo ist da die Sonne am Mittag?",
                "Ausrichtung ist entscheidend für den Jahresertrag – fehlende Info blockiert die Auslegung",
            )
        )
        katalog.append(
            (
                PRIO_WICHTIG,
                "Gibt es noch eine zweite nutzbare Dachfläche (z.B. Ost-West-Aufteilung)? "
                "Falls ja: Wie groß ist diese in etwa und in welche Richtung zeigt sie?",
                "Ost-West-Belegung kann höheren Eigenverbrauch ermöglichen",
            )
        )
    else:
        katalog.append(
            (
                PRIO_HILFREICH,
                "Gibt es neben der bekannten Hauptfläche ("
                + data.get("ausrichtung", "")
                + ") noch weitere nutzbare Dachflächen, "
                "z.B. eine Ost- oder Westseite?",
                "Zusatzflächen für mehr Ertrag oder bessere Eigenverbrauchsoptimierung",
            )
        )

    if data.get("dachflaeche") is None:
        katalog.append(
            (
                PRIO_WICHTIG,
                "Wie groß ist die nutzbare Dachfläche in etwa? Können Sie mir bitte die Dachbreite und -länge schätzen? "
                "(Faustregel: Hausbreite × Dachschrägen-Länge). Oder: Wie viele Meter ist das Haus breit und wie tief?",
                "Dachfläche bestimmt die maximal installierbare Modulanzahl und damit die kWp-Grenze",
            )
        )

    katalog.append(
        (
            PRIO_WICHTIG,
            "Wie ist die ungefähre Dachneigung? Ist es eher flach (unter 20°), normal geneigt (30–45°) oder steil? "
            "Ein Flachdach mit Aufständerung ist natürlich auch möglich.",
            "Neigung beeinflusst Ertrag und Montageaufwand; Flachdach = andere Preiskalkulation",
        )
    )

    katalog.append(
        (
            PRIO_WICHTIG,
            "Gibt es Hindernisse auf dem Dach, die Schatten werfen? Zum Beispiel: Kamine, Dachfenster, Lüftungsanlagen, "
            "Sat-Schüsseln? Wo befinden sich diese genau?",
            "Verschattung reduziert Ertrag überproportional – Planung muss das berücksichtigen",
        )
    )

    katalog.append(
        (
            PRIO_WICHTIG,
            "Werfen Nachbarhäuser, Bäume oder andere Objekte in der Umgebung Schatten auf das Dach – besonders morgens, "
            "abends oder im Winter? Wenn ja: zu welcher Tageszeit und auf welchen Teil des Dachs?",
            "Externe Verschattung ist einer der häufigsten unterschätzten Ertragsminderer",
        )
    )

    katalog.append(
        (
            PRIO_HILFREICH,
            "Wie alt ist die Dacheindeckung und in welchem Zustand ist sie? Wurde das Dach in den letzten 10–15 Jahren "
            "neu eingedeckt oder saniert?",
            "Dachzustand entscheidet, ob vor Montage eine Sanierung nötig ist – das beeinflusst Timing und Budget",
        )
    )

    katalog.append(
        (
            PRIO_HILFREICH,
            "Welches Dachmaterial haben Sie? (Ziegel, Blech, Bitumen, Trapezblech, Betondachstein, Schiefer?)",
            "Verschiedene Materialien brauchen unterschiedliche Montagesysteme, beeinflusst Zusatzkosten",
        )
    )

    for i, (prio, frage, begruendung) in enumerate(katalog, 1):
        _frage(i, prio, frage, begruendung)
    katalog_offset = len(katalog)
    katalog = []

    # ── Technik & Anschluss ─────────────────────────────────────────────────
    sub_header("⚡  TECHNIK & HAUSANSCHLUSS")

    katalog.append(
        (
            PRIO_WICHTIG,
            "Gibt es bei Ihnen einen Technikraum, Heizungskeller, Abstellraum oder Ähnliches, wo der Wechselrichter "
            "und ein eventueller Batteriespeicher installiert werden könnten? Ist dieser Raum trocken und frostfrei?",
            "Wechselrichter und Speicher brauchen geeigneten Stellplatz – frühzeitig klären verhindert spätere Überraschungen",
        )
    )

    katalog.append(
        (
            PRIO_WICHTIG,
            "Wie groß ist Ihre Hauptsicherung / Hausanschlussleistung? Das steht meist auf dem Zählerkasten, "
            "oft '3 × 25A' oder '3 × 63A'. Darf ich kurz einen Blick in den Zählerschrank werfen?",
            "Hausanschlussleistung begrenzt die einspeisbare Leistung – wichtig für Netzbetreiber-Anmeldung",
        )
    )

    katalog.append(
        (
            PRIO_HILFREICH,
            "Haben Sie einen digitalen Zweirichtungszähler oder noch einen alten Ferraris-Zähler? "
            "Haben Sie bereits Kontakt mit Ihrem Netzbetreiber gehabt?",
            "Zählertausch ist Pflicht bei PV – Netzbetreiber muss informiert werden",
        )
    )

    katalog.append(
        (
            PRIO_HILFREICH,
            "Wie viele Phasen hat Ihr Stromanschluss? Einphasig (230V) oder dreiphasig (400V)? "
            "(Üblicherweise dreiphasig in deutschen Haushalten, aber kurz bestätigen lassen)",
            "Einphasiger Anschluss begrenzt Einspeiseleistung und ist selten – aber es gibt ihn",
        )
    )

    for i, (prio, frage, begruendung) in enumerate(katalog, katalog_offset + 1):
        _frage(i, prio, frage, begruendung)
    katalog_offset += len(katalog)
    katalog = []

    # ── Verbrauch ───────────────────────────────────────────────────────────
    if verbrauch_unbekannt:
        sub_header("📊  STROMVERBRAUCH")
        katalog.append(
            (
                PRIO_WICHTIG,
                "Haben Sie zufällig Ihre letzte Jahresstromrechnung griffbereit oder kennen Sie Ihren ungefähren "
                "Jahresstromverbrauch in kWh? Das steht auf der Jahresabrechnung Ihres Versorgers. "
                "Alternativ: Was zahlen Sie im Jahr in etwa an Strom?",
                "Jahresverbrauch ist die wichtigste Kenngröße für die optimale Anlagendimensionierung",
            )
        )

        katalog.append(
            (
                PRIO_HILFREICH,
                "Planen Sie in nächster Zeit größere Verbraucher anzuschaffen? Zum Beispiel Klimaanlage, Pool, "
                "Infrarotsauna oder ähnliches?",
                "Zukünftige Großverbraucher sollten bei der Dimensionierung berücksichtigt werden",
            )
        )

        for i, (prio, frage, begruendung) in enumerate(katalog, katalog_offset + 1):
            _frage(i, prio, frage, begruendung)
        katalog_offset += len(katalog)
        katalog = []

    # ── Wallbox ─────────────────────────────────────────────────────────────
    if hat_wb:
        sub_header("🚗  WALLBOX & ELEKTROMOBILITÄT")

        katalog.append(
            (
                PRIO_WICHTIG,
                "Wie viele Elektro- oder Hybridfahrzeuge haben Sie oder planen Sie? "
                "Laden alle Fahrzeuge zuhause?",
                "Anzahl der Fahrzeuge bestimmt ob ein oder mehrere Ladepunkte benötigt werden",
            )
        )

        katalog.append(
            (
                PRIO_WICHTIG,
                "Wie viele Kilometer fahren Sie im Schnitt pro Tag bzw. pro Woche? "
                "Und: Laden Sie das Fahrzeug hauptsächlich nachts oder tagsüber?",
                "Ladeverhalten beeinflusst Speicherdimensionierung und Eigenverbrauchsoptimierung",
            )
        )

        katalog.append(
            (
                PRIO_WICHTIG,
                "Haben Sie eine Garage oder einen Carport? Wo genau parken Sie das Fahrzeug – "
                "direkt am Haus, in der Einfahrt, oder weiter entfernt? "
                "Wie lang wäre ungefähr der Kabelweg vom Zählerschrank zur Ladeposition?",
                "Kabelweg bestimmt Installationsaufwand und Leitungsquerschnitt – großer Kostenfaktor",
            )
        )

        katalog.append(
            (
                PRIO_HILFREICH,
                "Benötigen Sie 22 kW Ladeleistung (3-phasig voll) oder reichen 11 kW? "
                "Haben Sie einen Dienstwagen oder ein Fahrzeug, das 22 kW unterstützt und Sie möchten schnell laden?",
                "22 kW benötigt Anmeldung beim Netzbetreiber und ist selten nötig – Standard sind 11 kW",
            )
        )

        katalog.append(
            (
                PRIO_HILFREICH,
                "Möchten Sie die Wallbox mit PV-Überschussladen steuern können – also das Auto automatisch "
                "laden wenn die Sonne scheint? Haben Sie Interesse an einer App-Steuerung?",
                "Smart-Charging ist ein starkes Verkaufsargument – erhöht Eigenverbrauch erheblich",
            )
        )

        for i, (prio, frage, begruendung) in enumerate(katalog, katalog_offset + 1):
            _frage(i, prio, frage, begruendung)
        katalog_offset += len(katalog)
        katalog = []

    # ── Wärmepumpe / Heizung ────────────────────────────────────────────────
    if hat_wp or hat_fossil:
        sub_header("🌡️  HEIZUNG & WÄRMEPUMPE")

        if hat_wp:
            katalog.append(
                (
                    PRIO_WICHTIG,
                    "Ist eine Wärmepumpe als Ersatz für Ihre aktuelle Heizung gedacht oder als Ergänzung? "
                    "Wann müsste oder möchten Sie die aktuelle Heizung ersetzen?",
                    "Dringlichkeit des Heizungsersatzes ist entscheidend für Verkaufszeitraum und Förderantragstellung",
                )
            )

        if data.get("wohnflaeche") is None:
            katalog.append(
                (
                    PRIO_WICHTIG,
                    "Wie groß ist Ihre Wohnfläche in etwa? Alle Etagen zusammen?",
                    "Wohnfläche ist Hauptkriterium für die Dimensionierung der Wärmepumpenleistung",
                )
            )

        katalog.append(
            (
                PRIO_WICHTIG,
                "Haben Sie in Ihrem Haus Fußbodenheizung, Heizkörper oder beides? "
                "Falls Heizkörper: Sind das alte große Gussheizkörper oder modernere flache Heizkörper?",
                "Fußbodenheizung = ideal für Wärmepumpe; Heizkörper brauchen ggf. Vorlauftemperatur-Check",
            )
        )

        katalog.append(
            (
                PRIO_WICHTIG,
                "Wie hoch ist üblicherweise Ihre Vorlauftemperatur im Winter? Wissen Sie das, oder: "
                "bei welcher Außentemperatur ist Ihnen im Haus kalt? Dreht die Heizung dann richtig auf?",
                "Hohe Vorlauftemperatur > 55°C macht Wärmepumpe ineffizient – muss vorab geklärt werden",
            )
        )

        katalog.append(
            (
                PRIO_HILFREICH,
                "Haben Sie einen Warmwasserspeicher? Wie groß ist der ungefähr (Liter)? "
                "Wird Warmwasser über die Heizung oder elektrisch erzeugt?",
                "Warmwasserbereitung über Wärmepumpe möglich – erhöht Effizienz des Gesamtsystems",
            )
        )

        if alte_heizung or baujahr is None:
            katalog.append(
                (
                    PRIO_WICHTIG,
                    "Wie gut ist das Haus gedämmt? Wurde in den letzten Jahren etwas an der Fassade, "
                    "dem Dach oder den Fenstern gemacht? Wissen Sie ob Kellerdecke oder Dach gedämmt sind?",
                    "Schlechte Dämmung erhöht nötige Wärmepumpenleistung und senkt Effizienz – wichtig für Planung und Förderung",
                )
            )

        katalog.append(
            (
                PRIO_HILFREICH,
                "Gibt es bei Ihnen ausreichend Platz für eine Außeneinheit (ca. 1×1 m Grundfläche) "
                "an einer Hauswand oder im Garten? Gibt es Einschränkungen durch Nachbarn oder Denkmalschutz?",
                "Aufstellort Außeneinheit muss früh geklärt werden – Mindestabstände zur Grundstücksgrenze beachten",
            )
        )

        for i, (prio, frage, begruendung) in enumerate(katalog, katalog_offset + 1):
            _frage(i, prio, frage, begruendung)
        katalog_offset += len(katalog)
        katalog = []

    # ── Speicher ────────────────────────────────────────────────────────────
    if _hat_interesse(data, "speicher") or _hat_interesse(data, "batterie"):
        sub_header("🔋  BATTERIESPEICHER")

        katalog.append(
            (
                PRIO_WICHTIG,
                "Wie wichtig ist Ihnen die Unabhängigkeit vom Stromnetz? Haben Sie das Ziel, "
                "möglichst viel selbst erzeugten Strom auch selbst zu verbrauchen?",
                "Motivation hinter Speicherwunsch verstehen – Autarkie vs. Wirtschaftlichkeit beeinflusst Größenempfehlung",
            )
        )

        katalog.append(
            (
                PRIO_HILFREICH,
                "Hatten Sie in der Vergangenheit Stromausfälle? Wäre eine Notstromfunktion für Sie interessant – "
                "also dass der Strom weiter fließt auch wenn das Netz ausfällt?",
                "Blackout-Schutz / Backup-Funktion ist Zusatzmerkmal – erhöht Verkaufswert und rechtfertigt größeren Speicher",
            )
        )

        for i, (prio, frage, begruendung) in enumerate(katalog, katalog_offset + 1):
            _frage(i, prio, frage, begruendung)
        katalog_offset += len(katalog)
        katalog = []

    # ── Allgemein / Abschluss ───────────────────────────────────────────────
    sub_header("💰  BUDGET & ENTSCHEIDUNG")
    katalog.append(
        (
            PRIO_WICHTIG,
            "Haben Sie sich schon ein ungefähres Budget überlegt, welches Sie für die Investition einsetzen möchten? "
            "Oder sollen wir einfach die optimale Lösung zusammenstellen und dann gemeinsam schauen?",
            "Budget-Rahmen frühzeitig kennen verhindert, ein Angebot zu erstellen das nicht passt",
        )
    )

    katalog.append(
        (
            PRIO_WICHTIG,
            "Wie ist Ihr Zeitrahmen – wann möchten Sie die Anlage idealerweise in Betrieb haben? "
            "Gibt es einen bestimmten Grund (auslaufende Förderung, Heizungsaustausch-Pflicht, neues Fahrzeug)?",
            "Zeitdruck erzeugt Entscheidungsdynamik – wichtig für Priorisierung im Verkaufsprozess",
        )
    )

    katalog.append(
        (
            PRIO_HILFREICH,
            "Haben Sie schon andere Angebote eingeholt oder sich bei anderen Anbietern informiert? "
            "(Keine Wertung – ich frage nur um zu verstehen wo Sie gerade stehen)",
            "Wettbewerbssituation kennen – ermöglicht gezielte Differenzierung",
        )
    )

    katalog.append(
        (
            PRIO_HILFREICH,
            "Sind Sie Eigentümer des Hauses? Gibt es Mitentscheider (Partner, Miteigentümer), "
            "die heute nicht dabei sind aber für die finale Entscheidung wichtig sind?",
            "Entscheidungsstruktur kennen – verhindert dass ein Angebot ins Leere läuft",
        )
    )

    for i, (prio, frage, begruendung) in enumerate(katalog, katalog_offset + 1):
        _frage(i, prio, frage, begruendung)

    print(line())
    print()
    print(
        f"  {yellow('💡')}  {bold('Tipp:')} {C.YELLOW}Wichtige Fragen zuerst stellen – den Katalog als Checkliste nutzen.{C.RESET}"
    )
    print(
        f"  {yellow('💡')}  Notieren Sie die Antworten direkt, damit Sie diese danach eingeben können."
    )
    print()
    print(line())


# ---------------------------------------------------------------------------
# Phase 3: Antworten eingeben & Summary erstellen
# ---------------------------------------------------------------------------


def phase3_antworten_eingeben(data: dict) -> dict:
    """Vertiefende Antworten aus dem Kundengespräch eingeben."""
    section_header("PHASE 3 – ANTWORTEN EINGEBEN  |  Ergebnisse aus dem Kundengespräch")
    print(
        wrap(
            "Geben Sie nun die Antworten des Kunden ein. Unbekannte Felder einfach mit Enter überspringen.",
            indent=2,
        )
    )
    print()

    answers: dict = dict(data)  # Kopie der Phase-1-Daten

    sub_header("Dach (Präzisierung)")

    if data.get("dachflaeche") is None:
        raw = ask("Dachfläche nutzbar (m²) – jetzt bekannt?")
        answers["dachflaeche"] = _parse_float(raw)

    if data.get("ausrichtung", "unbekannt") in ("unbekannt", "", None):
        raw = ask("Ausrichtung Hauptfläche (S/SW/SO/W/O/N)")
        if raw:
            answers["ausrichtung"] = raw.strip().upper()

    raw_neigung = ask("Dachneigung in Grad (z.B. 30) oder Typ (flach/mittel/steil)")
    answers["dachneigung"] = raw_neigung if raw_neigung else "unbekannt"

    print(f"  {dim('Hindernisse auf dem Dach (Kamine, Dachfenster etc.)?')}")
    raw_hind = ask("Hindernisse vorhanden? (ja/nein/Details)")
    answers["dach_hindernisse"] = raw_hind if raw_hind else "keine"

    print(f"  {dim('Verschattung durch Nachbarn, Bäume?')}")
    raw_schatten = ask("Externe Verschattung? (ja/nein/Details)")
    answers["verschattung"] = raw_schatten if raw_schatten else "keine"

    raw_dachzustand = ask("Dachzustand / letztes Sanierungsjahr (z.B. gut / 2018)")
    answers["dachzustand"] = raw_dachzustand if raw_dachzustand else "unbekannt"

    sub_header("Technik & Anschluss")

    raw_technikraum = ask(
        "Technikraum / Stellplatz für WR+Speicher vorhanden? (ja/nein/Details)"
    )
    answers["technikraum"] = raw_technikraum if raw_technikraum else "unbekannt"

    raw_sicherung = ask("Hauptsicherung / Hausanschluss (z.B. 3×25A, 3×63A)")
    answers["hauptsicherung"] = raw_sicherung if raw_sicherung else "unbekannt"

    sub_header("Verbrauch & Budget")

    if data.get("jahresverbrauch") is None:
        raw = ask("Jahresstromverbrauch kWh (jetzt bekannt?)")
        answers["jahresverbrauch"] = _parse_float(raw)

    if data.get("wohnflaeche") is None:
        raw = ask("Wohnfläche m² (jetzt bekannt?)")
        answers["wohnflaeche"] = _parse_float(raw)

    raw_budget = ask(
        "Budget des Kunden (z.B. '20.000 €', 'flexibel', 'unter 30.000 €')"
    )
    answers["budget"] = raw_budget if raw_budget else "nicht genannt"

    raw_zeitrahmen = ask(
        "Gewünschter Zeitrahmen (z.B. 'bis Sommer', 'so schnell wie möglich', 'nächstes Jahr')"
    )
    answers["zeitrahmen"] = raw_zeitrahmen if raw_zeitrahmen else "nicht genannt"

    raw_entscheider = ask(
        "Entscheider vollständig anwesend? (ja / nein – Partner fehlt / etc.)"
    )
    answers["entscheider"] = raw_entscheider if raw_entscheider else "unbekannt"

    raw_mitbewerber = ask("Andere Angebote eingeholt? (nein / ja – von wem?)")
    answers["mitbewerber"] = raw_mitbewerber if raw_mitbewerber else "nein"

    # Wallbox-Details
    if _hat_wallbox(data):
        sub_header("Wallbox-Details")
        raw_anz_fz = ask("Anzahl Fahrzeuge / Ladepunkte")
        answers["anzahl_fahrzeuge"] = _parse_int(raw_anz_fz) if raw_anz_fz else None

        raw_km = ask("Tägliche Fahrtstrecke in km (ca.)")
        answers["tageskilometer"] = _parse_float(raw_km)

        raw_kabelweg = ask("Kabelweg vom Zähler zur Ladeposition (m)")
        answers["kabelweg_wallbox"] = raw_kabelweg if raw_kabelweg else "unbekannt"

        raw_22kw = ask("22 kW explizit gewünscht? (ja/nein)")
        answers["wallbox_22kw"] = (
            raw_22kw.lower() in ("ja", "j", "yes") if raw_22kw else False
        )

        raw_smart = ask("Smart-Charging / PV-Überschussladen gewünscht? (ja/nein)")
        answers["wallbox_smart"] = (
            raw_smart.lower() not in ("nein", "n", "no") if raw_smart else True
        )

    # Wärmepumpen-Details
    if _hat_waermepumpe_interesse(data) or _hat_fossil_heizung(data):
        sub_header("Heizungs-Details")
        raw_fbh = ask("Fußbodenheizung vorhanden? (ja/nein/teilweise)")
        answers["fussbodenheizung"] = raw_fbh if raw_fbh else "unbekannt"

        raw_vl = ask("Vorlauftemperatur (z.B. 55°C, hoch, niedrig, unbekannt)")
        answers["vorlauftemperatur"] = raw_vl if raw_vl else "unbekannt"

        raw_daemmung = ask(
            "Dämmzustand / Sanierungsstand (gut/mittel/schlecht/saniert JJJJ)"
        )
        answers["daemmzustand"] = raw_daemmung if raw_daemmung else "unbekannt"

        raw_ausseneinheit = ask("Platz für Außeneinheit vorhanden? (ja/nein/Details)")
        answers["ausseneinheit_platz"] = (
            raw_ausseneinheit if raw_ausseneinheit else "unbekannt"
        )

    sub_header("Notizen")
    raw_notizen = ask("Sonstige Anmerkungen / Besonderheiten (freitext)")
    answers["notizen"] = raw_notizen if raw_notizen else ""

    return answers


# ---------------------------------------------------------------------------
# Produktauswahl-Logik
# ---------------------------------------------------------------------------

PV_PAKETE = [
    {"name": "HeliosTech SunStart 5 kWp", "kwp": 5, "preis": 8900},
    {"name": "HeliosTech SunPro 8 kWp", "kwp": 8, "preis": 13200},
    {"name": "HeliosTech SunMax 10 kWp", "kwp": 10, "preis": 15800},
    {"name": "HeliosTech SunMax 12 kWp", "kwp": 12, "preis": 18400},
    {"name": "HeliosTech SunMax 15 kWp", "kwp": 15, "preis": 22500},
]

SPEICHER_PAKETE = [
    {"name": "HeliosTech VoltStore 5 kWh", "kwh": 5, "preis": 4200},
    {"name": "HeliosTech VoltStore 8 kWh", "kwh": 8, "preis": 6400},
    {"name": "HeliosTech VoltStore 12 kWh", "kwh": 12, "preis": 8900},
    {"name": "HeliosTech VoltStore 16 kWh", "kwh": 16, "preis": 11200},
]

WALLBOX_PAKETE = [
    {
        "name": "HeliosTech ChargePoint 11 kW",
        "kw": 11,
        "smart": False,
        "preis": 1490,
        "beschreibung": "1-phasig steuerbar, einfache Installation",
    },
    {
        "name": "HeliosTech ChargePoint Pro 11 kW",
        "kw": 11,
        "smart": True,
        "preis": 1890,
        "beschreibung": "Smart, App-Steuerung, PV-Überschussladen",
    },
    {
        "name": "HeliosTech ChargePoint Max 22 kW",
        "kw": 22,
        "smart": True,
        "preis": 2490,
        "beschreibung": "22 kW, smart, App, Netzbetreiber-Anmeldung erforderlich",
    },
]

WAERMEPUMPEN_PAKETE = [
    {
        "name": "HeliosTech HeatWave 8 kW",
        "kw": 8,
        "preis": 14500,
        "beschreibung": "Luft-Wasser, geeignet bis 100 m² Altbau / 150 m² Neubau, inkl. Installation",
    },
    {
        "name": "HeliosTech HeatWave 12 kW",
        "kw": 12,
        "preis": 17800,
        "beschreibung": "Luft-Wasser, geeignet bis 150 m² Altbau / 220 m² Neubau, inkl. Installation",
    },
    {
        "name": "HeliosTech HeatWave Pro 16 kW",
        "kw": 16,
        "preis": 22400,
        "beschreibung": "Luft-Wasser, geeignet bis 220 m² Altbau / 300 m² Neubau, inkl. Installation",
    },
]


def _empfehle_pv(answers: dict) -> dict:
    ausrichtung = answers.get("ausrichtung", "unbekannt")
    nord = isinstance(ausrichtung, str) and ausrichtung.upper() in ("N", "NORD")
    dachflaeche = answers.get("dachflaeche")
    verbrauch = answers.get("jahresverbrauch")
    hinweise = []

    kwp_optionen = []

    if dachflaeche:
        kwp_dach = dachflaeche * 0.18
        if nord:
            kwp_dach *= 0.80
            hinweise.append(
                "⚠️ Nord-Ausrichtung: Ertrag ca. 20% geringer, Anlage entsprechend kleiner dimensioniert"
            )
        kwp_optionen.append(kwp_dach)

    if verbrauch:
        kwp_verbrauch = verbrauch / 1000
        kwp_optionen.append(kwp_verbrauch)

    if not kwp_optionen:
        hinweise.append(
            "ℹ️ Keine Dachfläche/Verbrauchsdaten – Standardempfehlung SunPro 8 kWp"
        )
        paket = next(p for p in PV_PAKETE if p["kwp"] == 8)
        return {"paket": paket, "hinweise": hinweise, "berechnet_kwp": None}

    kwp_ziel = max(kwp_optionen)

    # Nächstgrößeres Paket wählen
    paket = PV_PAKETE[-1]  # default: größtes
    for p in PV_PAKETE:
        if p["kwp"] >= kwp_ziel:
            paket = p
            break

    hinweise.append(
        f"Berechnung: max({', '.join(f'{v:.1f}' for v in kwp_optionen)}) kWp → {paket['kwp']} kWp Paket gewählt"
    )
    return {"paket": paket, "hinweise": hinweise, "berechnet_kwp": kwp_ziel}


def _empfehle_speicher(answers: dict) -> Optional[dict]:
    verbrauch = answers.get("jahresverbrauch")
    hat_wb = _hat_wallbox(answers)
    hat_wp = _hat_waermepumpe_interesse(answers)
    hat_speicher_interesse = _hat_interesse(answers, "speicher") or _hat_interesse(
        answers, "batterie"
    )

    hinweise = []
    kwh_ziel = 5.0  # Minimum

    if verbrauch:
        # Faustregel: Verbrauch × 0.25 / 365 × 2
        kwh_berechnet = verbrauch * 0.25 / 365 * 2
        kwh_ziel = max(kwh_berechnet, 5.0)
        hinweise.append(
            f"Berechnung: {verbrauch:.0f} kWh × 0.25 / 365 × 2 = {kwh_berechnet:.1f} kWh → auf mind. 5 kWh angehoben"
        )

    empfehlen = hat_speicher_interesse
    if verbrauch and verbrauch > 4000:
        empfehlen = True
        hinweise.append(
            f"**Speicher empfehlenswert – Jahresverbrauch > 4.000 kWh ({verbrauch:.0f} kWh)**"
        )
    if hat_wb:
        empfehlen = True
        kwh_ziel = max(kwh_ziel, 8.0)
        hinweise.append(
            "**Speicher empfehlenswert – Wallbox vorhanden → Speicher für PV-Überschussladen sinnvoll**"
        )
    if hat_wp:
        empfehlen = True
        kwh_ziel = max(kwh_ziel, 10.0)
        hinweise.append(
            "**Speicher empfehlenswert – Wärmepumpe + Speicher = maximale Autarkie**"
        )

    if not empfehlen:
        return None

    # Passendes Paket wählen
    paket = SPEICHER_PAKETE[-1]
    for p in SPEICHER_PAKETE:
        if p["kwh"] >= kwh_ziel:
            paket = p
            break

    hinweise.append(
        "💡 Tipp: Einspeisevergütung ist aktuell gering – Eigenverbrauch durch Speicher deutlich wirtschaftlicher"
    )
    return {"paket": paket, "hinweise": hinweise, "berechnet_kwh": kwh_ziel}


def _empfehle_wallbox(answers: dict) -> Optional[dict]:
    if not _hat_wallbox(answers):
        return None
    hinweise = []
    will_22kw = answers.get("wallbox_22kw", False)
    will_smart = answers.get("wallbox_smart", True)

    if will_22kw:
        paket = next(p for p in WALLBOX_PAKETE if p["kw"] == 22)
        hinweise.append("⚠️ 22 kW: Anmeldung beim Netzbetreiber erforderlich (§ 19 NAV)")
    elif will_smart or _hat_interesse(answers, "pv"):
        paket = next(p for p in WALLBOX_PAKETE if "Pro" in p["name"])
        hinweise.append(
            "✅ ChargePoint Pro empfohlen – PV-Überschussladen maximiert Eigenverbrauch"
        )
    else:
        paket = next(
            p for p in WALLBOX_PAKETE if "Pro" in p["name"]
        )  # Standard auch Pro
        hinweise.append(
            "✅ ChargePoint Pro als Standard – Smart-Charging jederzeit nachrüstbar"
        )

    return {"paket": paket, "hinweise": hinweise}


def _empfehle_waermepumpe(answers: dict) -> Optional[dict]:
    if not (_hat_waermepumpe_interesse(answers) or _hat_fossil_heizung(answers)):
        return None
    if not _hat_waermepumpe_interesse(answers):
        return None  # Nur Hinweis wenn kein explizites Interesse

    wohnflaeche = answers.get("wohnflaeche")
    hinweise = []

    if wohnflaeche is None:
        paket = next(p for p in WAERMEPUMPEN_PAKETE if p["kw"] == 12)
        hinweise.append(
            "ℹ️ Wohnfläche unbekannt – HeatWave 12 kW als Standardempfehlung"
        )
    elif wohnflaeche < 120:
        paket = next(p for p in WAERMEPUMPEN_PAKETE if p["kw"] == 8)
    elif wohnflaeche <= 180:
        paket = next(p for p in WAERMEPUMPEN_PAKETE if p["kw"] == 12)
    else:
        paket = next(p for p in WAERMEPUMPEN_PAKETE if p["kw"] == 16)

    vl = answers.get("vorlauftemperatur", "").lower()
    if any(x in vl for x in ("hoch", "70", "75", "80")):
        hinweise.append(
            "⚠️ Hohe Vorlauftemperatur – Effizienz der WP prüfen, ggf. Heizkörpertausch empfehlen"
        )

    daemmung = answers.get("daemmzustand", "").lower()
    if any(x in daemmung for x in ("schlecht", "kaum", "keine")):
        hinweise.append(
            "⚠️ Schlechter Dämmzustand – vor WP-Einbau Sanierungsberatung empfehlen, Förderung BEG-Kombination prüfen"
        )

    hinweise.append(
        "💰 BEG-Förderung: bis zu 70% der Investitionskosten möglich (Grundförderung + Boni)"
    )
    return {"paket": paket, "hinweise": hinweise}


def _berechne_empfehlungen(answers: dict) -> dict:
    return {
        "pv": _empfehle_pv(answers),
        "speicher": _empfehle_speicher(answers),
        "wallbox": _empfehle_wallbox(answers),
        "waermepumpe": _empfehle_waermepumpe(answers),
    }


# ---------------------------------------------------------------------------
# Markdown-Generierung
# ---------------------------------------------------------------------------


def _foerderhinweise(empfehlungen: dict) -> str:
    hat_wp = empfehlungen.get("waermepumpe") is not None
    lines = [
        "## Förderhinweise\n",
        "- **KfW 270 – Erneuerbare Energien (Strom):** Günstige Finanzierung für PV-Anlagen und Speicher; ",
        "  Kreditbetrag bis 150 Mio. € je Vorhaben, attraktive Zinsen.\n",
        "- **Einspeisevergütung (EEG):** Für überschüssigen Strom wird eine gesetzlich festgelegte Vergütung gezahlt. ",
        "  Aktuell auf niedrigem Niveau – Eigenverbrauchsoptimierung durch Speicher ist wirtschaftlich sinnvoller.\n",
        "- **Steuerbefreiung PV (§ 3 Nr. 72 EStG):** Für Anlagen bis 30 kWp auf Einfamilienhäusern ",
        "  keine Einkommensteuer auf Einnahmen aus Einspeisung und Eigenverbrauch.\n",
        "- **Umsatzsteuer 0% auf PV-Anlage:** Seit 01.01.2023 gilt 0% MwSt. beim Kauf und der Installation ",
        "  von PV-Anlagen für Privatpersonen (§ 12 Abs. 3 UStG).\n",
    ]
    if hat_wp:
        lines += [
            "- **BEG – Bundesförderung effiziente Gebäude (Wärmepumpe):** ",
            "  Bis zu 70% der förderfähigen Kosten möglich (Grundförderung 30% + Effizienzbonus + Klimageschwindigkeitsbonus). ",
            "  Antrag vor Auftragsvergabe über BAFA stellen!\n",
            "- **KfW 458 – Klimafreundlicher Neubau / Sanierung:** Ergänzende Finanzierung für Heizungsaustausch.\n",
        ]
    lines += [
        "\n> ⚠️ **Hinweis:** Alle Förderungen werden als *mögliche Preisanpassungen* kommuniziert – ",
        "  niemals verbindlich versprechen. Förderhöhe und Verfügbarkeit können sich ändern. ",
        "  Kunde sollte sich vor Antragstellung eigenständig informieren oder wir vermitteln einen Förderberater.\n",
    ]
    return "".join(lines)


def _vertriebshinweise(answers: dict, empfehlungen: dict) -> str:
    hinweise = []
    verbrauch = answers.get("jahresverbrauch")
    hat_wb = _hat_wallbox(answers)
    hat_wp = empfehlungen.get("waermepumpe") is not None
    hat_fossil = _hat_fossil_heizung(answers)
    speicher = empfehlungen.get("speicher")
    mitbew = answers.get("mitbewerber", "nein")
    entscheid = answers.get("entscheider", "unbekannt")

    if speicher:
        for h in speicher.get("hinweise", []):
            if "**" in h:
                hinweise.append(h)

    if hat_fossil and not hat_wp:
        hinweise.append(
            "**Cross-Selling: Wärmepumpe ansprechen** – Kunde hat fossile Heizung (Öl/Gas). "
            "BEG-Förderung als Türöffner nutzen. GEG-Pflichten erwähnen."
        )

    if verbrauch and verbrauch > 6000:
        hinweise.append(
            f"**Hoher Verbrauch ({verbrauch:.0f} kWh/a)** – größere PV-Anlage und Speicher argumentieren"
        )

    if hat_wb and hat_wp:
        hinweise.append(
            "**Cross-Selling-Trifecta:** PV + Speicher + Wallbox + Wärmepumpe = Komplett-Paket. "
            "Synergien im Angebot hervorheben (Gesamtsystem-Effizienz)."
        )

    baujahr = answers.get("baujahr")
    if baujahr and baujahr < 1990:
        hinweise.append(
            f"**Altbau {baujahr}:** Dachzustand, Leitungsquerschnitte und Zählerschrank prüfen lassen. "
            "Ggf. E-Check empfehlen vor Installation."
        )

    dach_hind = answers.get("dach_hindernisse", "keine")
    if dach_hind and dach_hind.lower() not in ("keine", "nein", ""):
        hinweise.append(
            f"**Dach-Hindernisse:** '{dach_hind}' – bei Vor-Ort-Termin detailliert prüfen, "
            "Verschattungsanalyse ggf. mit Tool durchführen."
        )

    if mitbew and mitbew.lower() not in ("nein", "n", "keine", ""):
        hinweise.append(
            f"**Mitbewerber:** Kunde hat bereits Angebote eingeholt ({mitbew}). "
            "Differenzierung über Service, Garantie und Systemlösung aus einer Hand betonen."
        )

    if entscheid and any(
        x in entscheid.lower() for x in ("nein", "partner", "fehlt", "abwesend")
    ):
        hinweise.append(
            "**⚠️ Entscheider nicht vollständig:** Zweitermin mit allen Entscheidern einplanen "
            "oder digitale Präsentation/Zusammenfassung zum Weiterleiten mitschicken."
        )

    # Offene Punkte
    offen = []
    if answers.get("hauptsicherung", "unbekannt") in ("unbekannt", ""):
        offen.append("Hausanschlussleistung / Hauptsicherung noch klären")
    if answers.get("technikraum", "unbekannt") in ("unbekannt", ""):
        offen.append("Stellplatz für Wechselrichter und Speicher bestätigen")
    if answers.get("dachzustand", "unbekannt") in ("unbekannt", ""):
        offen.append("Dachzustand vor Ort begutachten")
    if hat_wp and answers.get("ausseneinheit_platz", "unbekannt") in ("unbekannt", ""):
        offen.append("Aufstellort Außeneinheit Wärmepumpe klären")

    result = "## Vertriebshinweise *(intern – nicht für den Kunden)*\n\n"
    if hinweise:
        result += "### Empfehlungen & Hinweise\n"
        for h in hinweise:
            result += f"- {h}\n"
        result += "\n"
    if offen:
        result += "### Noch zu klären\n"
        for o in offen:
            result += f"- ❓ {o}\n"
        result += "\n"
    if not hinweise and not offen:
        result += "_Keine besonderen Hinweise._\n\n"
    return result


def _naechste_schritte(answers: dict, empfehlungen: dict) -> str:
    schritte = [
        "- [ ] Vor-Ort-Termin zur Dachvermessung und Technikraumbesichtigung vereinbaren",
        "- [ ] Formelles Angebot auf Basis der Vor-Ort-Messung erstellen",
        "- [ ] Angebot an Kunden zusenden (Ziel: innerhalb 5 Werktage)",
    ]

    if empfehlungen.get("waermepumpe"):
        schritte.append(
            "- [ ] Heizlastberechnung beauftragen (Voraussetzung für WP-Dimensionierung)"
        )
        schritte.append(
            "- [ ] BAFA-Förderantrag vor Auftragsvergabe einreichen (Kunde informieren!)"
        )

    if _hat_wallbox(answers):
        schritte.append(
            "- [ ] Kabelweg und Unterverteilung für Wallbox prüfen / einplanen"
        )
        if answers.get("wallbox_22kw", False):
            schritte.append(
                "- [ ] Anmeldung 22-kW-Wallbox beim Netzbetreiber klären (§ 19 NAV)"
            )

    schritte += [
        "- [ ] Netzbetreiber-Anmeldung für PV-Anlage vorbereiten",
        "- [ ] Finanzierungsangebot / KfW-Konditionen anfragen (falls Kunde Interesse gezeigt hat)",
        "- [ ] Follow-up-Anruf 48h nach Angebotsversand einplanen",
    ]

    entscheid = answers.get("entscheider", "")
    if entscheid and any(x in entscheid.lower() for x in ("nein", "partner", "fehlt")):
        schritte.append("- [ ] Zweitermin mit allen Entscheidern einplanen")

    return "## Nächste Schritte\n\n" + "\n".join(schritte) + "\n"


def erstelle_markdown(answers: dict, empfehlungen: dict, datum: str) -> str:
    kundenname = answers.get("kundenname") or "Unbekannt"
    vertriebler = answers.get("vertriebler") or ""

    md = f"# Angebots-Summary – {kundenname}\n\n"
    md += f"**Erstellt:** {datum}"
    if vertriebler:
        md += f"  |  **Vertriebler:** {vertriebler}"
    md += "\n\n---\n\n"

    # Eckdaten
    md += "## Eckdaten Kunde\n\n"
    md += "| Merkmal | Wert |\n|---|---|\n"
    felder = [
        ("Kundenname", answers.get("kundenname", "—")),
        ("Interesse", ", ".join(answers.get("interesse", [])) or "—"),
        ("Baujahr Gebäude", str(answers.get("baujahr", "unbekannt"))),
        (
            "Wohnfläche",
            f"{answers['wohnflaeche']:.0f} m²"
            if answers.get("wohnflaeche")
            else "unbekannt",
        ),
        ("Personen HH", str(answers.get("personen", "unbekannt"))),
        (
            "Jahresstromverbrauch",
            f"{answers['jahresverbrauch']:.0f} kWh/a"
            if answers.get("jahresverbrauch")
            else "unbekannt",
        ),
        (
            "Dachfläche (nutzbar)",
            f"{answers['dachflaeche']:.0f} m²"
            if answers.get("dachflaeche")
            else "unbekannt",
        ),
        ("Dachausrichtung", answers.get("ausrichtung", "unbekannt")),
        ("Dachneigung", answers.get("dachneigung", "unbekannt")),
        ("Heizsystem", answers.get("heizung", "unbekannt")),
        ("Elektroauto", answers.get("elektroauto", "unbekannt")),
        ("Budget", answers.get("budget", "nicht genannt")),
        ("Zeitrahmen", answers.get("zeitrahmen", "nicht genannt")),
    ]
    for key, val in felder:
        md += f"| {key} | {val} |\n"
    md += "\n---\n\n"

    # Empfohlene Konfiguration
    md += "## Empfohlene Konfiguration\n\n"

    pv = empfehlungen.get("pv")
    if pv:
        paket = pv["paket"]
        md += f"### {paket['name']}\n"
        md += f"- **Leistung:** {paket['kwp']} kWp\n"
        md += f"- **Preis:** {paket['preis']:,.0f} € *(inkl. Montage, Wechselrichter, Anmeldung)*\n"
        md += "- Vollständiges PV-Paket mit allen notwendigen Komponenten und Inbetriebnahme.\n"
        if pv.get("hinweise"):
            for h in pv["hinweise"]:
                md += f"- *{h}*\n"
        md += "\n"

    speicher = empfehlungen.get("speicher")
    if speicher:
        paket = speicher["paket"]
        md += f"### {paket['name']}\n"
        md += f"- **Kapazität:** {paket['kwh']} kWh\n"
        md += f"- **Preis:** {paket['preis']:,.0f} €\n"
        md += "- Hausbatteriespeicher zur Maximierung des Eigenverbrauchs, optional mit Notstromfunktion.\n"
        if speicher.get("hinweise"):
            for h in speicher["hinweise"]:
                if not h.startswith("Berechnung"):
                    md += f"- *{h}*\n"
        md += "\n"

    wallbox = empfehlungen.get("wallbox")
    if wallbox:
        paket = wallbox["paket"]
        md += f"### {paket['name']}\n"
        md += f"- **Ladeleistung:** {paket['kw']} kW\n"
        md += f"- **Preis:** {paket['preis']:,.0f} € *(inkl. Installation)*\n"
        md += f"- {paket['beschreibung']}\n"
        if wallbox.get("hinweise"):
            for h in wallbox["hinweise"]:
                md += f"- *{h}*\n"
        md += "\n"

    wp = empfehlungen.get("waermepumpe")
    if wp:
        paket = wp["paket"]
        md += f"### {paket['name']}\n"
        md += f"- **Heizleistung:** {paket['kw']} kW\n"
        md += f"- **Preis:** {paket['preis']:,.0f} € *(inkl. Installation)*\n"
        md += f"- {paket['beschreibung']}\n"
        if wp.get("hinweise"):
            for h in wp["hinweise"]:
                md += f"- *{h}*\n"
        md += "\n"

    md += "---\n\n"

    # Preisübersicht
    md += "## Preisübersicht\n\n"
    md += "| Komponente | Produkt | Preis |\n"
    md += "|---|---|---:|\n"

    gesamt = 0
    if pv:
        p = pv["paket"]
        md += f"| Photovoltaik-Anlage | {p['name']} | {p['preis']:,.0f} € |\n"
        gesamt += p["preis"]
    if speicher:
        p = speicher["paket"]
        md += f"| Batteriespeicher | {p['name']} | {p['preis']:,.0f} € |\n"
        gesamt += p["preis"]
    if wallbox:
        p = wallbox["paket"]
        md += f"| Wallbox | {p['name']} | {p['preis']:,.0f} € |\n"
        gesamt += p["preis"]
    if wp:
        p = wp["paket"]
        md += f"| Wärmepumpe | {p['name']} | {p['preis']:,.0f} € |\n"
        gesamt += p["preis"]

    md += f"\n**Gesamtpreis (Listenpreis): {gesamt:,.0f} €**\n\n"
    md += "> *Alle Preise inkl. Montage, ohne MwSt. (0% MwSt. auf PV-Komponenten gemäß § 12 Abs. 3 UStG). "
    md += "Finaler Preis nach Vor-Ort-Begehung.*\n\n"
    md += "---\n\n"

    # Förderhinweise
    md += _foerderhinweise(empfehlungen)
    md += "\n---\n\n"

    # Vertriebshinweise
    md += _vertriebshinweise(answers, empfehlungen)
    md += "\n---\n\n"

    # Nächste Schritte
    md += _naechste_schritte(answers, empfehlungen)

    if answers.get("notizen"):
        md += "\n---\n\n## Notizen\n\n"
        md += answers["notizen"] + "\n"

    md += f"\n---\n*Generiert mit HeliosTech Solar-Assistent | {datum}*\n"
    return md


# ---------------------------------------------------------------------------
# Datei speichern
# ---------------------------------------------------------------------------


def speichere_markdown(md_content: str, kundenname: str, datum_str: str) -> str:
    safe_name = "".join(
        c if c.isalnum() or c in ("-", "_") else "_" for c in kundenname
    )
    safe_date = datum_str.replace(" ", "_").replace(":", "-").replace(".", "-")
    filename = f"angebot_{safe_name}_{safe_date}.md"
    try:
        with open(filename, "w", encoding="utf-8") as f:
            f.write(md_content)
        return filename
    except OSError:
        # Fallback: aktuelles Verzeichnis
        fallback = f"angebot_{safe_date}.md"
        with open(fallback, "w", encoding="utf-8") as f:
            f.write(md_content)
        return fallback


# ---------------------------------------------------------------------------
# Begrüßung & Hauptprogramm
# ---------------------------------------------------------------------------


def print_welcome() -> None:
    print()
    print(line("═"))
    logo = [
        "  ██╗  ██╗███████╗██╗     ██╗ ██████╗ ███████╗",
        "  ██║  ██║██╔════╝██║     ██║██╔═══██╗██╔════╝",
        "  ███████║█████╗  ██║     ██║██║   ██║███████╗",
        "  ██╔══██║██╔══╝  ██║     ██║██║   ██║╚════██║",
        "  ██║  ██║███████╗███████╗██║╚██████╔╝███████║",
        "  ╚═╝  ╚═╝╚══════╝╚══════╝╚═╝ ╚═════╝ ╚══════╝",
    ]
    for row in logo:
        print(f"{C.YELLOW}{C.BOLD}{row}{C.RESET}")
    print()
    print(f"  {C.BOLD}{C.WHITE}Solar-Vertriebs-Assistent{C.RESET}  {dim('v1.0')}")
    print(f"  {dim('Für den Vertriebler – auf dem Weg zum Kunden')}")
    print()
    print(line("═"))
    print()
    print(
        wrap(
            "Dieses Tool begleitet Sie durch 3 Schritte: "
            "(1) Vor-Qualifizierung – bekannte Eckdaten eingeben → "
            "(2) Maßgeschneiderter Fragekatalog wird automatisch ausgegeben → "
            "(3) Antworten aus dem Kundengespräch eingeben & Angebots-Summary als Markdown erstellen.",
            indent=2,
        )
    )
    print()


def print_zusammenfassung_phase1(data: dict) -> None:
    """Zeigt eine kompakte Zusammenfassung der Phase-1-Daten."""
    print()
    print(f"  {bold('Zusammenfassung Vor-Qualifizierung:')}")
    print()
    felder = [
        ("Kunde", data.get("kundenname") or dim("—")),
        ("Interesse", ", ".join(data.get("interesse", [])) or dim("nicht angegeben")),
        (
            "Dachfläche",
            f"{data['dachflaeche']:.0f} m²"
            if data.get("dachflaeche")
            else dim("unbekannt"),
        ),
        ("Ausrichtung", data.get("ausrichtung", dim("unbekannt"))),
        (
            "Jahresverbrauch",
            f"{data['jahresverbrauch']:.0f} kWh"
            if data.get("jahresverbrauch")
            else dim("unbekannt"),
        ),
        ("Heizung", data.get("heizung", dim("unbekannt"))),
        ("E-Auto", data.get("elektroauto", dim("unbekannt"))),
        (
            "Baujahr",
            str(data.get("baujahr")) if data.get("baujahr") else dim("unbekannt"),
        ),
        (
            "Personen",
            str(data.get("personen")) if data.get("personen") else dim("unbekannt"),
        ),
    ]
    for key, val in felder:
        print(f"  {dim(key + ':')} {val}")
    print()


def main() -> None:
    print_welcome()

    # ── Phase 1 ──────────────────────────────────────────────────────────────
    data = phase1_vorqualifizierung()
    print_zusammenfassung_phase1(data)

    # ── Phase 2 ──────────────────────────────────────────────────────────────
    phase2_fragekatalog(data)

    print()
    if not ask_yn(
        "Antworten jetzt eingeben und Angebots-Summary erstellen?",
        default="j",
    ):
        print(
            f"\n  {yellow('Fragekatalog ausgegeben. Viel Erfolg beim Kundengespräch!')}\n"
        )
        sys.exit(0)

    # ── Phase 3 ──────────────────────────────────────────────────────────────
    answers = phase3_antworten_eingeben(data)

    print()
    print(f"  {green('⟳')}  Berechne Produktempfehlungen…")
    empfehlungen = _berechne_empfehlungen(answers)

    # Vorschau im Terminal
    section_header("ERGEBNIS – PRODUKTEMPFEHLUNG VORSCHAU")
    gesamt = 0
    produkte = [
        ("PV-Anlage", empfehlungen.get("pv")),
        ("Speicher", empfehlungen.get("speicher")),
        ("Wallbox", empfehlungen.get("wallbox")),
        ("Wärmepumpe", empfehlungen.get("waermepumpe")),
    ]
    for label, emp in produkte:
        if emp:
            paket = emp["paket"]
            preis = paket["preis"]
            gesamt += preis
            print(
                f"  {green('✔')} {bold(label)}: {paket['name']} – {yellow(f'{preis:,.0f} €')}"
            )
        else:
            print(f"  {dim('–')} {dim(label)}: {dim('nicht empfohlen')}")

    print()
    print(f"  {bold('Gesamtpreis (Listenpreis):')} {green(bold(f'{gesamt:,.0f} €'))}")
    print()
    print(line())

    # Markdown erstellen & speichern
    datum_str = datetime.now().strftime("%d.%m.%Y %H:%M")
    md_content = erstelle_markdown(answers, empfehlungen, datum_str)
    kundenname = answers.get("kundenname") or "Kunde"
    datum_file = datetime.now().strftime("%Y%m%d_%H%M")

    filename = speichere_markdown(md_content, kundenname, datum_file)

    print()
    print(f"  {green('✔')}  Angebots-Summary gespeichert: {bold(filename)}")
    print()
    print(
        f"  {yellow('💡')}  Tipp: Datei mit einem Markdown-Viewer öffnen für beste Darstellung."
    )
    print(
        f"  {yellow('💡')}  Die Vertriebshinweise am Ende der Datei sind nur für internen Gebrauch."
    )
    print()
    print(line("═"))
    print(f"  {green(bold('Viel Erfolg beim Abschluss!'))}")
    print(line("═"))
    print()


if __name__ == "__main__":
    main()
