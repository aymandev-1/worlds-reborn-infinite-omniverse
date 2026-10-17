# WORLDS REBORN — DOOMSDAY SCENES 12–16
## Approved Dialogue, Cinematics & Mission Beats — Claude Code Handoff

**Source:** User-provided `Pasted text(7).txt` (Doomsday Scenes 11–16). The following scenes, lines, beat order, camera notes, gameplay, and checkpoints are preserved from that source, not invented.

**Important:** `audio_missing` is the default until a matching licensed recorded take exists. Subtitles must display each scripted line. Do not silently change dialogue, reveal information early, or let skipping a scene auto-complete rescues.

**Gameplay perspectives:** 012 Sam Wilson; 013 Bucky Barnes; 014 Sam Wilson; 015 Sue Storm; 016 Cyclops. Steve remains an observer; inventories and powers do not cross between protagonists.

---

# SCENE 12 — A WORLD ON ALERT

**ID:** WR1_012  
**Playable character:** Sam Wilson / Captain America  
**Locations:** Response briefing area → New York airspace → emergency landing site  
**Purpose:** Establish a coordinated Avengers response and confirm the other Earth is inhabited.

## Characters

- Sam Wilson.
- Joaquin Torres / Falcon.
- Shang-Chi.
- Scott Lang / Ant-Man.
- Shuri, remotely.
- Civilian helicopter pilot and passengers.
- Ground emergency crews.

## Beat 12.1 — Reports coming in

**Camera:** Establish the response area with several independent recordings of the sky.

SAM: “Different locations. Same object.”

SHURI: “Send the original recordings. Not copies from broadcasts.”

SCOTT: “Please say somebody’s already ruled out a very ambitious advertising campaign.”

SHURI: “I’m working with measurements.”

SAM: “Joaquin, we get above the interference. Scott, Shang-Chi—help the crews below.”

SHANG-CHI: “Where do they need us?”

SAM: “We’ll keep one channel open.”

**Gameplay:** Review the marked flight route and launch as Sam.

**Checkpoint:** `s12_response_started`

## Beat 12.2 — Aerial survey

**Gameplay:**

- Fly through three survey positions.
- Maintain controlled flight long enough to capture a usable sample.
- Joaquin follows a separate complementary route.
- Give a clear indicator when each sample is complete.

JOAQUIN: “First position. Sending.”

SHURI: “Received. Keep your instruments on the same settings.”

SAM: “We’re not getting closer just for a better picture.”

Avoid making the survey a race toward an apparently nearby planet.

**Checkpoint:** `s12_survey_recorded`

## Beat 12.3 — Distress call

A civilian helicopter reports instrument trouble in the affected area.

PILOT: “We’re losing reliable instruments. Requesting assistance.”

JOAQUIN: “Captain, helicopter west of us. They’re struggling to hold position.”

SAM: “I see them. Shuri, save the survey. We’re switching to rescue.”

The precise cause remains under investigation.

## Beat 12.4 — Rescue passengers

**Gameplay:**

- Match the helicopter’s speed.
- Establish a safe approach.
- Secure or extract passengers using an explicitly staged rescue interaction.
- Deliver them to the designated safe area.
- Joaquin guides the pilot toward the landing zone.

SAM: “Look at me. Follow my instructions.”

PASSENGER: “What’s happening up there?”

SAM: “Right now, we’re getting you down.”

JOAQUIN: “Landing area is clear.”

Ground support:

SHANG-CHI: “We’ve cleared the approach.”

SCOTT: “Emergency crews are ready.”

Do not represent physical passenger rescue solely through text.

**Checkpoint:** `s12_passengers_safe`

## Beat 12.5 — Landing and verification

The pilot lands with Joaquin’s assistance. Ground crews receive the passengers.

SAM: “Everyone accounted for?”

JOAQUIN: “Pilot and passengers. All down.”

SHURI: “Your flight recordings are clean enough to isolate the surface.”

## Beat 12.6 — Another population

Shuri displays processed imagery.

Show:
- Different coastlines.
- Surface features.
- Artificial lights consistent with habitation.

SHURI: “This is not just a light in the atmosphere. There are surface features—and artificial lights.”

SAM: “People?”

SHURI: “The evidence supports that.”

SAM: “Then protecting our world doesn’t mean treating theirs as an enemy.”

**Ending:** Sam orders the findings circulated to trusted responders.

**Checkpoint:** `s12_complete`

**Next:** Scene 13.

---

# SCENE 13 — ANOTHER RESPONSE

**ID:** WR1_013  
**Playable character:** Bucky Barnes  
**Locations:** Damaged research facility and extraction perimeter  
**Purpose:** Introduce the New Avengers’ response and connect them to Clint and Kate.

## Characters

- Bucky Barnes.
- Yelena Belova.
- John Walker.
- Red Guardian.
- Ghost.
- Sentry.
- Clint Barton.
- Kate Bishop.
- Facility scientists and security personnel.
- Emergency responders.

## Beat 13.1 — Distress at the facility

**Camera:** Establish a damaged building with evacuation already underway.

YELENA: “Scientists inside. Emergency power failing. That is the mission.”

BUCKY: “People first. Records if we can reach them safely.”

WALKER: “Perimeter’s clear enough to enter.”

GHOST: “Some internal doors are sealed.”

SENTRY: “Tell me what needs holding.”

No hostile invasion is assumed; the facility is damaged during the broader disturbance.

**Checkpoint:** `s13_entry_started`

## Beat 13.2 — Clint and Kate

Inside the first accessible section, Clint and Kate are already helping staff.

KATE: “We’ve got people across the broken section.”

CLINT: “They called us before the comms went down.”

YELENA: “Of course you were already here.”

CLINT: “We can catch up outside.”

**Gameplay:** Follow their established cable route to the first trapped group.

## Beat 13.3 — Open the route

**Gameplay as Bucky:**

- Remove a heavy obstruction.
- Secure an unstable door.
- Escort scientists through the accessible corridor.
- Cover Yelena as she checks adjoining rooms.

BUCKY: “This way. Stay with the person ahead of you.”

YELENA: “Room clear. Moving.”

**Checkpoint:** `s13_first_group_safe`

## Beat 13.4 — Teamwork across the facility

Stage each supporting role visibly:

- Ghost reaches staff behind a sealed section.
- Clint and Kate secure the broken crossing with cable arrows.
- Walker and Red Guardian keep the exit accessible.
- Sentry braces a structural section.
- Bucky and Yelena move people through.

KATE: “Cable’s set.”

CLINT: “One at a time. Keep your weight centred.”

RED GUARDIAN: “The exit stays open!”

WALKER: “Then help me clear it.”

SENTRY: “I can hold this. Keep them moving.”

Sentry’s power does not solve every objective: the team must organise people, reach sealed areas and avoid triggering further structural failure.

**Checkpoint:** `s13_staff_evacuated`

## Beat 13.5 — Recover the records

A scientist identifies an accessible monitoring recorder.

SCIENTIST: “It was measuring the disturbance before the building failed.”

BUCKY: “Can we remove it?”

SCIENTIST: “The recorder. Not the whole array.”

**Gameplay:** Recover the recorder through a safe route. Do not send civilians back into danger.

**Checkpoint:** `s13_recorder_recovered`

## Beat 13.6 — The narrowing intervals

Outside, the scientist reviews a readable summary.

SCIENTIST: “The boundary disturbance keeps returning. Each appearance lasts longer.”

YELENA: “How much longer?”

SCIENTIST: “Enough that the old projections are no longer reliable.”

BUCKY: “Sam needs this.”

Bucky contacts Sam.

BUCKY: “We have independent readings. We should compare them.”

SAM: “Send what you have. We’ll make one picture.”

**Ending:** The teams agree to coordinate. Clint and Kate remain involved.

**Checkpoint:** `s13_complete`

**Next:** Scene 14.

---

# SCENE 14 — FIRST CONTACT

**ID:** WR1_014  
**Playable character:** Sam Wilson  
**Location:** Joint emergency briefing and communications area  
**Purpose:** Bring the teams together and establish first contact with Reed.

## Characters

Physically present or staged in the briefing:

- Sam.
- Joaquin.
- Shang-Chi.
- Scott.
- Bucky.
- Yelena.
- Walker.
- Red Guardian.
- Ghost.
- Sentry.
- Clint.
- Kate.

Remote:
- Shuri.
- Reed Richards, appearing only when contact succeeds.

Use reaction shots and small groups to avoid presenting everyone as a line of silent NPCs.

## Beat 14.1 — One shared briefing

SAM: “We’ve been answering the same emergency from different directions. That changes now.”

YELENA: “Good. Start with the part we can act on.”

BUCKY: “The facility recorder is independent of the flight survey.”

SHURI: “And both show the same trend.”

The display compares:
- Flight readings.
- Facility records.
- Public sky imagery.

SHURI: “The appearances are lasting longer. The intervals are shortening.”

## Beat 14.2 — Define priorities

**Player dialogue choices as Sam:**

A:
SAM: “Protect people while we investigate.”

B:
SAM: “We need contact before assumptions become orders.”

C:
SAM: “Share the evidence. Nobody works from half a picture.”

All lead to the agreed plan:
- Coordinate civilian support.
- Share verified readings.
- Attempt communication.
- Do not assume the other Earth is attacking.

CLINT: “What do we tell the crews?”

SAM: “What we know. And what we don’t.”

**Checkpoint:** `s14_joint_plan_agreed`

## Beat 14.3 — Prepare the contact attempt

Shuri identifies a brief measurable communication opportunity in the disturbance.

SHURI: “There may be a window. We can attempt a transmission.”

SCOTT: “Communication, not a doorway?”

SHURI: “Correct.”

**Gameplay:** Help prepare the communications array:
- Route power through the designated control.
- Align the receiving equipment using readable instrument feedback.
- Test the channel.

This is a coordinated calibration interaction, not unrelated evidence collection.

**Checkpoint:** `s14_array_ready`

## Beat 14.4 — Someone answers

Static resolves into a voice.

REED: “We can see your Earth. We’re trying to stop this too.”

SAM: “This is Captain America. Who am I speaking to?”

REED: “Reed Richards. The Fantastic Four.”

Pause for reactions without turning the introduction into a comedy sequence.

SAM: “We have people studying the disturbance here.”

SHURI: “I can send a limited packet of readings.”

REED: “Send it. I’ll return our measurements.”

## Beat 14.5 — Opposite sides of one event

**Gameplay:** Confirm the data packet is transmitted and received.

SHURI: “The timing matches.”

REED: “We are measuring the same boundary from opposite sides.”

SAM: “Then our objective is to save both worlds.”

REED: “Agreed.”

The connection deteriorates.

REED: “I’ll work on keeping this channel open.”

**Ending:** Contact fails, but the exchanged data remains saved.

**State:** `reed_contact_established = true`

**Checkpoint:** `s14_complete`

**Next:** Scene 15, shifting to the Fantastic Four’s side.

---

# SCENE 15 — ACROSS THE DIVIDE

**ID:** WR1_015  
**Playable character:** Sue Storm  
**Location:** Fantastic Four laboratory and adjoining rooftop systems  
**Purpose:** Show the other world’s response and widen the crisis beyond two universes.

## Characters

- Sue Storm.
- Reed Richards.
- Johnny Storm.
- Ben Grimm.
- Laboratory staff where needed.
- Sam and Shuri through the communication channel.

Doom does not appear.

## Beat 15.1 — After the transmission

**Camera:** Begin on Reed’s failed connection, reversing the previous scene’s perspective.

JOHNNY: “So there really are people over there.”

BEN: “There are people here too. Tell me where you need me.”

REED: “The receiving system is losing alignment.”

SUE: “Then we keep it steady. Johnny, rooftop sensors. Ben, the support assembly.”

Return control to Sue.

**Checkpoint:** `s15_team_deployed`

## Beat 15.2 — Shield the laboratory

A spatial disturbance shakes the building.

**Gameplay as Sue:**
- Place a force field beneath falling debris.
- Shield a staff route.
- Maintain protection while Reed recalibrates.
- Let the player release and reposition fields using existing ability controls.

SUE: “Clear that aisle. Behind me.”

REED: “I need a little longer.”

SUE: “You have it. Use it.”

Success depends on protected routes and systems, not merely waiting beside a timer.

**Checkpoint:** `s15_lab_secured`

## Beat 15.3 — Johnny’s sensor installation

Johnny reports from the rooftop.

JOHNNY: “First sensor’s mounted. Second position is moving.”

SUE: “Wait for my field.”

**Gameplay:** Sue projects a temporary stabilising platform or shield at the marked rooftop installation position, within established range.

Johnny installs the sensor when it is safe.

JOHNNY: “Done. Sending the reading.”

**Checkpoint:** `s15_sensors_online`

## Beat 15.4 — Ben holds the assembly

A support mechanism begins pulling out of alignment.

BEN: “This thing is trying to leave the room.”

REED: “Hold the base. Not the upper ring.”

BEN: “Helpful distinction.”

**Gameplay:** Sue redirects loose debris and protects the connection while Ben braces the machinery.

**Checkpoint:** `s15_assembly_stable`

## Beat 15.5 — Communication restored

REED: “Captain Wilson? Shuri?”

SHURI: “We’re receiving.”

SAM: “What happened?”

SUE: “The same thing your people are dealing with. We’ve got the channel back.”

Clarify in interface and mission text:

**The transmission is stabilised. The incursion is not.**

## Beat 15.6 — More than two worlds

Reed compares new sensor results with the shared data.

REED: “There are additional disturbances.”

SHURI: “Separate from this boundary?”

REED: “Yes. The pattern repeats beyond our two universes.”

SAM: “How many?”

REED: “We don’t have a complete count.”

REED: “Saving our two worlds won’t be enough.”

**Ending:** Show a restrained scientific visualisation of several detected events. Do not present an omniscient complete multiverse map.

**Checkpoint:** `s15_complete`

**Next:** Scene 16, an independent X-Men perspective.

---

# SCENE 16 — THE OTHER SIDE

**ID:** WR1_016  
**Playable character:** Cyclops  
**Locations:** Xavier’s school briefing area → damaged monitoring station → return briefing  
**Purpose:** Introduce the X-Men’s Earth and discover a threatened overlap near the school.

## Characters

- Cyclops.
- Jean Grey.
- Beast.
- Nightcrawler.
- Professor Xavier.
- Magneto.
- Monitoring-station staff.

The X-Men have not contacted the Avengers or Fantastic Four yet.

## Beat 16.1 — Trouble near the school

**Camera:** Establish the school as a living place before entering the briefing.

BEAST: “The station stopped transmitting during the last disturbance.”

CYCLOPS: “Staff?”

BEAST: “No confirmed evacuation.”

XAVIER: “Find them. Bring back the readings if it is safe.”

MAGNETO: “And find out how close this is coming.”

CYCLOPS: “Jean, Hank, Kurt. We go together.”

**Checkpoint:** `s16_briefing_complete`

## Beat 16.2 — Arrival at the station

The team arrives at a damaged monitoring facility.

NIGHTCRAWLER: “There are people inside.”

JEAN: “The upper section is unstable.”

CYCLOPS: “Kurt, locate the staff. Jean, keep the route protected. Hank, stay with me.”

**Gameplay:** Survey the immediate entrance and select the safe route.

## Beat 16.3 — Controlled optic blasts

**Gameplay as Cyclops:**
- Remove designated damaged obstructions with controlled blasts.
- Avoid intact supports and civilian positions.
- Provide clear aiming feedback.
- Do not encourage firing indiscriminately through the building.

JEAN: “That beam is carrying the ceiling.”

CYCLOPS: “Then we leave it.”

A mistake should cause a recoverable setback, not arbitrary permanent civilian death.

**Checkpoint:** `s16_access_opened`

## Beat 16.4 — Extract the staff

Nightcrawler reaches an isolated group.

NIGHTCRAWLER: “I can bring them to the cleared position.”

CYCLOPS: “One group at a time. We’ll hold the route.”

**Gameplay:**
- Clear safe arrival positions for teleportation.
- Protect staff moving through the remaining corridor.
- Jean braces unstable sections.
- Verify every assigned staff member reaches the extraction zone.

SCIENTIST: “The lower recorder is still running.”

BEAST: “I know where to look.”

**Checkpoint:** `s16_staff_safe`

## Beat 16.5 — Recover the recorder

Cyclops escorts Beast to the accessible recorder.

BEAST: “The backup has the last complete survey.”

CYCLOPS: “Take the recorder. We are not rebuilding the station.”

**Gameplay:** Disconnect the recorder using an ordered, readable interaction while Jean protects the route.

**Checkpoint:** `s16_data_recovered`

## Beat 16.6 — An unfamiliar landscape

Outside, Beast examines the recording.

The display shows terrain briefly occupying the same coordinates as part of their world.

CYCLOPS: “That isn’t our landscape.”

BEAST: “No. The coordinates are consistent. The terrain is not.”

JEAN: “Another place crossing into ours?”

BEAST: “That is what the evidence suggests.”

He identifies a predicted overlap location near the school.

CYCLOPS: “Send the coordinates to the Professor.”

## Beat 16.7 — Protect the school

Back at the briefing:

XAVIER: “Begin preparations to move the students if necessary.”

MAGNETO: “Preparations are not protection.”

CYCLOPS: “Then help us protect them. We investigate the site before anything crosses.”

MAGNETO: “And if investigation is not enough?”

CYCLOPS: “We decide from evidence. Not fear.”

Do not make Magneto a villain in this introduction. His concern for his people is real, even though his later decisions become catastrophic.

**Ending:** The team prepares to inspect the overlap location.

**Checkpoint:** `s16_complete`

**Next scene continuity:** Scene 17 explores a surviving fragment of a devastated reality and recovers an evacuation recording.

---
