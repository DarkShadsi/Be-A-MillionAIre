# Question bank

`questions.csv` is the active UTF-8 question bank, not an empty template. It currently contains **54 questions**:

| Category identifier | Easy | Medium | Hard | Playable |
| --- | ---: | ---: | ---: | --- |
| `AI_FUNDAMENTALS` | 6 | 6 | 6 | Yes |
| `KNOWLEDGE_AND_PROBLEM_REPRESENTATION` | 6 | 6 | 6 | Yes |
| `SEARCH_AND_GAME_PLAYING` | 6 | 6 | 6 | Yes |
| `MACHINE_LEARNING` | 0 | 0 | 0 | No |
| `NEURAL_NETWORKS` | 0 | 0 | 0 | No |
| `DEEP_LEARNING` | 0 | 0 | 0 | No |
| `FUTURE_OF_AI` | 0 | 0 | 0 | No |
| `RESEARCH_IN_AI` | 0 | 0 | 0 | No |

A category needs at least five questions at each difficulty to start. A complete eight-category release needs at least 120 questions distributed across all 24 category/difficulty pools. The five empty categories are still incomplete. Extra questions in the three populated categories do not satisfy a missing pool.

## Format

Keep this exact header and column order:

```csv
id,category,difficulty,text,choice_a,choice_b,choice_c,choice_d,correct_choice_id,clue
```

- Use a unique, nonblank `id` for every question.
- Use a category identifier from the table above and `EASY`, `MEDIUM` or `HARD` for difficulty. Identifiers are case-sensitive.
- Supply a nonblank question, all four choices, an `A`/`B`/`C`/`D` correct-choice ID and a clue.
- Quote fields containing commas, double quotes or line breaks using standard CSV escaping; double a quote inside a quoted field. Save as UTF-8. The reader accepts a UTF-8 BOM and ignores empty lines.

`CsvQuestionRepository` validates the header, field count, required values, unique IDs and model identifiers before exposing the bank. Invalid data reports the file and CSV record. Validation checks structure; authors must also review factual correctness, ambiguity, clue usefulness and difficulty against the activity sources. Keep those references with the content-review work and have a second person review additions.

Each round selects five questions from each difficulty pool, in Easy → Medium → Hard order. It shuffles each selected question's choices and remaps the correct A–D ID. The source CSV remains unchanged. With six questions per difficulty, the selected subset can vary; random runs can still repeat an ordering.

## Check changes

From the project root, build and run practice mode:

```cmd
scripts\build-source.cmd
scripts\run-source.cmd --cli
```

The existing `cli.cmd` is also supported in a Maven checkout. In practice mode, `R` reloads the CSV from the category menu; `I` reveals model details including the answer. Practice mode does not save GUI score history. Restart the GUI after editing the bank because its startup state retains the loaded questions.

After a Maven build, run the complete release-input check:

```cmd
java -cp "target/classes;target/lib/*" com.beamillionaire.tools.ReleaseCheck
```

The current five empty categories cause this command to fail. It also checks required audio resources. Separately check that new questions and every choice ordering fit the stationary GUI reading area in both themes; CSV validity alone does not verify visual fit.
