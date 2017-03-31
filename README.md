# ThingsQuiz
Quiz app for RPi3 + Android Things + Philips Hue (min 2 lamps)

## The game

You need two teams and one game host. The LogCat functions for now as
UI. Some real UI has to be added (so you can hook up the Pi to a 
screen)

Two teams compete against each other, to get as many points as possible. 
Everytime a question is shown (for now only in the logcat), the team 
that hits the button first will be enlightened (the lamp is turned on).
Team 1 uses the left CTRL key and team 2 uses the right CTRL key. The 
team that presses first is allowed to say the answer. The game host 
decides whether the answer is correct. If correct, the game host hits 
the 'Y' (yes) key for approval or 'N' (no) to decline. One point is 
awarded to the team if the answer was correct. At the end of the game,
the scores and the winner are displayed.

The game has currently 3 questions. Press 'R' anytime to restart the game.

## Setup

I used the following setup. Also works without the Hue lamps, but hey,
that's no fun at all!

- Raspberry Pi 3
- Philip Hue Bridge 2.0
- 2 Philips Hue Color Lamps
- A keyboard

## How does it work

- The Pi will send instructions to the Hue Bridge, make sure they are on the same network
- Make sure at least 2 color lamps are connected to the Hue Bridge
- The app will address them as light '1' and '2'
- The keyboard acts as input, actually I want big red arcade buttons!

## Configuring it with your Hue setup

- In the HueHubControlService, set the IP address of your Hue Hub and 
  your Hue Hub username. See the Hue docs to get a username. 
  (https://developers.meethue.com/documentation/getting-started)
- The app tries to communicate with lamp 1 and 2. Make sure those lamps
  are configured
- The game has now 3 questions. It's just a list in `QuizActivity.java`,
  adapt it to your needs :)
  
## How to start

- Have Android Things running on your Pi (https://d.android.com/things/hardware/raspberrypi.html)
- Connect your Pi to your dev machine and launch the app
- A log line should appear: "Press any key to start"
- Have fun!

------

Created by Leon Joosse, Tom Sabel, Hans van der Scheer and Roland Kierkels 
during a Dutch Android User Group meetup. http://dutchaug.org
