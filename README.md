# Be a Millionaire

An Artificial Intelligence-themed desktop game inspired by *Who Wants to Be a Millionaire?*, built in Java with a futuristic UI and a question set focused entirely on AI topics.

## Overview

Players pick a category, then answer 15 increasingly difficult multiple-choice questions to climb a credit ladder — from 100 up to the 1,000,000 grand prize. Along the way, three lifelines ("helps") and three safety checkpoints add strategy to the climb.

## How to Play

1. **Choose a category** from the 8 available AI topics.
2. **Answer 15 questions**, each with 4 options and one correct answer. Difficulty increases as you progress.
3. **Bank your winnings** anytime before locking in an answer, or keep going for a bigger prize.
4. **Wrong answer?** The game ends. Your payout drops to 0 — unless you've passed a checkpoint or have Second Chance active, in which case you fall back to the last checkpoint instead.

## Prize Ladder

| # | Credits | Checkpoint |
|---|---------|------------|
| 1 | 100 | |
| 2 | 300 | |
| 3 | 500 | |
| 4 | 1,000 | ✅ Checkpoint 1 |
| 5 | 2,000 | |
| 6 | 5,000 | |
| 7 | 10,000 | |
| 8 | 15,000 | |
| 9 | 30,000 | ✅ Checkpoint 2 |
| 10 | 50,000 | |
| 11 | 75,000 | |
| 12 | 100,000 | ✅ Checkpoint 3 |
| 13 | 250,000 | |
| 14 | 500,000 | |
| 15 | 1,000,000 | 🏆 Grand Prize |

## Lifelines ("Helps")

- **50:50** — Removes two incorrect options, leaving one wrong and one right answer.
- **Clue** — Reveals a contextual keyword or hint related to the correct answer.
- **Second Chance** — Lets you guess again if your first answer is wrong, with no credit loss or checkpoint drop. Once used, it's consumed for the rest of the game — even if your first guess was actually correct.

Each lifeline can only be used once per game.

## Categories

1. Artificial Intelligence Fundamentals
2. Knowledge and Problem Representation
3. Search and Game Playing
4. Machine Learning
5. Neural Networks
6. Deep Learning
7. Future of AI
8. Research in AI

## Example Scenarios

**Using a lifeline:** At Question 6 (5,000 credits), a player uses 50:50, narrows it down, and answers correctly.

**Hitting a checkpoint:** A player reaches Question 10 (50,000 credits) but misses Question 11. Since they'd passed Checkpoint 2 (30,000 at Question 9), they fall back to 30,000 instead of 0.

**Second Chance in action:** At Question 13 (250,000 credits) with Second Chance active, a player misses on the first try but gets it right on the second attempt — keeping their progress and credits intact.

## Tech Stack

- **Language:** Java
- **UI:** Custom futuristic desktop interface

## Getting Started

```bash
# Clone the repository
git clone <repo-url>
cd be-a-millionaire
```

## License
