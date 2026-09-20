package com.beamillionaire.storage;

import com.beamillionaire.application.port.QuestionRepository;
import com.beamillionaire.domain.Category;
import com.beamillionaire.domain.Choice;
import com.beamillionaire.domain.Difficulty;
import com.beamillionaire.domain.Question;
import com.beamillionaire.domain.QuestionBank;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Loads one CSV and validates its questions before exposing the bank. */
public final class CsvQuestionRepository implements QuestionRepository {
    private static final List<String> QUESTION_HEADER =
            List.of("id", "category", "difficulty", "text", "choice_a", "choice_b", "choice_c", "choice_d", "correct_choice_id", "clue");
    private static final CSVFormat FORMAT = CSVFormat.RFC4180.builder()
            .setIgnoreEmptyLines(true)
            .get();

    private record Row(Path file, long number, List<String> values) {
        String value(int index) {
            return values.get(index);
        }

        IOException invalid(String message) {
            return csvError(file, number, message, null);
        }

        IOException invalid(RuntimeException cause) {
            return csvError(file, number, cause.getMessage(), cause);
        }
    }


    private final Path dataDirectory;

    public CsvQuestionRepository(Path dataDirectory) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "Question data directory is required.");
    }

    @Override
    public QuestionBank load() throws IOException {
        var rows = readRows(dataDirectory.resolve("questions.csv"), QUESTION_HEADER);
        var bank = new ArrayList<Question>();
        var ids = new java.util.HashSet<String>();
        for (Row row : rows) {
            requireValues(row, QUESTION_HEADER);
            if (!ids.add(row.value(0))) throw row.invalid("Duplicate question ID: " + row.value(0));
            try {
                bank.add(new Question(row.value(0), Category.valueOf(row.value(1)),
                        Difficulty.valueOf(row.value(2)), row.value(3),
                        List.of(new Choice("A",row.value(4)), new Choice("B",row.value(5)),
                                new Choice("C",row.value(6)), new Choice("D",row.value(7))),
                        row.value(8), row.value(9)));
            } catch (IllegalArgumentException exception) {
                throw row.invalid(exception);
            }
        }
        return new QuestionBank(bank);
    }

    private static List<Row> readRows(Path file, List<String> header) throws IOException {
        var records = new ArrayList<CSVRecord>();
        long nextRecord = 1;
        try (var reader = new PushbackReader(Files.newBufferedReader(file, StandardCharsets.UTF_8), 1)) {
            int first = reader.read();
            if (first != -1 && first != '\uFEFF') reader.unread(first);
            try (var parser = FORMAT.parse(reader)) {
                for (CSVRecord record : parser) {
                    records.add(record);
                    nextRecord = record.getRecordNumber() + 1;
                }
            }
        } catch (IOException | UncheckedIOException exception) {
            throw csvError(file, nextRecord, exception.getMessage(), exception);
        }
        if (records.isEmpty() || !records.getFirst().toList().equals(header)) {
            throw csvError(file, 1, "Expected exact header: " + String.join(",", header), null);
        }
        var rows = new ArrayList<Row>();
        for (CSVRecord record : records.subList(1, records.size())) {
            if (record.size() != header.size()) {
                throw csvError(file, record.getRecordNumber(), "Expected " + header.size()
                        + " fields, found " + record.size() + ".", null);
            }
            rows.add(new Row(file, record.getRecordNumber(), record.stream().map(String::strip).toList()));
        }
        return rows;
    }

    private static void requireValues(Row row, List<String> header) throws IOException {
        for (int index = 0; index < row.values().size(); index++) {
            if (row.value(index).isEmpty()) throw row.invalid(header.get(index) + " must not be blank.");
        }
    }

    private static IOException csvError(Path file, long record, String detail, Throwable cause) {
        return new IOException("Invalid CSV " + file + " at record " + record + ": " + detail, cause);
    }
}
