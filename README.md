# 1. Gruppeninformation
Vorname | Nachname | Matrikelnummer | Studienkennzahl | E-Mail
--- | --- | --- | --- | ---
Lukas | Lidauer | 01635862 | 033 532 | lukas.lidauer@tuwien.ac.at
Jan |	König |	01007167 |	033 532 | e1007167@student.tuwien.ac.at

# 2. Spielbeschreibung
## 2.1. Spielidee

In einer Fertigungsanlage im Weltraum entwickelt ein Android ein Bewusstsein. Um seiner drohenden Auslöschung wegen auffälligem Verhalten zu entgehen beschließt der Android zu fliehen.

escape.exe spielt in einem futuristischen und stark stilisierten Weltraum-Setting mit Pixelgrafik. Dabei handelt es sich um einen 2D-sidescrolling Runner. Der Android, die Spielfigur, bewegt sich durchgehend und ohne dafür nötige Eingabe von links nach rechts durch das Level. Zwei Fähigkeiten können dabei von den Spieler*innen ausgelöst werden: Sprung und Gravity-Change. 

## 2.2 Ablauf

Das Spiel beginnt mit einem kurzen Video-Intro, in dem die Story vorgestellt wird. Der Android ändert während dem Intro die Farbe seines Front-Screens, um darzustellen, dass er ein Bewusstsein entwickelt hat. 

Die Geschichte wird in einer Endsequenz zu Ende erzählt. Der Plan Zwischensequenzen zur Fortsetzung der Story zu nutzen wurde auf Grund der sehr einfachen Geschichte gestrichen. Intro und End-Sequenz haben eine integrierte Skip-Funktion. 

## 2.3 Game-Loop & Steuerung

Das Spiel startet mit einem Hauptmenü. Vom Hauptmenü aus können Highscores eingesehen werden, das Spiel fortgesetzt oder ein neues Spiel begonnen werden, sowie die Anwendung geschlossen werden. 

<img src="https://user-images.githubusercontent.com/2223159/224555907-9c5256e3-db31-451b-8592-9afce821ecaa.PNG" width="600">


Im eigentlichen Game-Loop gibt es zwei Fähigkeiten, die von den Spieler*innen ausgelöst werden können: Sprung und Gravity-Change. Mit einem Sprung können einfache Hindernisse überwunden werden. Gravity Change ändert die Ausrichtung der Magnete, die im Zero-G Umfeld nötig sind, um entweder am Boden oder der Decke zu gehen. D.h. durch das Auslösen der Gravity-Change Fähigkeit wechselt der Android zwischen dem Boden und der Decke als Laufplattform. Sprung und Gravity Change können nur in genau dieser Reihenfolge kombiniert werden. 

Gesteuert wird das Spiel dabei nativ mit Eingaben am Touchbildschirm. Sprung und Gravity Change werden über zwei voneinander unabhängigen Touchfelder ausgelöst. Um zu springen muss das linke Viertel des Displays berührt werden, Gravity Change kann über das drücke des restlichen Displays ausgelöst werden. Besonderes Augenmerk soll auf die konkrete Umsetzung der Gravity-Mechanic gelegt werden. Eine Gravitationsumkehr soll sich geschmeidig anfühlen, d.h. der Richtungsvektor auf der vertikalen Achse soll sich nicht linear verändern.

Die ursprüngliche Idee Buttons zum Auslösen dieser Fähigkeiten zu verwenden wurde auf Grund der mangelhaften Praktikabilität verworfen. Eine ursprüngliche geplante alternative Steuerung via Tastatur wurde verworfen, mit der Maus können stattdessen die Touchgesten nachempfunden werden.

Insgesamt wurden zwei Splash-Screens umgesetzt. Eine Game Finished Animation, die nach dem Outro getriggered wird, sowie eine Level Finished Animation, die die Leistung in einem Level zusammenfasst und als Ladebildschirm zwischen zwei Leveln dient: 

<img src="https://user-images.githubusercontent.com/2223159/224556247-1cd00587-7dc4-4118-84e0-e259aa65ae97.PNG" width="600">



## 2.4 Menü

Um möglichst einen großen Anteil des Displays für die Spieldarstellung zu reservieren, wurde die ursprüngliche Idee einer Menüleiste verworfen. Stattdessen befindet sich am oberen linken Rand ein Pause-Button. Wird dieser gedrückt, so wird das Spiel pausiert und ein Pause-Menü samt Steuerungserklärung öffnet sich. Statt einem textbasierten Hilfe-Dialog wurde eine grafische Umsetzung gewählt. Ebenfalls in jenem Menü enthalten ist die Option das Spiel zu muten. Über dieses Menü kann man schließlich auch ins Hauptmenü zurückkehren. 

<img src="https://user-images.githubusercontent.com/2223159/224556319-532035b7-b85c-4c83-ab14-1888181596d5.png" width="600">

Der aktuelle Score ist im Unterpunkt Highscores des Hauptmenüs zu sehen. Wird ein Leben verloren, so werden Punkte vom Score abgezogen. Wird ein Level beendet, werden Punkte zum Score addiert. 

Highscores werden für jedes Level separat geführt (wobei im Untermenü Highscores jeweils die höchste erreichte Punktzahl pro Level angezeigt wird). Ebenfalls wird die höchste Punktzahl bei Spielabschluss gelistet.

## 2.5 Hindernisse & Checkpoints

Neben diversen stationären Hindernissen wie Stufen oder Bodenfallen gibt es auch Verteidigungssysteme in der Fertigungsanlage. So müssen die Spieler*innen Laserkanonen ausweichen, um das Ziel zu erreichen. Falls die Spielfigur ein Hindernis berührt oder von einem Gegner besiegt wird, soll ein Splashscreen aufgerufen werden, bevor das Level neu gestartet wird.

Die Idee Checkpoints zu verwenden wurde verworfen, da die Level dazu zu kurz sind um davon effektiv Gebrauch zu machen. Ebenfalls sind keine begrenzte Leben vorhanden. Stattdessen werden Highscores in Abhängigkeit von den verbrauchten Leben geführt.   

## 2.6 Sprites

Ein animiertes Sprite gibt es für den Androiden. Die Überlegung Plasmaleitungen oder Laser durch Sprites umzusetzen wurde auf Grund der Performance verworfen. Jene Hindernisse sind nun Teil des Tilesets.  

Das fertige Sprite der Spielfigur beinhaltet Animationen für das Laufen, Springen, Abspringen, Aufwachen, Gravity-Change, Death (Anm.: die gelben Störpixel entstehen durch das Vergrößern der Grafik durch das HTML-Format): 

<img src="https://user-images.githubusercontent.com/2223159/224556458-c00979bf-faa9-4100-be77-bc16f46a7299.gif" width="52">

## 2.7 Technische Umsetzung

Im Folgenden ein Diagramm zur Darstellung der Game-States:

<img src="https://user-images.githubusercontent.com/2223159/224556530-5e445dd4-8f67-4425-8e44-395eb5f7e340.jpg" width="800">



Im folgenden ein grobes UML Diagramm zur Erklärung der Abhängigkeiten zwischen den Klassen:

<img src="https://user-images.githubusercontent.com/2223159/224556681-23647e91-340b-44c9-9a68-006a2f5eaf8e.png" width="500">

MenuActivity zeigt das Menü und kann mit einem Knopfdruck die GameActivity starten. Diese hat als Content die CanvasView, welche eine selbst programmierte View ist wo wir den Canvas bemalen. Diese View startet den GameThread, welcher die Renderloop beinhaltet und auch den aktuellen Zustand des Spiels (dieser wird bei jedem durchlauf der Renderloop einmal updated und gezeichnet). Im aktuellen GameState befinden sich Informationen über den Zustand des Spiels, wie Spielerpositionen, Gegnerpositionen, Projektile, Gravitation, Bewegungsvektoren, Kollisionsdaten, Texturen etc.

# 3. Credits
### 3.1 Background Music:
https://pixabay.com/users/zen_man-4257870/

### 3.2 Sprite base:
https://o-lobster.itch.io/

# 4. Walkthrough
[![YOUTUBE](https://user-images.githubusercontent.com/2223159/224563567-3b27facf-7d50-4258-8bc0-7f5e3c8b1a6f.svg)](https://youtu.be/Go0GET-VdmY)
