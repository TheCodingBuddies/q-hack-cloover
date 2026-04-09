# q-hack-cloover
Submission of the q-hackathon cloover challenge 2026

# Der KI-Verkaufscoach für Installateure erneuerbarer Energien

## Das Problem

Stell dir vor, du bist Vertriebsmitarbeiter bei einem Unternehmen, das Solaranlagen und Wärmepumpen für Privathaushalte in Deutschland installiert. Ein neuer Lead kommt rein — ein Hausbesitzer in einer Kleinstadt zwei Stunden südlich von Frankfurt. Du kennst seinen Namen, seine Adresse und weißt, dass er auf eine Wärmepumpen-Anzeige geklickt hat. Das war's.


# Liefergegenstände

- Ein funktionierender Prototyp oder eine Demo
- Eine kurze Präsentation (5–10 Min.) mit:
    - Wie ihr das Problem verstanden habt
    - Euer Lösungsansatz und eine Live-Demo
    - Wichtige Annahmen, die ihr getroffen habt
    - Mögliche Wirkung bei großflächigem Einsatz


## Wie haben wir das Problem verstanden
- Kunde möchte so schnell wie möglich eine Lösung, hat aber keine Ahnung von:
  -  Produkten
  -  Marktsituation
  -  Installationsdauer und Installationsumfang
  -  Gesetze und Förderungen
  -  Welche Daten ein Installateur benötgt
- Vertriebler brauch so umfangreich wie möglich wichtige Daten für die Installation
  -  fährt häufig zu Kunden und kann wegenen fehlender Daten kein Angebot abschließen
  -  das Ergänzen von wichtigen Informationen für die Installation ist ein schwieriger Zeitraubender Prozess
- Der Vertriebler ist auf dem Weg zum Kunden und sein KI-Verkaufs-Coach-Assistent gibt ihm letzte Hinweise.
- Vor Ort sollen der Endkunde und der Vertriebler aus einer Varianz an Angebotsoptionen verschiedene Szenarien durchgehen, ohne wartezeiten in kauf zu nehmen durch ergänzen von optionalen Daten

- Dem Vertriebler werden wird verschiedene Optionen und Fragen vorgeschlagen

## Kurze Demonstration der Demo
![image](architecture_grob.png)

![image](search_view.png)

![image](add_new_customer.png)

## Wichtige Annahmen die wir getroffen haben
- Der Kunde hat bereits sein Interesse für eine bestimmte Produktkategorie angezeigt
- Geodaten wie Solarfaktor, Wärmepumpeneignung (Fläche oder Tiefe) sind für Deutschland vorhanden und können direkt für eine Objekt-Adresse abgefragt werden
- für Förderungen nur die Deutschen Gesetze und Förderungen 
- Preise, Verfügbarkeit und Informationen der Produkte sind vorhanden können Ad-oc 

## Mögliche Wirkung bei großflächigem Einsatz
- Kunden werden gezielter gefragt
- Kunden haben ein größeres Vertrauen in den Prozess
- Kunden haben eine realistischere Vorstellung der Installation
- Installateure haben Planungssicherheit
- Energiewende kann schneller vornschreiten
